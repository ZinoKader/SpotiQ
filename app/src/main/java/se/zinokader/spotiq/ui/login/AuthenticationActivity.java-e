package se.zinokader.spotiq.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import javax.inject.Inject;

import nucleus5.view.NucleusAppCompatActivity;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.constants.SpotifyConstants;
import se.zinokader.spotiq.service.SpotifyService;
import se.zinokader.spotiq.ui.base.BasePresenter;
import se.zinokader.spotiq.util.Injector;

/**
 * Unfortunately the Spotify Android SDK forces us to use an Activity to authenticate users
 * This activity's sole purpose is authenticating users
 */
public class AuthenticationActivity extends NucleusAppCompatActivity<BasePresenter> implements ConnectionStateCallback {

    @Inject
    SpotifyService spotifyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((Injector) getApplication()).inject(this);

        AuthenticationRequest authRequest = new AuthenticationRequest.Builder(
                SpotifyConstants.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                SpotifyConstants.REDIRECT_URI)
                .setScopes(SpotifyConstants.DEFAULT_USER_SCOPES)
                .build();

        AuthenticationClient.openLoginActivity(this,
                SpotifyConstants.LOGIN_REQUEST_CODE,
                authRequest);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == SpotifyConstants.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    Log.d(LogTag.LOG_LOGIN, "Logged in successfully!");

                    //refresh our authentication token
                    spotifyService.getAuthenticator().setExpiryTimeStamp(response.getExpiresIn());
                    spotifyService.getAuthenticator().setAccessToken(response.getAccessToken());

                    //schedule our token renewal job
                    spotifyService.scheduleTokenRenewal();

                    break;
                default:
                    Log.d(LogTag.LOG_LOGIN, "Something went wrong on login");
            }
        }
        else {
            Log.d(LogTag.LOG_LOGIN, "Wrong request code for Spotify login");
        }

        this.finish();
    }

    @Override
    public void onLoggedIn() {
        Log.d(LogTag.LOG_LOGIN, "Logged in!");
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(LogTag.LOG_LOGIN, error.toString());
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(LogTag.LOG_LOGIN, "Spotify connection message: " + s);
    }
}
