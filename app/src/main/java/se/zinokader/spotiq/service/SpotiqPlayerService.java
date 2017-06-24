package se.zinokader.spotiq.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.PlayerInitializationException;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.ServiceConstants;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.util.di.Injector;

public class SpotiqPlayerService extends Service implements ConnectionStateCallback, Player.NotificationCallback {

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    private CompositeDisposable disposableActions = new CompositeDisposable();

    private Config playerConfig;
    private SpotifyPlayer spotifyPlayer;
    private String partyTitle;

    private IBinder binder = new PlayerServiceBinder();

    public class PlayerServiceBinder extends Binder {
        public SpotiqPlayerService getService() {
            return SpotiqPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        ((Injector) getApplicationContext()).inject(this);
    }

    @Override
    public void onDestroy() {
        disposableActions.clear();
        if (spotifyPlayer != null) {
            try {
                Spotify.awaitDestroyPlayer(this, 5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Player service initialization started");

        partyTitle = intent.getStringExtra(ApplicationConstants.PARTY_NAME_EXTRA);
        if (partyTitle.isEmpty()) {
            Log.d(LogTag.LOG_PLAYER_SERVICE, "Player service could not be initialized - Received empty party title");
            stopSelf();
        }

        if (intent.getAction().equals(ServiceConstants.ACTION_INIT)) {
            boolean didInit = initPlayer().blockingGet();
            if (!didInit) stopSelf();
        }

        return START_NOT_STICKY;
    }

    private Single<Boolean> initPlayer() {
        return Single.create(subscriber -> {
            if (playerConfig == null) {
                playerConfig = new Config(this,
                    spotifyCommunicatorService.getAuthenticator().getAccessToken(),
                    SpotifyConstants.CLIENT_ID);
                playerConfig.useCache(false);
            }
            spotifyPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    spotifyPlayer.addConnectionStateCallback(SpotiqPlayerService.this);
                    spotifyPlayer.addNotificationCallback(SpotiqPlayerService.this);
                    spotifyPlayer.setPlaybackBitrate(new Player.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            subscriber.onSuccess(true);
                            Log.d(LogTag.LOG_PLAYER_SERVICE, "Set Spotify Player custom playback bitrate successfully");
                        }

                        @Override
                        public void onError(Error error) {
                            subscriber.onError(new PlayerInitializationException(error.name()));
                            Log.d(LogTag.LOG_PLAYER_SERVICE, "Failed to set Spotify Player playback bitrate. Cause: " + error.toString());

                        }
                    }, PlaybackBitrate.BITRATE_HIGH);

                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                    Log.d(LogTag.LOG_PLAYER_SERVICE, "Could not initialize Spotify Player: " + throwable.getMessage());
                    stopSelf();
                }

            });
        });
    }

    private void handlePlayPause() {
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
    }

    public boolean isPlaying() {
        return spotifyPlayer != null && spotifyPlayer.getPlaybackState().isPlaying;
    }

    public void play() {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "PLAY CLICKED!");
    }

    public void pause() {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "PAUSE CLICKED!");
    }

    private void showLostPlaybackPermissionToast() {
        MDToast lostPlaybackPermissionToast =
            MDToast.makeText(this,
                getString(R.string.playback_permission_lost_notice),
                MDToast.LENGTH_LONG,
                MDToast.TYPE_INFO);
        lostPlaybackPermissionToast.setIcon(R.drawable.ic_spotify_connect);
        lostPlaybackPermissionToast.show();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        switch (playerEvent) {
            case kSpPlaybackNotifyLostPermission:
                showLostPlaybackPermissionToast();
            case kSpPlaybackNotifyTrackDelivered:
                tracklistRepository.removeFirstSong(partyTitle)
                    .delay(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(wasRemoved -> {
                        // musicAction();
                    });
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify playback error: " + error.toString());
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {
        stopSelf();
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify login failed: " + error.toString());
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify connection message: " + message);
    }
}
