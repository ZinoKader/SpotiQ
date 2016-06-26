package se.zinokader.spotiq;

import dagger.Module;
import dagger.Provides;
import se.zinokader.spotiq.presenter.LoginPresenter;
import se.zinokader.spotiq.presenter.LoginPresenterImpl;
import se.zinokader.spotiq.presenter.LobbyPresenter;
import se.zinokader.spotiq.presenter.LobbyPresenterImpl;
import se.zinokader.spotiq.presenter.PartyPresenter;
import se.zinokader.spotiq.presenter.PartyPresenterImpl;
import se.zinokader.spotiq.presenter.SearchPresenter;
import se.zinokader.spotiq.presenter.SearchPresenterImpl;

@Module
public class AppModule {

    @Provides
    public LoginPresenter provideLoginPresenter() {
        return new LoginPresenterImpl();
    }

    @Provides
    public LobbyPresenter provideLobbyPresenter() {
        return new LobbyPresenterImpl();
    }

    @Provides
    public PartyPresenter providePartyPresenter() { return new PartyPresenterImpl();}

    @Provides
    public SearchPresenter provideSearchPresenter() { return new SearchPresenterImpl();}

}
