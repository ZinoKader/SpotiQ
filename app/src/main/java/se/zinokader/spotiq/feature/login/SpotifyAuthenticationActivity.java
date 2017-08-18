package se.zinokader.spotiq.feature.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import javax.inject.Inject;

import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.zinokader.spotiq.BuildConfig;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.util.di.Injector;

/**
 * Unfortunately the Spotify Android SDK forces us to use an Activity to authenticate users
 * This activity's sole purpose is authenticating users
 */
public class SpotifyAuthenticationActivity extends AppCompatActivity implements ConnectionStateCallback {

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        ((Injector) getApplication()).inject(this);
        overridePendingTransition(R.anim.short_fade_in, R.anim.short_fade_out);

        AuthenticationRequest authRequest = new AuthenticationRequest.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            AuthenticationResponse.Type.TOKEN,
            SpotifyConstants.REDIRECT_URI)
            .setScopes(SpotifyConstants.DEFAULT_USER_SCOPES)
            .build();

        AuthenticationClient.openLoginActivity(this,
            ApplicationConstants.LOGIN_INTENT_REQUEST_CODE,
            authRequest);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.short_fade_in, R.anim.short_fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == ApplicationConstants.LOGIN_INTENT_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    Log.d(LogTag.LOG_LOGIN, "Logged in successfully!");

                    //refresh our authentication token
                    spotifyCommunicatorService.getAuthenticator().setExpiryTimeStamp(response.getExpiresIn());
                    spotifyCommunicatorService.getAuthenticator().setAccessToken(response.getAccessToken());

                    spotifyCommunicatorService.getWebApi().getMe(new Callback<UserPrivate>() {
                        @Override
                        public void success(UserPrivate userPrivate, Response response) {
                            if (userPrivate.product.equals(SpotifyConstants.PRODUCT_PREMIUM)) {
                                setResult(SpotifyConstants.RESULT_CODE_AUTHENTICATED);
                            }
                            else {
                                setResult(SpotifyConstants.RESULT_CODE_NO_PREMIUM);
                            }
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            setResult(SpotifyConstants.RESULT_CODE_CONNECTION_FAILURE);
                            finish();
                        }
                    });
                    break;
                case CODE:
                    setResult(SpotifyConstants.RESULT_CODE_SPOTIFY_CODE);
                    finish();
                case EMPTY:
                    setResult(SpotifyConstants.RESULT_CODE_EMPTY);
                    finish();
                case ERROR:
                    setResult(SpotifyConstants.RESULT_CODE_ERROR);
                    finish();
                case UNKNOWN:
                    setResult(SpotifyConstants.RESULT_CODE_UNKNOWN);
                    finish();
                default:
                    setResult(SpotifyConstants.RESULT_CODE_WRONG_REQUEST_CODE);
                    Log.d(LogTag.LOG_LOGIN, "Something went wrong on login");
                    finish();
            }
        }
        else {
            setResult(SpotifyConstants.RESULT_CODE_WRONG_REQUEST_CODE);
            Log.d(LogTag.LOG_LOGIN, "Wrong request code for Spotify login");
            finish();
        }
    }

    @Override
    public void onLoggedIn() {
    }

    @Override
    public void onLoggedOut() {
    }

    @Override
    public void onTemporaryError() {
    }

    @Override
    public void onConnectionMessage(String s) {
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(LogTag.LOG_LOGIN, error.toString());
    }
}
