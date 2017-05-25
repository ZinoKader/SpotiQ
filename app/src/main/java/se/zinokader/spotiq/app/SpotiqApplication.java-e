package se.zinokader.spotiq.app;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.jakewharton.threetenabp.AndroidThreeTen;

import se.zinokader.spotiq.service.JobMapper;
import se.zinokader.spotiq.util.ComponentInjector;
import se.zinokader.spotiq.util.Injector;

public class SpotiqApplication extends Application implements Injector {

    private ComponentInjector<AppComponent> injector;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this); //java 8 time api backport
        JobManager.create(this).addJobCreator(new JobMapper());

        AppComponent appComponent = DaggerAppComponent.create();
        injector = new ComponentInjector<>(AppComponent.class, appComponent);
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
