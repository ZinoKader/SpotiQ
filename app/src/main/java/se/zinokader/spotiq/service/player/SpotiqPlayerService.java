package se.zinokader.spotiq.service.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.andrewlord1990.snackbarbuilder.toastbuilder.ToastBuilder;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.PlayerInitializationException;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.ServiceConstants;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.util.NetworkUtil;
import se.zinokader.spotiq.util.NotificationUtil;
import se.zinokader.spotiq.util.ServiceUtil;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.exception.EmptyTracklistException;

public class SpotiqPlayerService extends Service implements ConnectionStateCallback, Player.NotificationCallback {

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    private CompositeDisposable disposableActions = new CompositeDisposable();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Config playerConfig;
    private SpotifyPlayer spotifyPlayer;
    private boolean isTracklistEmpty = false;
    private String partyTitle;

    private boolean changingConfiguration;

    private MediaSessionHandler mediaSessionHandler;
    private NotificationManager notificationManager;
    private static final int ONGOING_NOTIFICATION_ID = 821;

    private IBinder binder = new PlayerServiceBinder();

    public class PlayerServiceBinder extends Binder {
        public SpotiqPlayerService getService() {
            return SpotiqPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        changingConfiguration = false;
        sendPlayingStatusBroadcast(isPlaying());
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        changingConfiguration = false;
        sendPlayingStatusBroadcast(isPlaying());
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!changingConfiguration) {
            Log.d(LogTag.LOG_PLAYER_SERVICE, "Starting foreground player service");
            sendNotification(true);
        }
        return true;
    }

