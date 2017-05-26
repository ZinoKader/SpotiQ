package se.zinokader.spotiq.ui.login;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import se.zinokader.spotiq.service.SpotifyService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class StartupPresenter extends BasePresenter<StartupActivity> {

    private static final int LOG_IN_DELAY = 2;
    private static final int FINISH_DELAY = 1;

    @Inject
    SpotifyService spotifyService;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    public void logIn() {
        getView().startProgress();
        Observable.just(LOG_IN_DELAY)
                .delay(LOG_IN_DELAY, TimeUnit.SECONDS)
                .subscribe( success -> getView().goToAuthentication());
    }

    public void logInFinished() {
        getView().finishProgress();
        Observable.just(FINISH_DELAY)
                .delay(FINISH_DELAY, TimeUnit.SECONDS)
                .subscribe( success -> getView().goToLobby());
    }
}
