package se.zinokader.spotiq.feature.login;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;


public class StartupPresenter extends TiPresenter<StartupView> {

    @Override
    public void attachView(@NonNull StartupView view) {
        super.attachView(view);
        if (!view.isPresenterAttached()) {
            view.setPresenter(this);
        }
    }

    void logIn() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener(authResult -> {
                sendToView(view -> view.showMessage("Connected to SpotiQ servers"));
                sendToView(StartupView::startProgress);
                Observable.just(ApplicationConstants.SHORT_ACTION_DELAY)
                    .delay(ApplicationConstants.SHORT_ACTION_DELAY, TimeUnit.SECONDS)
                    .subscribe(delay -> sendToView(StartupView::goToSpotifyAuthentication));
            })
            .addOnFailureListener(exception -> sendToView(view -> view.showMessage("Could not connect to SpotiQ servers")));
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
            .subscribe(success -> sendToView(StartupView::goToLobby));
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
