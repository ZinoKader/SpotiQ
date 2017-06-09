package se.zinokader.spotiq.feature.search;

import android.support.annotation.NonNull;
import android.util.Log;

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.util.mapper.TrackMapper;

public class SearchPresenter extends TiPresenter<SearchView> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private String spotifyId;

    @Override
    protected void onAttachView(@NonNull SearchView view) {
        super.onAttachView(view);
        if (!view.isPresenterAttached()) {
            view.setPresenter(this);
        }
    }

    void init() {
    }

    void searchTracks(String query) {

        Map<String, Object> searchOptions = new HashMap<>();
        searchOptions.put(SpotifyService.LIMIT, SpotifyConstants.SEARCH_QUERY_RESPONSE_LIMIT);
        searchOptions.put(SpotifyService.OFFSET, 0);

        List<Track> collectedTracks = new ArrayList<>();

        searchTracksRecursively(query, searchOptions)
                .debounce(600, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Track>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Track> tracks) {
                        collectedTracks.addAll(tracks);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        List<Song> songs = TrackMapper.tracksToSongs(collectedTracks, "hey");
                        Log.d("DAMN YO!", "CHECK THAT SIZE: " + songs.size());
                    }
                });

    }

    private Observable<List<Track>> searchTracksRecursively(String query, Map<String, Object> searchOptions) {

        int lastOffset = (int) searchOptions.get(SpotifyService.OFFSET);
        Log.d("OFFSET", "OFFSET IS AT " + lastOffset);

        return spotifyRepository.searchTracks(query, searchOptions, spotifyCommunicatorService.getWebApi())
                .map(tracksPager -> tracksPager.tracks)
                .flatMap(tracks -> {
                    if (tracks.next != null || lastOffset + tracks.limit <= SpotifyConstants.TOTAL_ITEMS_LIMIT) {
                        searchOptions.put(SpotifyService.OFFSET, lastOffset + tracks.limit);
                        return Observable.just(tracks.items)
                                .concatWith(searchTracksRecursively(query, searchOptions));
                    }
                    else {
                        return Observable.just(tracks.items);
                    }
                });
    }

}
