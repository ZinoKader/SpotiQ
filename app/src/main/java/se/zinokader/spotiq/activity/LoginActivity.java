package se.zinokader.spotiq.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.dd.processbutton.iml.ActionProcessButton;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.SpotiqApplication;
import se.zinokader.spotiq.constants.Constants;
import se.zinokader.spotiq.presenter.LoginPresenter;
import se.zinokader.spotiq.view.LoginView;


public class LoginActivity extends BaseActivity implements LoginView, ConnectionStateCallback {

    @Inject
    LoginPresenter loginPresenter;

    @BindView(R.id.activity_login_view)
    View mainview;
    @BindView(R.id.spotiq_logo)
    ImageView spotiqlogo;
    @BindView(R.id.loginButton)
    ActionProcessButton loginbutton;
    @BindView(R.id.create_party_button)
    ImageView createpartybutton;
    @BindView(R.id.join_party_button)
    ImageView joinpartybutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LoginTheme);
        setContentView(R.layout.activity_login);
        ((SpotiqApplication)getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);

        loginbutton.setTypeface(ROBOTOLIGHT);
        loginbutton.setMode(ActionProcessButton.Mode.ENDLESS);
        loginbutton.setColorScheme(ContextCompat.getColor(this, R.color.colorPrimaryTransparent), ContextCompat.getColor(this, R.color.materialWhite),
                ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorLightTint));

    }

    @Override
    public void onResume() {
        super.onResume();
        loginPresenter.setView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.loginButton)
    @Override
    public void loginPressed() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(Constants.SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, Constants.SPOTIFY_REDIRECT_URI);
        builder.setScopes(new String[]{Constants.SPOTIFY_PERMISSION_READ_PRIVATE, Constants.SPOTIFY_PERMISSION_STREAMING,
                Constants.SPOTIFY_PERMISSION_MODIFYPUBLICPLAYLIST, Constants.SPOTIFY_PERMISSION_MODIFYPRIVATEPLAYLIST});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, Constants.REQUEST_CODE, request);

        loginbutton.setProgress(1);
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

        if (requestCode == Constants.REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent i = new Intent(LoginActivity.this, LobbyActivity.class);
                            Bundle responsebundle = new Bundle();
                            responsebundle.putParcelable("response", response);
                            i.putExtra("responsebundle", responsebundle);

                            Pair<View, String> p1 = Pair.create((View)spotiqlogo, getString(R.string.logo_transition_name));
                            Pair<View, String> p2 = Pair.create((View)createpartybutton, getString(R.string.create_party_button_transition));
                            Pair<View, String> p3 = Pair.create((View)joinpartybutton, getString(R.string.join_party_button_transition));

                            ActivityOptionsCompat sharedtransition = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(LoginActivity.this, p1, p2, p3);
                            ActivityCompat.startActivity(LoginActivity.this, i, sharedtransition.toBundle());

                            loginbutton.setProgress(100);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginbutton.setProgress(0);
                                }
                            }, 2000);
                        }
                    }, 800); //delay så att lobbyactivity hinner beräkna nya bounds på logo i transition
                    break;
                case ERROR:
                    Snackbar.make(mainview, "Something went wrong, that's all we know", Snackbar.LENGTH_LONG)
                            .setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {loginPressed();}}).show();
                    Log.d("LOGIN ERROR", response.getError());
                    loginbutton.setProgress(-1);
                    break;
                //Användaren tryckte bak medan auth pågick
                default:
                    Snackbar.make(mainview, "Authentication cancelled", Snackbar.LENGTH_LONG).show();
                    loginbutton.setProgress(0);
            }

        }

    }
}

