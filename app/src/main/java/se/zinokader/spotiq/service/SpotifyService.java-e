package se.zinokader.spotiq.service;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.constants.ServiceConstants;
import se.zinokader.spotiq.model.SpotifyAuthenticator;
import se.zinokader.spotiq.ui.login.AuthenticationActivity;

@Singleton
public class SpotifyService extends Job {

    private static final long TOKEN_EXPIRY_OFFSET = TimeUnit.MINUTES.toSeconds(15);
    private static final long TOKEN_EXPIRY_CUTOFF = TimeUnit.MINUTES.toSeconds(20);
    private static final SpotifyAuthenticator spotifyAuthenticator = new SpotifyAuthenticator();

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        if (spotifyAuthenticator.getExpiresIn() < TOKEN_EXPIRY_CUTOFF) {
            Intent loginIntent = new Intent(getContext(), AuthenticationActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(loginIntent);
            Log.d(LogTag.LOG_TOKEN_SERVICE, "Ran token renewal job successfully");
            Log.d(LogTag.LOG_TOKEN_SERVICE, "NEW TOKEN: " + spotifyAuthenticator.getAccessToken());
        }
        else {
            Log.d(LogTag.LOG_TOKEN_SERVICE, "Token wasn't updated - existing token still valid");
        }
        return Result.SUCCESS;
    }

    public void scheduleTokenRenewal() {
        long updatePeriodMillis = TimeUnit.SECONDS.toMillis(spotifyAuthenticator.getExpiresIn() - TOKEN_EXPIRY_OFFSET);
        long updatePeriodFlexMillis = TimeUnit.MINUTES.toMillis(5);

        new JobRequest.Builder(ServiceConstants.TOKEN_RENEWAL_JOB_TAG)
                .setPeriodic(updatePeriodMillis, updatePeriodFlexMillis)
                .setPersisted(false)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setUpdateCurrent(true) //only have one instance of this job running at a time
                .setRequirementsEnforced(true)
                .build()
                .schedule();

        Log.d(LogTag.LOG_TOKEN_SERVICE, "Token renewal job scheduled");
    }

    public SpotifyAuthenticator getAuthenticator() {
        return spotifyAuthenticator;
    }

}
