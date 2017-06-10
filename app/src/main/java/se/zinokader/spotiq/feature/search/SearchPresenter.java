package se.zinokader.spotiq.feature.search;

import android.support.annotation.NonNull;
import android.util.Log;

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
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
                .concatMap(tracksPager -> Observable.fromArray(tracksPager.tracks))
                .subscribe(new Observer<Pager<Track>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Pager<Track> trackPager) {
                        collectedTracks.addAll(trackPager.items);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        List<Song> songs = TrackMapper.tracksToSongs(collectedTracks, "zinne97");
                        Log.d("pls work ", "pls " + songs.size());
                        for (Song song: songs) {
                            Log.d("SONG :", song.getName());
                        }
                    }
                });

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
