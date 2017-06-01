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
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.model.SpotifyAuthenticator;
import se.zinokader.spotiq.ui.login.SpotifyAuthenticationActivity;

@Singleton
public class SpotifyCommunicatorService extends Service {

    private static final long TOKEN_EXPIRY_CUTOFF = TimeUnit.MINUTES.toSeconds(20);

    private static final SpotifyAuthenticator spotifyAuthenticator = new SpotifyAuthenticator();
    private static final SpotifyApi spotifyWebApi = new SpotifyApi();
    private static Disposable tokenRenewalJob;

    /**
     * We are using a service to simplify context-handling
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * We are implementing our service this way because the only way to refresh the token is opening
     * a new activity, which is annoying when the user is navigating other apps while using SpotiQ.
     * To resolve this, we are registering an observable interval that will mimic a foreground service.
     * This job should be run on resume from all presenters which care about token renewal, and paused
     * on pause. The job will be run on immidiately on call, and then every 5 minutes thereafter.
     */
    public void startForegroundTokenRenewalJob() {
        tokenRenewalJob = Observable.interval(0, 5, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(interval -> {
                    if (spotifyAuthenticator.getExpiresIn() < TOKEN_EXPIRY_CUTOFF) {
                        Intent loginIntent = new Intent(getApplicationContext(), SpotifyAuthenticationActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        getApplicationContext().startActivity(loginIntent);
                        Log.d(LogTag.LOG_TOKEN_SERVICE, "Token was updated. New token: " + spotifyAuthenticator.getAccessToken());
                    }
                    else {
                        Log.d(LogTag.LOG_TOKEN_SERVICE, "Token wasn't updated - existing token still valid");
                    }
                });
    }

    public void pauseForegroundTokenRenewalJob() {
        tokenRenewalJob.dispose();
    }

    public SpotifyAuthenticator getAuthenticator() {
        return spotifyAuthenticator;
    }

    public SpotifyService getWebApi() {
        return spotifyWebApi.setAccessToken(getAuthenticator().getAccessToken()).getService();
    }

}
