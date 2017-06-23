package se.zinokader.spotiq.feature.party;

import android.os.Bundle;
import android.util.Log;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public class PartyPresenter extends BasePresenter<PartyView> implements ConnectionStateCallback, Player.NotificationCallback {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private SpotifyPlayer spotifyPlayer;
    private String partyTitle;

    private static final int LOAD_USER_RESTARTABLE_ID = 9814;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        //listen to user changes
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.NETWORK_RETRY_DELAY_SEC, TimeUnit.SECONDS)),
            (partyView, userPrivate) -> {
                User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                partyView.setUserDetails(user.getUserName(), user.getUserImageUrl());
                loadHost(user.getUserId());
            });

        if (savedState == null) {
            start(LOAD_USER_RESTARTABLE_ID);
        }

    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    private void loadHost(String userId) {
        add(partiesRepository.isHostOfParty(userId, partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .retryWhen(throwable -> throwable.delay(ApplicationConstants.NETWORK_RETRY_DELAY_SEC, TimeUnit.SECONDS))
            .subscribe(resultDelivery -> resultDelivery.split((partyView, userIsHost) -> {
                if (userIsHost) {
                    //loadPlayer();
                    partyView.setHostPriviliges();
                    partyView.showMessage("Connected as a party host");
                }
            }, (partyView, throwable) -> {
                partyView.showMessage("Could not load host priviliges, retrying...");
            })));
    }


    Observable<Boolean> musicAction() {
        /*
        if (tracklist.isEmpty()) {
            restartableFirst(EMPTY_TRACKLIST_MESSAGE_RESTARTABLE_ID,
                () -> Observable.just(new Empty()),
                (partyView, empty) -> partyView.showMessage("No songs in the tracklist, try adding some!"));
            return Observable.just(false);
        }
        else if (spotifyPlayer.getPlaybackState().isPlaying) {
            return pause();
        }
        else if(spotifyPlayer.getMetadata().currentTrack != null) {
            return resume();
        }
        else {
            return play();
        }
        */
        return Observable.just(true);
    }

    private Observable<Boolean> play() {
        /*
        return Observable.create(subscriber -> spotifyPlayer.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                subscriber.onNext(true);
                subscriber.onComplete();
            }

            @Override
            public void onError(Error error) {
                subscriber.onNext(false);
                subscriber.onComplete();
            }
        }, tracklist.get(0).getSongUri(), 0, 0));
        */
        return Observable.just(true);
    }

    private Observable<Boolean> resume() {
        return Observable.create(subscriber -> spotifyPlayer.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                subscriber.onNext(true);
                subscriber.onComplete();
            }

            @Override
            public void onError(Error error) {
                subscriber.onNext(false);
                subscriber.onComplete();
            }
        }));
    }

    private Observable<Boolean> pause() {
        return Observable.create(subscriber -> spotifyPlayer.pause(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                subscriber.onNext(true);
                subscriber.onComplete();
            }

            @Override
            public void onError(Error error) {
                subscriber.onNext(false);
                subscriber.onComplete();
            }
        }));
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(LogTag.LOG_PARTY, "Spotify login failed: " + error.toString());
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(LogTag.LOG_PARTY, "Spotify connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(LogTag.LOG_PARTY, "Spotify playback event " + playerEvent.name());

        switch (playerEvent) {
            case kSpPlaybackNotifyTrackDelivered:
                tracklistRepository.removeFirstSong(partyTitle)
                    .delay(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(wasRemoved -> {
                        musicAction();
                    });
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(LogTag.LOG_PARTY, "Spotify playback error: " + error.toString());
    }
}
