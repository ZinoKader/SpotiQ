package se.zinokader.spotiq.feature.base;

import android.view.View;

import net.grandcentrix.thirtyinch.TiView;

public interface BaseView extends TiView {
    void showMessage(String message);
    View getRootView();
}
