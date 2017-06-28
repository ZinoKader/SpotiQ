package se.zinokader.spotiq.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;

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


    public static Notification createPlayerNotification(Context context, boolean ongoing, String title, String description, Bitmap largeIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(context, ApplicationConstants.MEDIA_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setLargeIcon(largeIcon)
                .setStyle(new Notification.MediaStyle()
                    .setMediaSession(new MediaSession(context, ApplicationConstants.MEDIA_SESSSION_TAG).getSessionToken()))
                .setColorized(true)
                .setContentTitle(title)
                .setContentText(description)
                .setOngoing(ongoing)
                .build();
        }
        else {
            return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.MediaStyle()
                    .setMediaSession(new MediaSessionCompat(context, ApplicationConstants.MEDIA_SESSSION_TAG).getSessionToken()))
                .setColorized(true)
                .setContentTitle(title)
                .setContentText(description)
                .setOngoing(ongoing)
                .setChannel(ApplicationConstants.MEDIA_NOTIFICATION_CHANNEL_ID)
                .setDefaults(4)
                .build();
        }
    }

}
