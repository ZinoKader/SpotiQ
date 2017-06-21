package se.zinokader.spotiq.feature.login;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.repository.UserRepository;
import se.zinokader.spotiq.util.type.Empty;


public class StartupPresenter extends BasePresenter<StartupView> {

    @Inject
    UserRepository userRepository;

    private CompositeDisposable disposableActions = new CompositeDisposable();

    void logIn() {
        view().subscribe(startupViewOptionalView -> {
            if (startupViewOptionalView.view != null) {
                userRepository.logInFirebaseUser()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(didLogin -> {
                        if (didLogin) {
                            startupViewOptionalView.view.showMessage("Registering and authenticating user...");
                        } else {
                            startupViewOptionalView.view.showMessage("Could not connect to SpotiQ servers");
                        }
                        startupViewOptionalView.view.startProgress();
                        Observable.just(ApplicationConstants.SHORT_ACTION_DELAY_SEC)
                            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS)
                            .subscribe(delay -> startupViewOptionalView.view.goToSpotifyAuthentication());
                    }, throwable -> {

                    });
            }
        }).dispose();
    }

    void logInFinished() {
        disposableActions.add(view().subscribe(startupViewOptionalView -> {
            if (startupViewOptionalView.view != null) {
                Observable.just(new Empty())
                    .observeOn(AndroidSchedulers.mainThread())
                    .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .doOnNext(firstDelayFinished -> {
                        startupViewOptionalView.view.finishProgress();
                        startupViewOptionalView.view.showMessage("Successfully authenticated with Spotify");
                    })
                    .delay(ApplicationConstants.LONG_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe(delayFinished -> {
                        disposableActions.clear();
                        startupViewOptionalView.view.goToLobby();
                    });
            }
        }));
    }

    void logInFailed() {
        disposableActions.add(view().subscribe(startupViewOptionalView -> {
            if (startupViewOptionalView.view != null) {
                Observable.just(new Empty())
                    .observeOn(AndroidSchedulers.mainThread())
                    .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe(delayFinished -> {
                        disposableActions.clear();
                        startupViewOptionalView.view.resetProgress();
                        startupViewOptionalView.view.showMessage("Something went wrong on Spotify authentication");
                    });
            }
        }));
    }

}
