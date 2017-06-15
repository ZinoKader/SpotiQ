package se.zinokader.spotiq.feature.base;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.inputmethod.InputMethodManager;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;
import com.pascalwelsch.compositeandroid.activity.CompositeActivity;

import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public abstract class BaseActivity extends CompositeActivity implements BaseView {

    @Override
    public void onPause() {
        hideKeyboard();
        super.onPause();
    }

    public void showMessage(String message) {
        new SnackbarBuilder(getRootView())
            .message(message)
            .build()
            .show();
    }

    public void finishWithSuccess(String message) {
        new SnackbarBuilder(getRootView())
            .duration(Snackbar.LENGTH_SHORT)
            .message(message)
            .timeoutDismissCallback(dismissed -> finish())
            .build()
            .show();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getRootView().getWindowToken(), 0);
    }

    public void startForegroundTokenRenewalService() {
        startService(new Intent(this, SpotifyCommunicatorService.class));
    }

    public void stopForegroundTokenRenewalService() {
        stopService(new Intent(this, SpotifyCommunicatorService.class));
    }

}
