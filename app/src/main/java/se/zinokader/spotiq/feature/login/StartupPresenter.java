package se.zinokader.spotiq.feature.login;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.repository.UserRepository;
import se.zinokader.spotiq.util.type.Empty;


public class StartupPresenter extends BasePresenter<StartupView> {

    @Inject
    UserRepository userRepository;

    void logIn() {
        userRepository.logInFirebaseUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(loginDelivery -> loginDelivery.split(
                (startupView, didLogin) -> {
                    if (didLogin) {
                        startupView.showMessage("Registering and authenticating user...");
                    } else {
                        startupView.showMessage("Could not connect to SpotiQ servers");
                    }
                    startupView.startProgress();
                    Observable.just(ApplicationConstants.SHORT_ACTION_DELAY_SEC)
                        .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS)
                        .subscribe(delay -> startupView.goToSpotifyAuthentication());
                },
                (startupView, throwable) -> {
                    startupView.showMessage("Something went wrong, please try again");
                }));
    }

    void logInFinished() {
        Observable.just(new Empty())
            .observeOn(AndroidSchedulers.mainThread())
            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .doOnNext(firstDelayFinishedDelivery -> firstDelayFinishedDelivery.split(
                (startupView, empty) -> {
                    startupView.finishProgress();
                    startupView.showMessage("Successfully authenticated with Spotify");
                },
                (startupView, throwable) -> {
                })
            )
            .delay(ApplicationConstants.LONG_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe(delayFinishedDelivery -> delayFinishedDelivery.split(
                (startupView, empty) -> {
                    startupView.goToLobby();
                },
                (startupView, throwable) -> {
                }));
    }

    void logInFailed() {
        Observable.just(new Empty())
            .observeOn(AndroidSchedulers.mainThread())
            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(delayFinishedDelivery -> delayFinishedDelivery.split(
                (startupView, empty) -> {
                    startupView.resetProgress();
                    startupView.showMessage("Something went wrong on Spotify authentication");
                },
                (startupView, throwable) -> {
                }));
    }

}
