package se.zinokader.spotiq.feature.login;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.util.type.Empty;


public class StartupPresenter extends BasePresenter<StartupView> {

    public static final int LOG_IN_RESTARTABLE_ID = 8265;
    public static final int LOG_IN_FINISHED_RESTARTABLE_ID = 7161;
    public static final int LOG_IN_FAILED_RESTARTABLE_ID = 4716;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        //Log in //TODO: Move this to a repository and return Observable<Boolean> to handle failure correctly
        restartableFirst(LOG_IN_RESTARTABLE_ID,
            () -> Observable.create(subscriber -> FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    subscriber.onNext(true);
                    subscriber.onComplete();
                })
                .addOnFailureListener(subscriber::onError)),
            (startupView, o) -> {
                startupView.showMessage("Connected to SpotiQ servers");
                startupView.startProgress();
                Observable.just(ApplicationConstants.SHORT_ACTION_DELAY_SEC)
                    .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS)
                    .subscribe(delay -> startupView.goToSpotifyAuthentication());
            },
            (startupView, throwable) -> {
                startupView.showMessage("Could not connect to SpotiQ servers");
            });


        //Connect finished
        restartableFirst(LOG_IN_FINISHED_RESTARTABLE_ID,
            () -> Observable.just(new Empty())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread()),
            (startupView, empty) -> {
                startupView.finishProgress();
                startupView.showMessage("Connected to Spotify successfully");
                Observable.just(new Empty())
                    .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe(delayFinished -> startupView.goToLobby());
            });


        //Connect failed
        restartableFirst(LOG_IN_FAILED_RESTARTABLE_ID,
            () -> Observable.just(new Empty())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread()),
            (startupView, empty) -> {
                startupView.resetProgress();
                startupView.showMessage("Something went wrong when connecting to Spotify");
            });
    }

}
