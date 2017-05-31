package se.zinokader.spotiq.ui.login;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.ui.base.BasePresenter;
import se.zinokader.spotiq.util.helper.FirebaseAuthenticationHelper;


public class StartupPresenter extends BasePresenter<StartupActivity> {

    private static final int LOG_IN_DELAY = 1;
    private static final int FINISH_DELAY = 1;

    @Inject
    FirebaseAuthenticationHelper firebaseAuthenticationHelper;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void logIn() {
        firebaseAuthenticationHelper.authenticateAnonymously()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean b) {
                        getView().showConnectedToSpotiqServers();
                        getView().startProgress();
                        Observable.just(LOG_IN_DELAY)
                                .delay(LOG_IN_DELAY, TimeUnit.SECONDS)
                                .subscribe( success -> getView().goToSpotifyAuthentication());
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().showFailedConnectionToSpotiqServers();
                    }

                    @Override
                    public void onSubscribe(Disposable d) {}
                });
    }

    void logInFinished() {
        getView().finishProgress();
        Observable.just(FINISH_DELAY)
                .delay(FINISH_DELAY, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( success -> getView().goToLobby());
    }

    void logInFailed() {
        getView().resetProgress();
    }

}
