package se.zinokader.spotiq.ui.login;

import android.os.Bundle;

import javax.inject.Inject;

import se.zinokader.spotiq.service.SpotifyAuthenticationService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class StartupPresenter extends BasePresenter<StartupActivity> {

    @Inject
    SpotifyAuthenticationService spotifyAuthenticationService;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    public void logIn() {
        getView().goToAuthentication();
    }
}
