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
import se.zinokader.spotiq.databinding.ActivityStartupBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.lobby.LobbyActivity;

@RequiresPresenter(StartupPresenter.class)
public class StartupActivity extends BaseActivity<StartupPresenter> implements StartupView {

    ActivityStartupBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handle when the app is open and the app is resumed by opening it from the launcher
        //in that case we want to go to the latest activity and immidiately finish this one
        if (!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_startup);
        binding.logInButton.setOnClickListener(c -> getPresenter().logIn());
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
                    startActivity(new Intent(getApplicationContext(), LobbyActivity.class));
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

        if (resultCode == SpotifyConstants.RESULT_CODE_AUTHENTICATED) {
            getPresenter().logInFinished();
        }
        else if(resultCode == SpotifyConstants.RESULT_CODE_NO_PREMIUM) {
            getPresenter().logInFailed(true);
        }
        else {
            getPresenter().logInFailed(false);
        }
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }
}
