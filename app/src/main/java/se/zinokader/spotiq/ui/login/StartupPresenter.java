package se.zinokader.spotiq.ui.login;

import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import se.zinokader.spotiq.service.SpotifyAuthenticationService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class StartupPresenter extends BasePresenter {

    @Inject
    SpotifyAuthenticationService spotifyAuthenticationService;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.d("wow", "he");
    }

    void test() {
        Log.d("SHIT", spotifyAuthenticationService.getAuthenticator().getAccessToken());
    }
}
