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

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.model.ChildEvent;
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
                        sendToView(view -> {
                            view.addPartyMember(partyMember);
                            if (isLoadUpTimeUp()) view.showMessage(partyMember.getUserName() + " has joined the party");
                        });
                        break;
                    case CHANGED:
                        sendToView(view -> view.changePartyMember(partyMember));
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
                    sendToView(view -> {
                        view.addSong(song);
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
                    sendToView(PartyView::setHostPriviliges);
                    loadPlayer();
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

    @Override
    public void onLoggedIn() {
        /*
        spotifyPlayer.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Error error) {

            }
        }, "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp", 0, 0);
        */
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
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(LogTag.LOG_PARTY, "Spotify playback error: " + error.toString());
    }
}
