package se.zinokader.spotiq.feature.base;

import android.view.View;

public interface BaseView {
    void showMessage(String message);
    void finishWithSuccess(String message);
    View getRootView();
}
