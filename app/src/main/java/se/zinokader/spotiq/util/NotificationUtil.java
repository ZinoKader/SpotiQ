package se.zinokader.spotiq.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

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

}
