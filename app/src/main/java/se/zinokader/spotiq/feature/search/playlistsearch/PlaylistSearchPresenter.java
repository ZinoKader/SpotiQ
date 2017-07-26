package se.zinokader.spotiq.feature.search.playlistsearch;

import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.feature.search.preview.PreviewPlayer;
import se.zinokader.spotiq.model.Song;
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

        //load user
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
            (lobbyView, userPrivate) -> {
                user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
            },
            (lobbyView, throwable) -> {
                Log.d(LogTag.LOG_SEARCH, "Error when getting user Spotify data");
                throwable.printStackTrace();
            });

        restartableLatestCache(LOAD_PLAYLISTS_RESTARTABLE_ID,
            () -> spotifyRepository.getMyPlaylists(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
            (playlistSearchView, playlists) -> {
                playlistSearchView.updatePlaylists(playlists);
            },
            (playlistSearchView, throwable) -> {
                Log.d(LogTag.LOG_SEARCH, "Error when getting playlist data");
                throwable.printStackTrace();
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

    void requestSong(Song song) {
        tracklistRepository.checkSongInDbPlaylist(song, partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(songExistsDelivery -> songExistsDelivery.split(
                (songSearchView, songExists) -> {
                    if (songExists) {
                        songSearchView.showMessage("This song is already queued up in the tracklist");
                    }
                    else {
                        tracklistRepository.addSong(song, partyTitle).subscribe(addedWithSuccess -> {
                            if (addedWithSuccess) {
                                partiesRepository.incrementUserSongRequestCount(partyTitle, user);
                                songSearchView.finishWithSuccess("Song added to the tracklist!");
                            }
                            else {
                                songSearchView.showMessage("Something went wrong when adding the song, try again");
                            }
                        });
                    }
                },
                (songSearchView, throwable) -> {
                    Log.d(LogTag.LOG_SEARCH, "Something went wrong when informing the user of song addition status");
                }));
    }

    void loadPlaylistSongs(PlaylistSimple playlist) {
        Map<String, Object> searchOptions = new HashMap<>();
        searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.PLAYLIST_TRACK_SEARCH_QUERY_RESPONSE_LIMIT);
        searchOptions.put(SpotifyService.OFFSET, 0);

        findPlaylistTracksRecursively(playlist, searchOptions)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .concatMap(tracksPager -> Observable.fromArray(TrackMapper.playlistTracksToSongs(tracksPager, user)))
            .compose(this.deliverFirst())
            .subscribe(searchDelivery -> searchDelivery.split(
                (playlistSearchView, songs) -> {
                    if (songs.isEmpty()) playlistSearchView.showMessage("Playlist is empty");
                    playlistSearchView.updateSongs(songs);
                },
                (songSearchView, throwable) -> {
                }));
    }

    private Observable<List<PlaylistTrack>> findPlaylistTracksRecursively(PlaylistSimple playlist, Map<String, Object> searchOptions) {
        int lastOffset = (int) searchOptions.get(SpotifyService.OFFSET);
        return spotifyRepository.getPlaylistTracks(playlist.owner.id, playlist.id, searchOptions, spotifyCommunicatorService.getWebApi())
            .concatMap(playlistPager -> {
                if (lastOffset + playlistPager.limit >= SpotifyConstants.PLAYLIST_TRACKS_TOTAL_ITEMS_LIMIT) {
                    Log.d(LogTag.LOG_SEARCH, "values " + lastOffset + " " + playlistPager.limit);
                    return Observable.just(playlistPager.items);
                }
                else {
                    searchOptions.put(SpotifyService.OFFSET, lastOffset + playlistPager.limit);
                    return Observable.just(playlistPager.items)
                        .concatWith(findPlaylistTracksRecursively(playlist, searchOptions));
                }
            })
            .doOnError(throwable -> Log.d(LogTag.LOG_SEARCH, "Something went wrong on search: " + throwable.getMessage()));
    }

}