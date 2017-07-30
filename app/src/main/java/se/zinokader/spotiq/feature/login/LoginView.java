package se.zinokader.spotiq.feature.login;

import se.zinokader.spotiq.feature.base.BaseView;

public interface LoginView extends BaseView {
    void startProgress();
    void finishProgress();
    void resetProgress();
    void goToSpotifyAuthentication();
    void goToLobby();
}
