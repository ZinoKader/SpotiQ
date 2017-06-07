package se.zinokader.spotiq.feature.login;

import se.zinokader.spotiq.feature.base.BaseView;

public interface StartupView extends BaseView {
    void setPresenter(StartupPresenter presenter);
    void startProgress();
    void finishProgress();
    void resetProgress();
    void goToSpotifyAuthentication();
    void goToLobby();
}
