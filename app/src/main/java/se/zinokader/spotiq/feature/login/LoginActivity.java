package se.zinokader.spotiq.feature.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.databinding.ActivityLoginBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.lobby.LobbyActivity;

@RequiresPresenter(LoginPresenter.class)
public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginView {

    ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.logInButton.setOnClickListener(c -> {
            startProgress();
            getPresenter().logIn();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.logInButton.dispose();
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

    public void goToSpotifyAuthentication() {
        startActivityForResult(new Intent(this, SpotifyAuthenticationActivity.class),
            ApplicationConstants.LOGIN_INTENT_REQUEST_CODE);
    }

    public void goToLobby() {
        getRootView().post(() -> {
            Animator activityTransition = prepareCircularRevealAnimation();
            activityTransition.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startActivity(new Intent(LoginActivity.this, LobbyActivity.class));
                    delayResetAnimatedChanges();
                }
            });
            activityTransition.start();
        });
    }

    private Animator prepareCircularRevealAnimation() {
        Animator circularRevealAnimation;

        //calculate bounds
        int x = binding.root.getWidth() / 2;
        int y = binding.root.getHeight() / 2;
        int startRadius = binding.logInButton.getWidth();
        int endRadius = (int) Math.hypot(binding.root.getWidth(), binding.root.getHeight());

        //hide unwanted views
        binding.root.setVisibility(View.GONE);
        binding.logInButton.setVisibility(View.GONE);
        binding.spotiqLogo.setVisibility(View.GONE);

        //prepare root view color
        binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        binding.root.setVisibility(View.VISIBLE);

        circularRevealAnimation = ViewAnimationUtils.createCircularReveal(binding.root,
            x, y, startRadius, endRadius);
        circularRevealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        circularRevealAnimation.setDuration(ApplicationConstants.DIALOG_ANIMATION_DURATION);

        return circularRevealAnimation;
    }

    private void delayResetAnimatedChanges() {
        new Handler().postDelayed( () -> {
            //unhide views that were hidden on animation
            binding.logInButton.revertAnimation();
            binding.root.setVisibility(View.VISIBLE);
            binding.logInButton.setVisibility(View.VISIBLE);
            binding.spotiqLogo.setVisibility(View.VISIBLE);
            binding.root.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.colorPrimary));
        }, 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != ApplicationConstants.LOGIN_INTENT_REQUEST_CODE) {
            Log.d(LogTag.LOG_LOGIN, "Wrong request code on Spotify auth. Expected: " +
                ApplicationConstants.LOGIN_INTENT_REQUEST_CODE + ", received: " + requestCode);
            return;
        }

        //TODO: Remove (debug only)
        //showMessage("Result code: " + resultCode);

        if (resultCode == SpotifyConstants.RESULT_CODE_AUTHENTICATED) {
            getPresenter().logInFinished();
        }
        else if (resultCode == SpotifyConstants.RESULT_CODE_NO_PREMIUM) {
            getPresenter().logInFailed(false);
        }
        else {
            getPresenter().logInFailed(true);
        }
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }
}
