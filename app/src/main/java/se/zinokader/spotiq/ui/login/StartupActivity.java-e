package se.zinokader.spotiq.ui.login;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constants.SpotifyConstants;
import se.zinokader.spotiq.databinding.ActivityStartupBinding;
import se.zinokader.spotiq.ui.base.BaseActivity;
import se.zinokader.spotiq.ui.lobby.LobbyActivity;

@RequiresPresenter(StartupPresenter.class)
public class StartupActivity extends BaseActivity<StartupPresenter> {

    private ActivityStartupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_startup);
        binding.setPresenter(getPresenter());
    }

    public void startProgress() {
        binding.logInButton.startAnimation();
    }

    public void finishProgress() {
        binding.logInButton.doneLoadingAnimation(ContextCompat.getColor(this, R.color.colorAccent),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_finished_white));
    }

    public void resetProgress() {
        binding.logInButton.revertAnimation();
    }

    public void goToAuthentication() {
        startActivityForResult(new Intent(this, AuthenticationActivity.class), SpotifyConstants.LOGIN_REQUEST_CODE);
    }

    public void goToLobby() {
        startActivity(new Intent(this, LobbyActivity.class));
        binding.logInButton.revertAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != SpotifyConstants.LOGIN_REQUEST_CODE) return;

        if (resultCode == RESULT_OK) {
            new SnackbarBuilder(binding.getRoot())
                    .duration(Snackbar.LENGTH_SHORT)
                    .message(R.string.log_in_success)
                    .timeoutDismissCallback(snackbar -> getPresenter().logInFinished())
                    .build()
                    .show();
        }
        else {
            new SnackbarBuilder(binding.getRoot())
                    .duration(Snackbar.LENGTH_LONG)
                    .message(R.string.log_in_failed)
                    .timeoutDismissCallback(snackbar -> getPresenter().logInFailed())
                    .build()
                    .show();
        }
    }
}
