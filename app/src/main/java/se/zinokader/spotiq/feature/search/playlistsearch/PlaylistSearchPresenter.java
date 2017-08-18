package se.zinokader.spotiq.feature.search.playlistsearch;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.feature.search.preview.PreviewPlayer;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.util.mapper.TrackMapper;

public class PlaylistSearchPresenter extends BasePresenter<PlaylistSearchView> {

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private PreviewPlayer songPreviewPlayer;
    private String partyTitle;
    private User user;

    static final int LOAD_USER_RESTARTABLE_ID = 765;
    static final int LOAD_PLAYLISTS_RESTARTABLE_ID = 766;

    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        songPreviewPlayer = new PreviewPlayer();

        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS)),
            (lobbyView, userPrivate) -> {
                user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
            },
            (lobbyView, throwable) -> {
                Log.d(LogTag.LOG_SEARCH, "Error when getting user Spotify data");
            });

        restartableReplay(LOAD_PLAYLISTS_RESTARTABLE_ID,
            () -> getUserPlaylists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
            (playlistSearchView, playlistSimplePager) -> {
                playlistSearchView.updatePlaylists(playlistSimplePager.items);
            },
            (playlistSearchView, throwable) -> {
                Log.d(LogTag.LOG_SEARCH, "Error when getting user Spotify data");
            });

        if (savedState == null) {
            start(LOAD_USER_RESTARTABLE_ID);
            start(LOAD_PLAYLISTS_RESTARTABLE_ID);
        }
    }

    @Override
    protected void onDestroy() {
        songPreviewPlayer.release();
        super.onDestroy();
    }

    void startPreview(String previewUrl) {
        songPreviewPlayer.playPreview(previewUrl);
    }

    void stopPreview() {
        songPreviewPlayer.stopPreview();
    }

    private Observable<Pager<PlaylistSimple>> getUserPlaylists() {
        return Observable.create(subscriber -> {
            Map<String, Object> searchOptions = new HashMap<>();
            searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.PLAYLIST_SEARCH_QUERY_RESPONSE_LIMIT);
            searchOptions.put(SpotifyService.OFFSET, 0);

            findPlaylistsRecursively(searchOptions)
                .subscribeOn(Schedulers.io())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS))
                .subscribe(playlistPager -> {
                    subscriber.onNext(playlistPager);
                }, throwable -> {
                    Log.d(LogTag.LOG_SEARCH, "Error when getting playlist data");
                });
        });
    }

    void loadPlaylistSongs(PlaylistSimple playlist) {
        Map<String, Object> searchOptions = new HashMap<>();
        searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.PLAYLIST_TRACK_SEARCH_QUERY_RESPONSE_LIMIT);
        searchOptions.put(SpotifyService.OFFSET, 0);

        partiesRepository.getParty(partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(partySnapshot -> {
                Party dbParty = partySnapshot.child(FirebaseConstants.CHILD_PARTYINFO).getValue(Party.class);
                searchOptions.put(SpotifyService.MARKET, dbParty.getHostMarket());

                findPlaylistTracksRecursively(playlist, searchOptions)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    //filter out local and unplayable tracks
                    .map(playlistPager -> {
                        List<PlaylistTrack> filteredTracks = new ArrayList<>();
                        filteredTracks.addAll(playlistPager.items);
                        for (PlaylistTrack playlistTrack : playlistPager.items) {
                            if (playlistTrack.is_local || !playlistTrack.track.is_playable) {
                                filteredTracks.remove(playlistTrack);
                            }
                        }
                        return filteredTracks;
                    })
                    .map(filteredTracks -> TrackMapper.playlistTracksToSongs(filteredTracks, user))
                    .subscribe(songs -> {
                        if (getView() != null) {
                            if (songs.isEmpty()) getView().showMessage("Playlist is empty");
                            getView().updateSongs(songs);
                        }
                    }, throwable -> {
                        Log.d(LogTag.LOG_SEARCH, "Something went wrong on loading playlist songs");
                        throwable.printStackTrace();
                    });
            });
    }

    private Observable<Pager<PlaylistSimple>> findPlaylistsRecursively(Map<String, Object> searchOptions) {
        int lastOffset = (int) searchOptions.get(SpotifyService.OFFSET);
        return spotifyRepository.getMyPlaylists(searchOptions, spotifyCommunicatorService.getWebApi())
            .concatMap(playlistPager -> {
                if (lastOffset + playlistPager.limit >= SpotifyConstants.PLAYLIST_SEARCH_TOTAL_ITEMS_LIMIT
                    || lastOffset + playlistPager.limit >= playlistPager.total) {
                    return Observable.just(playlistPager);
                }
                else {
                    searchOptions.put(SpotifyService.OFFSET, lastOffset + playlistPager.limit);
                    return Observable.just(playlistPager).concatWith(findPlaylistsRecursively(searchOptions));
                }
            })
            .doOnError(throwable -> Log.d(LogTag.LOG_SEARCH, "Something went wrong on playlist recursion: " + throwable.getMessage()));
    }

    private Observable<Pager<PlaylistTrack>> findPlaylistTracksRecursively(PlaylistSimple playlist, Map<String, Object> searchOptions) {
        int lastOffset = (int) searchOptions.get(SpotifyService.OFFSET);
        return spotifyRepository.getPlaylistTracks(playlist.owner.id, playlist.id, searchOptions, spotifyCommunicatorService.getWebApi())
            .concatMap(playlistPager -> {
                if (lastOffset + playlistPager.limit >= SpotifyConstants.PLAYLIST_TRACK_SEARCH_TOTAL_ITEMS_LIMIT
                    || lastOffset + playlistPager.limit >= playlistPager.total) {
                    return Observable.just(playlistPager);
                }
                else {
                    searchOptions.put(SpotifyService.OFFSET, lastOffset + playlistPager.limit);
                    return Observable.just(playlistPager).concatWith(findPlaylistTracksRecursively(playlist, searchOptions));
                }
            })
            .doOnError(throwable -> Log.d(LogTag.LOG_SEARCH, "Something went wrong on playlist tracks recursion: " + throwable.getMessage()));
    }

}
