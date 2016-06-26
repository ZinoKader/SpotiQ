package se.zinokader.spotiq.presenter;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.view.LoginView;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;
import static se.zinokader.spotiq.R.id.logoTextView;

public class LoginPresenterImpl implements LoginPresenter {

    private LoginView view;
    private static final String CLIENT_ID = "5646444c2abc4d8299ee3f2cb274f0b6";
    private static final String REDIRECT_URI = "spotiq://callback";
    private static final int REQUEST_CODE = 1337;


    @Override
    public void setView(LoginView view) {
        this.view = view;
    }

    @Override
    public void login(Activity activity) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
    }



}
