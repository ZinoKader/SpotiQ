package se.zinokader.spotiq.ui.base;

import android.os.Bundle;

import nucleus5.factory.PresenterFactory;
import nucleus5.presenter.Presenter;
import nucleus5.view.NucleusAppCompatActivity;
import se.zinokader.spotiq.util.Injector;

public class BaseActivity<T extends Presenter> extends NucleusAppCompatActivity<T> {

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

}
