package se.zinokader.spotiq.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;

public class NotificationChannelUtil {

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

        int notificationImportance = NotificationManager.IMPORTANCE_MAX;

        NotificationChannel mChannel = new NotificationChannel(channelId, userVisibleChannelName, notificationImportance);
        mChannel.setDescription(userVisibleDescription);

        mNotificationManager.createNotificationChannel(mChannel);
    }

}
