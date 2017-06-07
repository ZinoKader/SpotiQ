package se.zinokader.spotiq.feature.login;

import android.support.annotation.NonNull;

import com.github.b3er.rxfirebase.auth.RxFirebaseAuth;
import com.google.firebase.auth.FirebaseAuth;

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;


public class StartupPresenter extends TiPresenter<StartupView> {

    @Override
    public void attachView(@NonNull StartupView view) {
        super.attachView(view);
        view.setPresenter(this);
    }

    void logIn() {
        RxFirebaseAuth.signInAnonymously(FirebaseAuth.getInstance())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                            sendToView(view -> view.showMessage("Connected to SpotiQ servers"));
                            sendToView(StartupView::startProgress);
                            Observable.just(ApplicationConstants.SHORT_ACTION_DELAY)
                                    .delay(ApplicationConstants.SHORT_ACTION_DELAY, TimeUnit.SECONDS)
                                    .subscribe( delay -> sendToView(StartupView::goToSpotifyAuthentication));
                        },
                        failed -> sendToView(view -> view.showMessage("Could not connect to SpotiQ servers")));
    }

    void logInFinished() {
        Observable.just(ApplicationConstants.MEDIUM_ACTION_DELAY)
                .observeOn(AndroidSchedulers.mainThread())
                .delay(ApplicationConstants.SHORT_ACTION_DELAY, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .doOnNext(next -> {
                    sendToView(StartupView::finishProgress);
                    sendToView(view -> view.showMessage("Connected to Spotify successfully"));
                })
                .delay(ApplicationConstants.MEDIUM_ACTION_DELAY, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe( success -> sendToView(StartupView::goToLobby));
    }

    void logInFailed() {
        Observable.just(ApplicationConstants.SHORT_ACTION_DELAY)
                .delay(ApplicationConstants.SHORT_ACTION_DELAY, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(failed -> {
                    sendToView(StartupView::resetProgress);
                    sendToView(view -> view.showMessage("Something went wrong when connecting to Spotify"));
                });
    }

}
