package se.zinokader.spotiq;

import android.app.Application;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;


public class SpotiqApplication extends Application {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder().appModule(new AppModule()).build();
        RxPaparazzo.register(this);
    }

    public AppComponent getComponent() {
        return component;
    }
}
