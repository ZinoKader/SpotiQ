package se.zinokader.spotiq.service.player;

import android.app.Notification;
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
import se.zinokader.spotiq.model.ChildEvent;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.util.NetworkUtil;
import se.zinokader.spotiq.util.NotificationUtil;
import se.zinokader.spotiq.util.PreferenceUtil;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.exception.EmptyTracklistException;

public class SpotiqHostService extends Service implements ConnectionStateCallback, Player.NotificationCallback {

    @Inject
    TracklistRepository tracklistRepository;

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    private CompositeDisposable subscriptions = new CompositeDisposable();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Config playerConfig;
    private SpotifyPlayer spotifyPlayer;
    private boolean isTracklistEmpty = true;
    private String partyTitle;

    private boolean inForeground;
    private boolean changingConfiguration;

    private MediaSessionHandler mediaSessionHandler;
    private static final int ONGOING_NOTIFICATION_ID = 821;

    private IBinder binder = new PlayerServiceBinder();

    public class PlayerServiceBinder extends Binder {
        public SpotiqHostService getService() {
            return SpotiqHostService.this;
        }
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!NetworkUtil.isConnected(context)) {
                handleLostConnection();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        changingConfiguration = false;
        inForeground = false;
        sendPlayingStatusBroadcast(isPlaying());
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        changingConfiguration = false;
        inForeground = false;
        sendPlayingStatusBroadcast(isPlaying());
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!changingConfiguration) {
            Log.d(LogTag.LOG_PLAYER_SERVICE, "Starting player service in foreground");
            inForeground = true;
            updatePlayerNotification();
        }
        return true;
    }

    @Override
    public void onCreate() {
        ((Injector) getApplicationContext()).inject(this);
        mediaSessionHandler = new MediaSessionHandler(this);
        Log.d(LogTag.LOG_PLAYER_SERVICE, "SpotiQ Player service created");
    }

    @Override
    public void onDestroy() {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "SpotiQ Player service destroyed");
        stopForeground(true);
        mediaSessionHandler.releaseSessions();
        subscriptions.clear();
        if (connectionReceiver != null) {
            unregisterReceiver(connectionReceiver);
        }
        if (spotifyPlayer != null) {
            try {
                Spotify.awaitDestroyPlayer(SpotiqHostService.this, 5, TimeUnit.SECONDS);
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
                    if (!didInit) {
                        stopSelf();
                        return;
                    }
                    registerReceiver(connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                });
                setupServiceSettings();
                break;
            case ServiceConstants.ACTION_PLAY_PAUSE:
                if (spotifyPlayer == null) {
                    Log.d(LogTag.LOG_PLAYER_SERVICE, "SpotifyPlayer was null - stopping service");
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

    private void updatePlayerNotification() {
        if (!inForeground) return;
        String title;
        String description;
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.image_album_placeholder);

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
                            playerNotification = NotificationUtil.buildPlayerNotification(SpotiqHostService.this,
                                mediaSessionHandler.getMediaSession(), title, description, albumArt);
                        } else {
                            mediaSessionHandler.setMetaData(NotificationUtil.buildMediaMetadataCompat(currentTrack, albumArt));
                            playerNotification = NotificationUtil.buildPlayerNotificationCompat(SpotiqHostService.this,
                                mediaSessionHandler.getMediaSessionCompat(), title, description, albumArt);
                        }
                        startForeground(ONGOING_NOTIFICATION_ID, playerNotification);
                    }
                });
        } else {
            title = "Hosting party " + partyTitle;
            description = "Player is idle";
            Notification playerNotification;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                playerNotification = NotificationUtil.buildPlayerNotification(SpotiqHostService.this,
                    mediaSessionHandler.getMediaSession(), title, description, largeIcon);
            } else {
                playerNotification = NotificationUtil.buildPlayerNotificationCompat(SpotiqHostService.this,
                    mediaSessionHandler.getMediaSessionCompat(), title, description, largeIcon);
            }
            startForeground(ONGOING_NOTIFICATION_ID, playerNotification);
        }
    }

    private void sendPlayingStatusBroadcast(boolean isPlaying) {
        Intent playingStatusIntent = new Intent(ServiceConstants.PLAYING_STATUS_BROADCAST_NAME);
        playingStatusIntent.putExtra(ServiceConstants.PLAYING_STATUS_ISPLAYING_EXTRA, isPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(playingStatusIntent);
    }

    private Single<Boolean> initPlayer() {
        return Single.create(subscriber -> {

            playerConfig = new Config(SpotiqHostService.this,
                spotifyCommunicatorService.getAuthenticator().getAccessToken(),
                SpotifyConstants.CLIENT_ID);
            playerConfig.useCache(false);

            spotifyPlayer = Spotify.getPlayer(playerConfig, SpotiqHostService.this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    spotifyPlayer.addConnectionStateCallback(SpotiqHostService.this);
                    spotifyPlayer.addNotificationCallback(SpotiqHostService.this);
                    spotifyPlayer.setPlaybackBitrate(new Player.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            subscriber.onSuccess(true);
                            Log.d(LogTag.LOG_PLAYER_SERVICE, "Set Spotify Player custom playback bitrate successfully");
                            sendPlayingStatusBroadcast(false); //make sure all listeners are up to sync with an inititally paused status
                            mediaSessionHandler.setSessionActive();
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

    public boolean isPlaying() {
        return !(spotifyPlayer == null || spotifyPlayer.getPlaybackState() == null) && spotifyPlayer.getPlaybackState().isPlaying;
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
                sendPlayingStatusBroadcast(false);
                if (throwable instanceof EmptyTracklistException) {
                    isTracklistEmpty = true;
                }
            });
    }

    private void setupServiceSettings() {

        //synchronize tracklist status
        tracklistRepository.getFirstSong(partyTitle)
            .subscribe((song, throwable) -> {
                if (!(throwable instanceof EmptyTracklistException)) {
                    isTracklistEmpty = false;
                }
            });

        //setup auto playing the first track
        if (PreferenceUtil.isAutoPlayEnabled(this)) {
            subscriptions.add(tracklistRepository.listenToTracklistChanges(partyTitle)
                .subscribe(childEvent -> {
                    if (childEvent.getChangeType().equals(ChildEvent.Type.ADDED) && isTracklistEmpty) {
                        play();
                    }
                }));
        }

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

    private void handlePlaybackError() {
        sendPlayingStatusBroadcast(false);
        showPlaybackFailedToast();
        handlePlaybackEnd();
    }

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
                updatePlayerNotification();
                sendPlayingStatusBroadcast(true);
                mediaSessionHandler.setPlaybackStatePlaying();
                break;
            case kSpPlaybackNotifyPause:
                updatePlayerNotification();
                sendPlayingStatusBroadcast(false);
                mediaSessionHandler.setPlaybackStatePaused();
                break;
        }
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

    private void showPlaybackFailedToast() {
        mainThreadHandler.post(() -> {
            new ToastBuilder(this)
                .customView(LayoutInflater.from(this).inflate(getResources().getLayout(R.layout.toast_playback_failed_container), null))
                .duration(ApplicationConstants.LONG_TOAST_DURATION_SEC)
                .build()
                .show();
        });
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify playback error: " + error.toString());
        handlePlaybackError();
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
        /*
        switch (error) {
            case kSpErrorNeedsPremium:
                break;
        }
        */
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(LogTag.LOG_PLAYER_SERVICE, "Spotify connection message: " + message);
    }

}
