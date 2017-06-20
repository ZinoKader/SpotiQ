package se.zinokader.spotiq.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.threetenabp.AndroidThreeTen;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.util.di.ComponentInjector;
import se.zinokader.spotiq.util.di.Injector;

public class SpotiqApplication extends Application implements Injector {

    private static AppComponent appComponent;
    private ComponentInjector<AppComponent> injector;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        AndroidThreeTen.init(this); //java 8 time api backport

        appComponent = DaggerAppComponent.create();
        injector = new ComponentInjector<>(AppComponent.class, appComponent);

        //TODO: Change 10k to 26 once they change this...
        if (Build.VERSION.SDK_INT >= 10000) {
            createNotificationChannels();
        }
    }

    /**
     * Allows abstracting injection to any object
     * @param target target object to be injected
     */
    @Override
    public void inject(Object target) {
        injector.inject(target);
    }


    /**
     * Create notification channels required for display notifications on >=API O
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = ApplicationConstants.MEDIA_NOTIFICATION_CHANNEL_ID;
        CharSequence userVisibleChannelName = getString(R.string.media_notification_channel_name);
        String userVisibleDescription = getString(R.string.media_notification_channel_description);

        int notificationImportance = NotificationManager.IMPORTANCE_MAX;

        NotificationChannel mChannel = new NotificationChannel(channelId, userVisibleChannelName, notificationImportance);
        mChannel.setDescription(userVisibleDescription);

        mNotificationManager.createNotificationChannel(mChannel);
    }
}
