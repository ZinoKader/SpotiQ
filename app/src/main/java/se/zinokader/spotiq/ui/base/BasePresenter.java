package se.zinokader.spotiq.ui.base;

import android.os.Bundle;

import icepick.Icepick;
import nucleus5.presenter.Presenter;

public class BasePresenter<ViewType> extends Presenter<ViewType> {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Icepick.restoreInstanceState(this, savedState);
    }

    @Override
    protected void onSave(Bundle state) {
        super.onSave(state);
        Icepick.saveInstanceState(this, state);
    }

}
