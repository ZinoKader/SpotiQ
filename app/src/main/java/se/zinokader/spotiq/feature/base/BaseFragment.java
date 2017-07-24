package se.zinokader.spotiq.feature.base;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;

import nucleus5.factory.PresenterFactory;
import nucleus5.presenter.Presenter;
import nucleus5.view.NucleusSupportFragment;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.util.di.Injector;

public abstract class BaseFragment<P extends Presenter> extends NucleusSupportFragment<P> implements BaseView {

    private boolean snackbarShowing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PresenterFactory<P> superFactory = super.getPresenterFactory();
        setPresenterFactory(superFactory == null ? null : (PresenterFactory<P>) () -> {
            P presenter = superFactory.createPresenter();
            ((Injector) getActivity().getApplication()).inject(presenter);
            return presenter;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void showMessage(String message) {
        if (snackbarShowing) {
            deferMessage(message);
        }
        else {
            snackbarShowing = true;
            new SnackbarBuilder(((BaseView) getActivity()).getRootView())
                .message(message)
                .dismissCallback((snackbar, i) -> snackbarShowing = false)
                .build()
                .show();
        }
    }

    private void deferMessage(String message) {
        new Handler().postDelayed(() -> {
            snackbarShowing = true;
            new SnackbarBuilder(((BaseView) getActivity()).getRootView())
                .message(message)
                .dismissCallback((snackbar, i) -> snackbarShowing = false)
                .build()
                .show();
        }, ApplicationConstants.DEFER_SNACKBAR_DELAY);
    }

    @Override
    public void finishWithSuccess(String message) {
        ((BaseView) getActivity()).finishWithSuccess(message);
    }

    @Override
    public View getRootView() {
        return ((BaseView) getActivity()).getRootView();
    }
}
