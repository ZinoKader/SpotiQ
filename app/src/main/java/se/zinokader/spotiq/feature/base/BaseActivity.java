package se.zinokader.spotiq.feature.base;

import android.content.Intent;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;
import com.pascalwelsch.compositeandroid.activity.CompositeActivity;

import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public abstract class BaseActivity extends CompositeActivity implements BaseView {

    public void showMessage(String message) {
        new SnackbarBuilder(getRootView())
                .message(message)
                .build()
                .show();
    }

    public void startForegroundTokenRenewalService() {
        startService(new Intent(this, SpotifyCommunicatorService.class));
    }

    public void stopForegroundTokenRenewalService() {
        stopService(new Intent(this, SpotifyCommunicatorService.class));
    }

}
