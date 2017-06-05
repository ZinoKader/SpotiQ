package se.zinokader.spotiq.app;

import javax.inject.Singleton;

import dagger.Component;
import se.zinokader.spotiq.feature.lobby.LobbyActivity;
import se.zinokader.spotiq.feature.lobby.LobbyPresenter;
import se.zinokader.spotiq.feature.login.SpotifyAuthenticationActivity;
import se.zinokader.spotiq.feature.login.StartupActivity;
import se.zinokader.spotiq.feature.login.StartupPresenter;
import se.zinokader.spotiq.feature.party.PartyActivity;
import se.zinokader.spotiq.feature.party.PartyPresenter;

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
