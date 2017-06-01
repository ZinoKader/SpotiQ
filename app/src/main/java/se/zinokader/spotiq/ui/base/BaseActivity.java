package se.zinokader.spotiq.ui.base;

import android.os.Bundle;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;

import nucleus5.factory.PresenterFactory;
import nucleus5.presenter.Presenter;
import nucleus5.view.NucleusAppCompatActivity;
import se.zinokader.spotiq.util.di.Injector;

public abstract class BaseActivity<T extends Presenter> extends NucleusAppCompatActivity<T> implements BaseView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final PresenterFactory<T> superFactory = super.getPresenterFactory();
        setPresenterFactory( () -> {
            T presenter = superFactory.createPresenter();
            ((Injector) getApplication()).inject(presenter);
            return presenter;
        });
        super.onCreate(savedInstanceState);
    }

    public void showMessage(String message) {
        new SnackbarBuilder(getRootView())
                .message(message)
                .build()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
