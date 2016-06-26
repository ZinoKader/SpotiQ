package se.zinokader.spotiq.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Spotify;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import se.zinokader.spotiq.MvpApplication;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.presenter.LoginPresenter;
import se.zinokader.spotiq.view.LoginView;

public class LoginActivity extends BaseActivity implements LoginView, ConnectionStateCallback {

    @Inject
    LoginPresenter loginPresenter;

    @BindView(R.id.activity_login_view)
    View mainview;
    @BindView(R.id.logoTextView)
    TextView logoTextView;
    @BindView(R.id.loginButton)
    Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LoginTheme);
        setContentView(R.layout.activity_login);
        ((MvpApplication)getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);
        logoTextView.setTypeface(BACKTOBLACK);
        loginButton.setTypeface(ROBOTOLIGHT);
    }

    @Override
    public void onResume() {
        super.onResume();
        loginPresenter.setView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Spotify.destroyPlayer(this);
    }

    @OnClick(R.id.loginButton)
    @Override
    public void loginPressed() {
        loginPresenter.login(this);
    }

    @Override
    public void onLoggedIn() {
        Snackbar.make(mainview, "Logged in successfully", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoggedOut() {
        Log.d("Logged out", "logged out");
        Snackbar.make(mainview, "Logged out", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Snackbar.make(mainview, "Failed to log in", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            LobbyActivity.resultCode = resultCode;
            LobbyActivity.requestCode = requestCode;
            LobbyActivity.intent = intent;
            Intent i = new Intent(LoginActivity.this, LobbyActivity.class);
            startActivity(i);
        }
    }
}

