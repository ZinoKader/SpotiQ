package se.zinokader.spotiq.feature.search;

import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.feature.search.preview.PreviewPlayer;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.util.mapper.TrackMapper;

public class SearchPresenter extends BasePresenter<SearchView> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private PreviewPlayer songPreviewPlayer;
    private String partyTitle;
    private User user;

    private static final int LOAD_USER_RESTARTABLE_ID = 764;

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

        if (savedState == null) {
            start(LOAD_USER_RESTARTABLE_ID);
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
                (searchView, songExists) -> {
                    if (songExists) {
                        searchView.showMessage("This song is already queued up in the tracklist");
                    }
                    else {
                        tracklistRepository.addSong(song, partyTitle).subscribe(addedWithSuccess -> {
                            if (addedWithSuccess) {
                                partiesRepository.incrementUserSongRequestCount(user, partyTitle);
                                searchView.finishWithSuccess("Song added to the tracklist!");
                            }
                            else {
                                searchView.showMessage("Something went wrong when adding the song, try again");
                            }
                        });
                    }
                },
                (searchView, throwable) -> {
                    Log.d(LogTag.LOG_SEARCH, "Something went wrong when informing the user of song addition status");
                }));
    }

    void searchTracks(String query) {

        Map<String, Object> searchOptions = new HashMap<>();
        searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.SEARCH_QUERY_RESPONSE_LIMIT);
        searchOptions.put(SpotifyService.OFFSET, 0);

        searchTracksRecursively(query, searchOptions)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .concatMap(tracksPager -> Observable.fromArray(TrackMapper.tracksToSongs(tracksPager.tracks.items, user)))
            .compose(this.deliverFirst())
            .subscribe(searchDelivery -> searchDelivery.split(
                (searchView, songs) -> {
                    if (songs.isEmpty()) searchView.showMessage("No songs were found for the search query " + query);
                    searchView.updateSearch(songs, false);
                },
                (searchView, throwable) -> {
                }));
    }

    private Observable<TracksPager> searchTracksRecursively(String query, Map<String, Object> searchOptions) {

        int lastOffset = (int) searchOptions.get(SpotifyService.OFFSET);

        return spotifyRepository.searchTracks(query, searchOptions, spotifyCommunicatorService.getWebApi())
            .concatMap(tracksPager -> {
                if (lastOffset + tracksPager.tracks.limit >= SpotifyConstants.TOTAL_ITEMS_LIMIT) {
                    return Observable.just(tracksPager);
                }
                searchOptions.put(SpotifyService.OFFSET, lastOffset + tracksPager.tracks.limit);
                return Observable.just(tracksPager)
                    .concatWith(searchTracksRecursively(query, searchOptions));
            });
    }

}
