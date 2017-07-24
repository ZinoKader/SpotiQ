package se.zinokader.spotiq.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;

import com.spotify.sdk.android.player.Metadata;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.ServiceConstants;
import se.zinokader.spotiq.feature.party.PartyActivity;
import se.zinokader.spotiq.service.SpotiqPlayerService;

public class NotificationUtil {

    /**
     * Create notification channels required for displayal of notifications on >=API O
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannels(Context context) {
        NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = ApplicationConstants.MEDIA_NOTIFICATION_CHANNEL_ID;
        CharSequence userVisibleChannelName = context.getString(R.string.media_notification_channel_name);
        String userVisibleDescription = context.getString(R.string.media_notification_channel_description);

        int notificationImportance = NotificationManager.IMPORTANCE_DEFAULT;
        AudioAttributes audioAttributes =
            new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();

        NotificationChannel mediaNotificationChannel = new NotificationChannel(channelId, userVisibleChannelName, notificationImportance);
        mediaNotificationChannel.setDescription(userVisibleDescription);
        mediaNotificationChannel.enableVibration(false);
        mediaNotificationChannel.enableLights(false);
        mediaNotificationChannel.setSound(Uri.EMPTY, audioAttributes);

        mNotificationManager.createNotificationChannel(mediaNotificationChannel);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification buildPlayerNotification(Context context, MediaSession mediaSession,
                                                       boolean serviceIsForeground, boolean ongoing,
                                                       String title, String description, Bitmap largeIcon) {

        PendingIntent openPartyIntent = PendingIntent.getActivity(context, 0,
            new Intent(context, PartyActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseActionIntent = new Intent(context, SpotiqPlayerService.class);
        playPauseActionIntent.setAction(ServiceConstants.ACTION_PLAY_PAUSE);

        PendingIntent playPauseButtonIntent = serviceIsForeground
            ? PendingIntent.getForegroundService(context, 1,
            playPauseActionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            : PendingIntent.getService(context, 1,
            playPauseActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(context, ApplicationConstants.MEDIA_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setLargeIcon(largeIcon)
            .setActions(new Notification.Action(R.drawable.ic_notification_play_pause, "Play/Pause", playPauseButtonIntent))
            .setStyle(new Notification.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0))
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(description)
            .setContentIntent(openPartyIntent)
            .setOngoing(ongoing)
            .build();

    }

    public static Notification buildPlayerNotificationCompat(Context context, MediaSessionCompat mediaSessionCompat,
                                                             boolean ongoing, String title,
                                                             String description, Bitmap largeIcon) {

        PendingIntent openPartyIntent = PendingIntent.getActivity(context, 0,
            new Intent(context, PartyActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseActionIntent = new Intent(context, SpotiqPlayerService.class);
        playPauseActionIntent.setAction(ServiceConstants.ACTION_PLAY_PAUSE);

        PendingIntent playPauseIntent = PendingIntent.getService(context, 1,
            playPauseActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int largeIconWidth = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int largeIconHeight = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        largeIcon = Bitmap.createScaledBitmap(largeIcon, largeIconWidth, largeIconHeight, false);

        return new NotificationCompat.Builder(context, ApplicationConstants.MEDIA_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setLargeIcon(largeIcon)
            .addAction(new NotificationCompat.Action(R.drawable.ic_notification_play_pause, "Play/Pause", playPauseIntent))
            .setStyle(new MediaStyle()
                .setMediaSession(mediaSessionCompat.getSessionToken())
                .setShowActionsInCompactView(0))
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(description)
            .setContentIntent(openPartyIntent)
            .setOngoing(ongoing)
            .setDefaults(4)
            .build();
    }

    /**
     * Create meta data for notifications that allow the system to send track information to
     * bluetooth systems, show artwork in lock screens among other uses
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static MediaMetadata buildMediaMetadata(Metadata.Track trackMetadata, Bitmap albumArt) {
        return new MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, trackMetadata.name) //song title for display
            .putString(MediaMetadata.METADATA_KEY_TITLE, trackMetadata.name) //song title for info
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, trackMetadata.artistName) //artist name for display
            .putString(MediaMetadata.METADATA_KEY_ARTIST, trackMetadata.artistName) //artist name for info
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, trackMetadata.albumName) //album name for display
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt) //album art for display
            .build();
    }

    public static MediaMetadataCompat buildMediaMetadataCompat(Metadata.Track trackMetadata, Bitmap albumArt) {
        return new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, trackMetadata.name) //song title for display
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, trackMetadata.name) //song title for info
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, trackMetadata.artistName) //artist name for display
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, trackMetadata.artistName) //artist name for info
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, trackMetadata.albumName) //album name for display
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt) //album art for display
            .build();
    }

}
