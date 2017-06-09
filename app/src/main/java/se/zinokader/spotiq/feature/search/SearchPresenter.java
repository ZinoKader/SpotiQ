package se.zinokader.spotiq.feature.search;

import android.support.annotation.NonNull;
import android.util.Log;

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

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

        searchTracksRecursively(query, searchOptions, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Track>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        
                    }

                    @Override
                    public void onNext(List<Track> trackList) {
                        Log.d("IS IT WORKING?", "LET THE SIZE SPEAK !" + trackList.size());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private Observable<List<Track>> searchTracksRecursively(String query, Map<String, Object> searchOptions, int offset) {

        searchOptions.put(SpotifyService.OFFSET, offset);

        return spotifyRepository.searchTracks(query, searchOptions, spotifyCommunicatorService.getWebApi())
                .map(tracksPager -> tracksPager.tracks)
                .flatMap(tracks -> {
                    if (tracks.offset + tracks.limit < tracks.total) {
                        searchOptions.put(SpotifyService.OFFSET, tracks.limit);
                        return Observable.just(tracks.items)
                                .concatWith(searchTracksRecursively(query, searchOptions, tracks.limit));
                    }
                    else {
                        return Observable.just(tracks.items);
                    }
                });
    }

}
