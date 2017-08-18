package se.zinokader.spotiq.feature.search.songsearch;

import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.feature.search.preview.PreviewPlayer;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.util.mapper.TrackMapper;

public class SongSearchPresenter extends BasePresenter<SongSearchView> {

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private PreviewPlayer songPreviewPlayer;
    private String partyTitle;
    private User user;

    static final int LOAD_USER_RESTARTABLE_ID = 764;

    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        songPreviewPlayer = new PreviewPlayer();

        //load user data and user search suggestions
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS)),
            (songSearchView, userPrivate) -> {
                user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                //load personalized search suggestions
                Map<String, Object> searchOptions = new HashMap<>();
                searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.TOP_TRACKS_QUERY_RESPONSE_LIMIT);
                searchOptions.put(SpotifyService.TIME_RANGE, SpotifyConstants.TIME_RANGE_SHORT);
                spotifyRepository.getMyTopTracks(searchOptions, spotifyCommunicatorService.getWebApi())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS))
                    .subscribe(tracks -> {
                        List<Song> songSuggestions = TrackMapper.tracksToSongs(tracks, user);
                        SongSearchSuggestionsBuilder suggestionsBuilder =
                            new SongSearchSuggestionsBuilder(songSuggestions, ApplicationConstants.MAX_SONG_SUGGESTIONS);
                        songSearchView.updateSearchSuggestions(suggestionsBuilder);
                    });
            },
            (songSearchView, throwable) -> {
                Log.d(LogTag.LOG_SEARCH, "Error when getting user Spotify data");
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

    void searchTracks(String query) {
        Map<String, Object> searchOptions = new HashMap<>();
        searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.TRACK_SEARCH_QUERY_RESPONSE_LIMIT);
        searchOptions.put(SpotifyService.OFFSET, 0);

        partiesRepository.getParty(partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(partySnapshot -> {
                Party dbParty = partySnapshot.child(FirebaseConstants.CHILD_PARTYINFO).getValue(Party.class);
                searchOptions.put(SpotifyService.MARKET, dbParty.getHostMarket());

                searchTracksRecursively(query, searchOptions)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(tracksPager -> TrackMapper.tracksToSongs(tracksPager.tracks.items, user))
                    .subscribe(songs -> {
                        if (getView() != null) {
                            if (songs.isEmpty()) getView().showMessage("No songs were found for the search query " + query);
                            getView().updateSearch(songs);
                        }
                    }, throwable -> {
                        Log.d(LogTag.LOG_SEARCH, "Something went wrong on searching for tracks");
                        throwable.printStackTrace();
                    });
            });
    }

    private Observable<TracksPager> searchTracksRecursively(String query, Map<String, Object> searchOptions) {
        int lastOffset = (int) searchOptions.get(SpotifyService.OFFSET);
        return spotifyRepository.searchTracks(query, searchOptions, spotifyCommunicatorService.getWebApi())
            .concatMap(tracksPager -> {
                if (lastOffset + tracksPager.tracks.limit >= SpotifyConstants.TRACK_SEARCH_TOTAL_ITEMS_LIMIT
                    || lastOffset + tracksPager.tracks.limit >= tracksPager.tracks.total) {
                    return Observable.just(tracksPager);
                }
                else {
                    searchOptions.put(SpotifyService.OFFSET, lastOffset + tracksPager.tracks.limit);
                    return Observable.just(tracksPager).concatWith(searchTracksRecursively(query, searchOptions));
                }
            })
            .doOnError(throwable -> Log.d(LogTag.LOG_SEARCH, "Something went wrong on tracks search recursion: " + throwable.getMessage()));
    }

}
