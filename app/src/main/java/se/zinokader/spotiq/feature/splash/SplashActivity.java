package se.zinokader.spotiq.feature.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivitySplashBinding;
import se.zinokader.spotiq.feature.login.StartupActivity;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        //handle when the app is open and the app is resumed by opening it from the launcher
        //in that case we want to go to the latest activity and immidiately finish this one
        if (!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
        }
        else {
            animateToLoginActivity();
        }
    }

    private void animateToLoginActivity() {

        AnimatorSet logoAnimationSet = new AnimatorSet();

        logoAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Intent intent = new Intent(SplashActivity.this, StartupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        ValueAnimator scaleDownAnimator = ValueAnimator.ofFloat(1, ApplicationConstants.ANIMATION_LOGO_SCALE_DOWN);
        ValueAnimator scaleUpAnimator = ValueAnimator.ofFloat(ApplicationConstants.ANIMATION_LOGO_SCALE_DOWN, ApplicationConstants.ANIMATION_LOGO_SCALE_UP);

        scaleDownAnimator.setInterpolator(new OvershootInterpolator());
        scaleUpAnimator.setInterpolator(new AccelerateInterpolator());
        scaleDownAnimator.setDuration(ApplicationConstants.ANIMATION_LOGO_SCALE_DOWN_DURATION_MS);
        scaleUpAnimator.setDuration(ApplicationConstants.ANIMATION_LOGO_SCALE_UP_DURATION_MS);

        scaleDownAnimator.addUpdateListener(valueAnimator -> {
            float scalePercent = (float) valueAnimator.getAnimatedValue();
            binding.spotiqLogo.setScaleX(scalePercent);
            binding.spotiqLogo.setScaleY(scalePercent);
        });
        scaleUpAnimator.addUpdateListener(valueAnimator -> {
            float scalePercent = (float) valueAnimator.getAnimatedValue();
            binding.spotiqLogo.setScaleX(scalePercent);
            binding.spotiqLogo.setScaleY(scalePercent);
        });

        logoAnimationSet
            .play(scaleDownAnimator)
            .before(scaleUpAnimator);
        logoAnimationSet.start();
    }

}
