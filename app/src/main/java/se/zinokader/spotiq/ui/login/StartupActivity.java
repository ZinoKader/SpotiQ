package se.zinokader.spotiq.ui.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

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
        startActivityForResult(new Intent(this, AuthenticationActivity.class),
                SpotifyConstants.LOGIN_REQUEST_CODE);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void goToLobby() {
        Animator activityTransition = getCircularRevealAnimation();
        activityTransition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(new Intent(getApplicationContext(), LobbyActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                delayResetAnimatedChanges();
            }
        });
        activityTransition.start();
    }

    private Animator getCircularRevealAnimation() {
        //calculate bounds
        int x = binding.root.getWidth() / 2;
        int y = binding.root.getHeight() / 2;
        int startRadius = binding.logInButton.getWidth();
        int endRadius = (int) Math.hypot(binding.root.getWidth(), binding.root.getHeight());

        //hide unwanted views
        binding.root.setVisibility(View.GONE);
        binding.logInButton.setVisibility(View.GONE);
        binding.spotiqLogo.setVisibility(View.GONE);

        Animator circularRevealAnimation = ViewAnimationUtils.createCircularReveal(binding.root,
                x, y, startRadius, endRadius);
        circularRevealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        //prepare root view color
        binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        binding.root.setVisibility(View.VISIBLE);

        return circularRevealAnimation;
    }

    private void delayResetAnimatedChanges() {
        new Handler().postDelayed( () -> {
            binding.logInButton.revertAnimation();
            binding.root.setVisibility(View.VISIBLE);
            binding.logInButton.setVisibility(View.VISIBLE);
            binding.spotiqLogo.setVisibility(View.VISIBLE);
            binding.root.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorPrimary));
        }, 1000);
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
