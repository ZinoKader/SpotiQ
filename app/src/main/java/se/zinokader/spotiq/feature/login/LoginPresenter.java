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


public class LoginPresenter extends BasePresenter<LoginView> {

    @Inject
    UserRepository userRepository;

    void logIn() {
        userRepository.logInFirebaseUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(loginDelivery -> loginDelivery.split(
                (loginView, didLogin) -> {
                    if (didLogin) {
                        loginView.showMessage("Registering and authenticating user...");
                        Observable.just(new Empty())
                            .delay(ApplicationConstants.MEDIUM_ACTION_DELAY_SEC, TimeUnit.SECONDS)
                            .subscribe(delayFinished -> loginView.goToSpotifyAuthentication());
                    }
                    else {
                        loginView.showMessage("Could not connect to SpotiQ servers");
                        loginView.resetProgress();
                    }
                },
                (loginView, throwable) -> {
                    loginView.showMessage("Login failed, please try again");
                }));
    }

    void logInFinished() {
        Observable.just(new Empty())
            .observeOn(AndroidSchedulers.mainThread())
            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .doOnNext(firstDelayFinishedDelivery -> firstDelayFinishedDelivery.split(
                (loginView, empty) -> {
                    loginView.finishProgress();
                    loginView.showMessage("Successfully authenticated with Spotify");
                },
                (loginView, throwable) -> {
                    loginView.showMessage("Login failed, please try again");
                })
            )
            .delay(ApplicationConstants.MEDIUM_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe(delayFinishedDelivery -> delayFinishedDelivery.split(
                (loginView, empty) -> {
                    loginView.goToLobby();
                },
                (loginView, throwable) -> {
                    loginView.showMessage("Login failed, please try again");
                }));
    }

    void logInFailed(boolean hasPremium) {

        String errorMessage = hasPremium
            ? "Something went wrong on Spotify authentication"
            : "You need a Spotify Premium account to log in";

        Observable.just(new Empty())
            .observeOn(AndroidSchedulers.mainThread())
            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(delayFinishedDelivery -> delayFinishedDelivery.split(
                (loginView, empty) -> {
                    loginView.resetProgress();
                    loginView.showMessage(errorMessage);
                },
                (loginView, throwable) -> {
                    loginView.showMessage("Login failed, please try again");
                }));
    }

}
