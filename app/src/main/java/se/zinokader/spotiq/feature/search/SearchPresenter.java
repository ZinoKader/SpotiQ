package se.zinokader.spotiq.feature.search;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;

public class SearchPresenter extends BasePresenter<SearchView> {

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private String partyTitle;
    private ArrayList<Song> songRequests;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (savedState != null) {
            partyTitle = savedState.getString(ApplicationConstants.PARTY_NAME_EXTRA);
            songRequests = savedState.getParcelableArrayList(ApplicationConstants.SONG_REQUESTS_EXTRA);
        }
        else {
            songRequests = new ArrayList<>();
        }
    }

    @Override
    public void takeView(SearchView searchView) {
        super.takeView(searchView);
        searchView.updateRequestList(songRequests);
        searchView.updateSongRequestsLabel();
    }

    @Override
    protected void onSave(@NonNull Bundle state) {
        super.onSave(state);
        state.putString(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        state.putParcelableArrayList(ApplicationConstants.SONG_REQUESTS_EXTRA, songRequests);
    }

    void addRequest(Song song) {
        for (Song requestedSong : songRequests) {
            if (requestedSong.getSongSpotifyId().equals(song.getSongSpotifyId())) {
                if (getView() != null) getView().showMessage("\"" + song.getName() + "\"" + " already selected");
                return;
            }
        }
        songRequests.add(song);
        if (getView() != null) getView().updateRequestList(songRequests);
        if (getView() != null) getView().updateSongRequestsLabel();
    }

    void removeRequest(Song song) {
        songRequests.remove(song);
        if (getView() != null) getView().updateRequestList(songRequests);
        if (getView() != null) getView().updateSongRequestsLabel();
    }

    void queueRequestedSongs() {
        Observable.fromIterable(songRequests)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .concatMap(song -> tracklistRepository.checkSongInDbPlaylist(song, partyTitle))
            .filter(songBooleanPair -> {
                Boolean songExistsInDb = songBooleanPair.second;
                return !songExistsInDb;
            })
            .concatMapEager(songBooleanPair -> tracklistRepository.addSong(songBooleanPair.first, partyTitle))
            .subscribe(isSongAdded -> {
                if (isSongAdded) {
                    spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                        .subscribeOn(Schedulers.io())
                        .subscribe(userPrivate -> partiesRepository.incrementUserSongRequestCount(partyTitle, userPrivate.id));
                }
            });

        if (getView() != null) getView().finishRequest();
    }


    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

}
