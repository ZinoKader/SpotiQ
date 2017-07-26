package se.zinokader.spotiq.application;

import android.app.Application;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.threetenabp.AndroidThreeTen;

import io.fabric.sdk.android.Fabric;
import se.zinokader.spotiq.util.NotificationUtil;
import se.zinokader.spotiq.util.ShortcutUtil;
import se.zinokader.spotiq.util.di.ComponentInjector;
import se.zinokader.spotiq.util.di.Injector;

public class SpotiqApplication extends Application implements Injector {

    private static AppComponent appComponent;
    private ComponentInjector<AppComponent> injector;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        AndroidThreeTen.init(this); //java 8 time api backport

        appComponent = DaggerAppComponent.create();
        injector = new ComponentInjector<>(AppComponent.class, appComponent);

        //register notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createNotificationChannels(this);
        }

        //remove shortcuts on app restart
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.removeAllShortcuts(this);
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

}
