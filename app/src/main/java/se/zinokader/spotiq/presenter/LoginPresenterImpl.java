package se.zinokader.spotiq.presenter;

import se.zinokader.spotiq.view.LoginView;

public class LoginPresenterImpl implements LoginPresenter {

    private LoginView view;

    @Override
    public void setView(LoginView view) {
        this.view = view;
    }

}
