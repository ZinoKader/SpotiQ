package se.zinokader.spotiq.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import se.zinokader.spotiq.constants.Constants;

public class AuthenticationService extends Service {

    public static int expirytime;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AuthenticationResponse response = intent.getParcelableExtra(Constants.RESPONSE);
        expirytime = response.getExpiresIn();
        checkForExpiry();
        return START_REDELIVER_INTENT;
    }

    public void checkForExpiry() {
        if(expirytime < 300) {
        }
    }

}
