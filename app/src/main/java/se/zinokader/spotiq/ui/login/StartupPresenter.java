package se.zinokader.spotiq.ui.login;

import android.os.Bundle;

import com.github.b3er.rxfirebase.auth.RxFirebaseAuth;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class StartupPresenter extends BasePresenter<StartupActivity> {


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void logIn() {
        RxFirebaseAuth.signInAnonymously(FirebaseAuth.getInstance())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            getView().showMessage("Connected to SpotiQ servers");
                            getView().startProgress();
                            Observable.just(1)
                                    .delay(1, TimeUnit.SECONDS)
                                    .subscribe( delay -> getView().goToSpotifyAuthentication());
                        },
                        failed -> getView().showMessage("Could not connect to SpotiQ servers"));
    }

    void logInFinished() {
        Observable.just(1)
                .delay(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(next -> {
                    getView().finishProgress();
                    getView().showMessage("Connected to Spotify successfully");
                })
                .delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe( success -> getView().goToLobby());
    }

    void logInFailed() {
        Observable.just(1)
                .delay(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( failed -> {
                    getView().resetProgress();
                    getView().showMessage("Something went wrong when connecting to Spotify");
                });
    }

}
