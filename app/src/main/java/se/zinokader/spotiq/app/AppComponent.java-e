package se.zinokader.spotiq.app;

import javax.inject.Singleton;

import dagger.Component;
import se.zinokader.spotiq.ui.login.AuthenticationActivity;
import se.zinokader.spotiq.ui.login.StartupActivity;

@Singleton
@Component(modules = { AppModule.class })
public interface AppComponent {
    void inject(AuthenticationActivity activity);
    void inject(StartupActivity activity);
}
