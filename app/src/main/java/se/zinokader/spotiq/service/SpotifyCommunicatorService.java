package se.zinokader.spotiq.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.constant.ServiceConstants;
import se.zinokader.spotiq.feature.login.SpotifyAuthenticationActivity;
import se.zinokader.spotiq.model.SpotifyAuthenticator;

@Singleton
public class SpotifyCommunicatorService extends Service {

    private static final SpotifyAuthenticator spotifyAuthenticator = new SpotifyAuthenticator();
    private static final SpotifyApi spotifyWebApi = new SpotifyApi();
    private static Disposable tokenRenewalJob;

    @Override
    public void onCreate() {
        super.onCreate();
        startJob();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopJob();
    }

    /**
     * We are implementing our service this way because the only way to refresh the token is opening
     * a new activity, which is annoying when the user is navigating other apps while using SpotiQ.
     * To resolve this, we are registering an observable interval that will mimic a foreground service.
     * This job should be run on resume from all presenters which care about token renewal, and paused
     * on pause. The job will be run on immidiately on call, and then every 5 minutes thereafter.
     */

    private void startJob() {
        tokenRenewalJob = Observable.interval(0, 5, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(interval -> {
                    if (spotifyAuthenticator.getExpiresIn() < ServiceConstants.TOKEN_EXPIRY_CUTOFF) {
                        Intent loginIntent = new Intent(this, SpotifyAuthenticationActivity.class);
                        startActivity(loginIntent);
                        Log.d(LogTag.LOG_TOKEN_SERVICE, "Token was updated. New token: " + spotifyAuthenticator.getAccessToken());
                    }
                    else {
                        Log.d(LogTag.LOG_TOKEN_SERVICE, "Token wasn't updated - existing token still valid");
                    }
                });
    }

    private void stopJob() {
        tokenRenewalJob.dispose();
    }

    public SpotifyAuthenticator getAuthenticator() {
        return spotifyAuthenticator;
    }

    public SpotifyService getWebApi() {
        return spotifyWebApi.setAccessToken(getAuthenticator().getAccessToken()).getService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
