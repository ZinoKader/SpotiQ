package se.zinokader.spotiq.app;

import javax.inject.Singleton;

import dagger.Component;
import se.zinokader.spotiq.ui.lobby.LobbyActivity;
import se.zinokader.spotiq.ui.lobby.LobbyPresenter;
import se.zinokader.spotiq.ui.login.SpotifyAuthenticationActivity;
import se.zinokader.spotiq.ui.login.StartupActivity;
import se.zinokader.spotiq.ui.login.StartupPresenter;
import se.zinokader.spotiq.ui.party.PartyActivity;
import se.zinokader.spotiq.ui.party.PartyPresenter;

@Singleton
@Component(modules = { AppModule.class })
public interface AppComponent {
    void inject(SpotifyAuthenticationActivity target);
    void inject(StartupActivity target);
    void inject(StartupPresenter target);
    void inject(LobbyActivity target);
    void inject(LobbyPresenter target);
    void inject(PartyActivity target);
    void inject(PartyPresenter target);
}
