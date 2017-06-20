package se.zinokader.spotiq.feature.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.inputmethod.InputMethodManager;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;

import nucleus5.factory.PresenterFactory;
import nucleus5.presenter.Presenter;
import nucleus5.view.NucleusAppCompatActivity;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.util.di.Injector;

public abstract class BaseActivity<P extends Presenter> extends NucleusAppCompatActivity<P> implements BaseView {

    private boolean snackbarShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PresenterFactory<P> superFactory = super.getPresenterFactory();
        setPresenterFactory(superFactory == null ? null : (PresenterFactory<P>) () -> {
            P presenter = superFactory.createPresenter();
            ((Injector) getApplication()).inject(presenter);
            return presenter;
        });
    }

    @Override
    public void onPause() {
        hideKeyboard();
        super.onPause();
    }

    public void showMessage(String message) {
        snackbarShowing = true;
        new SnackbarBuilder(getRootView())
            .message(message)
            .timeoutDismissCallback(snackbar -> snackbarShowing = false)
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

    public boolean isSnackbarShowing() {
        return snackbarShowing;
    }
}
