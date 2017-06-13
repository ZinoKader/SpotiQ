package se.zinokader.spotiq.feature.base;

import android.view.View;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

public interface BaseView extends TiView {
    void setPresenter(TiPresenter presenter);
    boolean isPresenterAttached();
    void showMessage(String message);
    void finishWithSuccess(String message);
    View getRootView();
}
