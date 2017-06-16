package se.zinokader.spotiq.feature.party;

import android.support.annotation.NonNull;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import net.grandcentrix.thirtyinch.TiPresenter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.model.ChildEvent;
import se.zinokader.spotiq.model.PartyChangePublisher;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public class PartyPresenter extends TiPresenter<PartyView> implements ConnectionStateCallback, Player.NotificationCallback {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private boolean isPaused = false;
    private boolean wasPausedOnStartup = true;
    private List<Song> tracklist = new ArrayList<>();
    private static final PartyChangePublisher partyChangePublisher = new PartyChangePublisher();

    private LocalDateTime initializedTimeStamp;
    private SpotifyPlayer spotifyPlayer;
    private String partyTitle;

    @Override
    protected void onAttachView(@NonNull PartyView view) {
        super.onAttachView(view);
        if (!view.isPresenterAttached()) {
            view.setPresenter(this);
        }
    }

    void init() {
        initializedTimeStamp = LocalDateTime.now();
        sendToView(view -> view.delegateDataChanges(partyChangePublisher));
        loadPartyListener();
        loadTracklistListener();
        loadUser();
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    private boolean isLoadUpTimeUp() {
        return ChronoUnit.SECONDS.between(initializedTimeStamp, LocalDateTime.now()) >= ApplicationConstants.LOAD_UP_TIME_SEC;
    }

    private void loadPartyListener() {
        partiesRepository.listenToPartyMemberChanges(partyTitle)
            .delay(ApplicationConstants.DEFAULT_DELAY_MS, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(childEvent -> {
                User partyMember = childEvent.getDataSnapshot().getValue(User.class);
                switch (childEvent.getChangeType()) {
                    case ADDED:
                        partyChangePublisher.getNewPartyMemberPublisher().onNext(partyMember);
                        sendToView(view -> {
                            if (isLoadUpTimeUp()) view.showMessage(partyMember.getUserName() + " has joined the party");
                        });
                        break;
                    case CHANGED:
                        partyChangePublisher.getChangedPartyMemberPublisher().onNext(partyMember);
                        break;
                }
            });
    }

    private void loadTracklistListener() {
        tracklistRepository.listenToTracklistChanges(partyTitle)
            .delay(ApplicationConstants.DEFAULT_DELAY_MS, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(childEvent -> {
                if (childEvent.getChangeType().equals(ChildEvent.Type.ADDED)) {
                    Song song = childEvent.getDataSnapshot().getValue(Song.class);
                    tracklist.add(song);
                    partyChangePublisher.getNewTrackPublisher().onNext(song);
                    sendToView(view -> {
                        if (isLoadUpTimeUp()) view.showMessage(song.getName() + " queued by " + song.getAddedByUserName());
                    });
                }
            });
    }

    private void loadUser() {
        spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(userPrivate -> {
                User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                sendToView(view -> view.setUserDetails(user.getUserName(), user.getUserImageUrl()));
                loadHost(user.getUserId());
            });
    }

    private void loadHost(String userId) {
        partiesRepository.isHostOfParty(userId, partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(userIsHost -> {
                if (userIsHost) {
                    loadPlayer();
                    sendToView(PartyView::setHostPriviliges);
                }
            });
    }

    private void loadPlayer() {
        sendToView(view -> {
            Config playerConfig = view.setupPlayerConfig(spotifyCommunicatorService.getAuthenticator().getAccessToken());
            Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    spotifyPlayer = player;
                    spotifyPlayer.addConnectionStateCallback(PartyPresenter.this);
                    spotifyPlayer.addNotificationCallback(PartyPresenter.this);

                    spotifyPlayer.setPlaybackBitrate(new Player.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(LogTag.LOG_PARTY, "Set custom playback bitrate successfully");
                        }

                        @Override
                        public void onError(Error error) {
                            Log.d(LogTag.LOG_PARTY, "Failed to set playback bitrate. Cause: " + error.toString());

                        }
                    }, PlaybackBitrate.BITRATE_HIGH);

                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e(LogTag.LOG_PARTY, "Could not initialize player: " + throwable.getMessage());
                }

            });
        });
    }

    Observable<Boolean> play() {
        if (tracklist.isEmpty()) {
            sendToView(view -> view.showMessage("No songs in the tracklist, try adding some!"));
            return Observable.just(false);
        }
        else if (isPaused) {
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
        else {
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
        }
    }

    Observable<Boolean> pause() {
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
            //This gets called when the player initializes too. Checks if that was the case as well.
            //This SDK is so hacky
            case kSpPlaybackNotifyPause:
                if (!wasPausedOnStartup) {
                    isPaused = true;
                }
                else {
                    wasPausedOnStartup = false;
                }
                break;
            case kSpPlaybackNotifyPlay:
                isPaused = false;
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(LogTag.LOG_PARTY, "Spotify playback error: " + error.toString());
    }
}
