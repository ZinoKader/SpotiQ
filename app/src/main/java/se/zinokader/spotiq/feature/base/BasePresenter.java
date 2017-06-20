package se.zinokader.spotiq.feature.base;

import nucleus5.presenter.RxPresenter;

public abstract class BasePresenter<ViewType> extends RxPresenter<ViewType>  {

    public void request(int methodId) {
        start(methodId);
    }

}