    @Override
    public void onCreate() {
        ((Injector) getApplicationContext()).inject(this);
        mediaSessionHandler = new MediaSessionHandler(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(LogTag.LOG_PLAYER_SERVICE, "SpotiQ Player service created");
    }

    @Override
    public void onDestroy() {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "SpotiQ Player service destroyed");
        mediaSessionHandler.releaseSessions();
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

    public void onTaskRemoved(Intent rootIntent) {
        //Called when application closes by user interaction
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changingConfiguration = true;
    }

    private void sendNotification(boolean shouldStartForeground) {
        String title;
        String description;
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.image_album_placeholder);
        boolean shouldBeOngoing = ServiceUtil.isPlayerServiceInForeground(this);

        if (isPlaying() && !isTracklistEmpty) {
            Metadata.Track currentTrack = spotifyPlayer.getMetadata().currentTrack;
            title = currentTrack.name;
            description = currentTrack.artistName + " - " + currentTrack.albumName;
            Glide.with(this)
                .load(currentTrack.albumCoverWebUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap albumArt, GlideAnimation<? super Bitmap> glideAnimation) {

                        Notification playerNotification;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mediaSessionHandler.setMetaData(NotificationUtil.buildMediaMetadata(currentTrack, albumArt));
                            playerNotification = NotificationUtil.buildPlayerNotification(SpotiqPlayerService.this, mediaSessionHandler.getMediaSession(),
                                shouldStartForeground, shouldBeOngoing, title, description, albumArt);
                        }
                        else {
                            mediaSessionHandler.setMetaData(NotificationUtil.buildMediaMetadataCompat(currentTrack, albumArt));
                            playerNotification = NotificationUtil.buildPlayerNotificationCompat(SpotiqPlayerService.this, mediaSessionHandler.getMediaSessionCompat(),
                                shouldBeOngoing, title, description, albumArt);
                        }

                        if (shouldStartForeground) {
                            startForeground(ONGOING_NOTIFICATION_ID, playerNotification);
                        }
                        else {
                            notificationManager.notify(ONGOING_NOTIFICATION_ID, playerNotification);
                        }
                    }
                });
        }
        else {
            title = "Hosting party " + partyTitle;
            description = "Player is idle";
            Notification playerNotification;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                playerNotification = NotificationUtil.buildPlayerNotification(SpotiqPlayerService.this, mediaSessionHandler.getMediaSession(),
                    shouldStartForeground, shouldBeOngoing, title, description, largeIcon);
            }
            else {
                playerNotification = NotificationUtil.buildPlayerNotificationCompat(SpotiqPlayerService.this, mediaSessionHandler.getMediaSessionCompat(),
                    shouldBeOngoing, title, description, largeIcon);
            }

            if (shouldStartForeground) {
                startForeground(ONGOING_NOTIFICATION_ID, playerNotification);
            }
            else {
                notificationManager.notify(ONGOING_NOTIFICATION_ID, playerNotification);
            }
        }
    }

    private void sendPlayingStatusBroadcast(boolean isPlaying) {
        Intent playingStatusIntent = new Intent(ServiceConstants.PLAYING_STATUS_BROADCAST_NAME);
        playingStatusIntent.putExtra(ServiceConstants.PLAYING_STATUS_ISPLAYING_EXTRA, isPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(playingStatusIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Player service initialization started");

        switch (intent.getAction()) {
            case ServiceConstants.ACTION_INIT:
                partyTitle = intent.getStringExtra(ApplicationConstants.PARTY_NAME_EXTRA);
                if (partyTitle.isEmpty()) {
                    Log.d(LogTag.LOG_PLAYER_SERVICE, "Player service could not be initialized - Received empty party title");
                    stopSelf();
                    break;
                }
                initPlayer().subscribe(didInit -> {
                    Log.d(LogTag.LOG_PLAYER_SERVICE, "Player initialization status: " + didInit);
                    if (!didInit) stopSelf();
                    registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                });
                break;
            case ServiceConstants.ACTION_PLAY_PAUSE:
                if (spotifyPlayer == null) {
                    Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify Player was null - stopping service");
                    stopSelf();
                    break;
                }
                if (isPlaying()) {
                    pause();
                }
                else {
                    play();
                }
        }

        return START_STICKY;
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
                            sendNotification(ServiceUtil.isPlayerServiceInForeground(SpotiqPlayerService.this));
                            sendPlayingStatusBroadcast(false); //make sure all listeners are up to sync with an inititally paused status
                            mediaSessionHandler.setSessionActive(true);
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

    public void play() {
        if (!NetworkUtil.isConnected(this)) {
            handleLostConnection();
            return;
        }
        if (!isTracklistEmpty && spotifyPlayer.getMetadata().currentTrack != null) {
            resume();
        }
        else {
            playNext();
        }
    }

    public void pause() {
        spotifyPlayer.pause(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(LogTag.LOG_PLAYER_SERVICE, "Paused music successfully");
            }

            @Override
            public void onError(Error error) {
                Log.d(LogTag.LOG_PLAYER_SERVICE, "Failed to pause music");
            }
        });
    }

    private void resume() {
        spotifyPlayer.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(LogTag.LOG_PLAYER_SERVICE, "Resumed music successfully");
            }

            @Override
            public void onError(Error error) {
                Log.d(LogTag.LOG_PLAYER_SERVICE, "Failed to resume music");
            }
        });
    }

    private void playNext() {
        tracklistRepository.getFirstSong(partyTitle)
            .subscribe(song -> {
                spotifyPlayer.playUri(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(LogTag.LOG_PLAYER_SERVICE, "Playing next song in tracklist");
                        isTracklistEmpty = false;
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d(LogTag.LOG_PLAYER_SERVICE, "Failed to play next song in tracklist: " + error.name());
                        sendPlayingStatusBroadcast(false);
                    }
                }, song.getSongUri(), 0, 0);
            }, throwable -> {
                Log.d(LogTag.LOG_PLAYER_SERVICE, "Could not play next song: " + throwable.getMessage());
                isTracklistEmpty = true;
                sendPlayingStatusBroadcast(false);
            });
    }

    private void handlePlaybackEnd() {
        tracklistRepository.removeFirstSong(partyTitle)
            .subscribe(wasRemoved -> {
                if (spotifyPlayer == null) return;
                if (wasRemoved) playNext();
            }, throwable -> {
                if (throwable instanceof EmptyTracklistException) {
                    Log.d(LogTag.LOG_PLAYER_SERVICE, "Tracklist empty, next song not played");
                    isTracklistEmpty = true;
                }
            });
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Playback event: " + playerEvent.name());
        switch (playerEvent) {
            case kSpPlaybackNotifyLostPermission:
                showLostPlaybackPermissionToast();
                break;
            case kSpPlaybackNotifyTrackDelivered:
                handlePlaybackEnd();
                break;
            case kSpPlaybackNotifyPlay:
                sendNotification(ServiceUtil.isPlayerServiceInForeground(SpotiqPlayerService.this));
                sendPlayingStatusBroadcast(true);
                mediaSessionHandler.setPlaybackStatePlaying();
                break;
            case kSpPlaybackNotifyPause:
                sendNotification(ServiceUtil.isPlayerServiceInForeground(SpotiqPlayerService.this));
                sendPlayingStatusBroadcast(false);
                mediaSessionHandler.setPlaybackStatePaused();
                break;
        }
    }

    public boolean isPlaying() {
        return !(spotifyPlayer == null || spotifyPlayer.getPlaybackState() == null) && spotifyPlayer.getPlaybackState().isPlaying;
    }

    private void showLostPlaybackPermissionToast() {
        mainThreadHandler.post(() -> {
            new ToastBuilder(this)
                .customView(LayoutInflater.from(this).inflate(getResources().getLayout(R.layout.toast_lost_permission_container), null))
                .duration(ApplicationConstants.LONG_TOAST_DURATION_SEC)
                .build()
                .show();
        });
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!NetworkUtil.isConnected(context)) {
                handleLostConnection();
            }
        }
    };

    private void handleLostConnection() {
        if (isPlaying()) pause();
        sendPlayingStatusBroadcast(false);
        mediaSessionHandler.setPlaybackStatePaused();
        mainThreadHandler.post(() -> {
            new ToastBuilder(getApplicationContext())
                .customView(LayoutInflater.from(getApplicationContext()).inflate(getResources().getLayout(R.layout.toast_lost_connection_container), null))
                .duration(ApplicationConstants.LONG_TOAST_DURATION_SEC)
                .build()
                .show();
        });
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
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify login failed: " + error.toString());
        switch (error) {
            case kSpErrorNeedsPremium:
                break;
        }
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify connection message: " + message);
    }

}
