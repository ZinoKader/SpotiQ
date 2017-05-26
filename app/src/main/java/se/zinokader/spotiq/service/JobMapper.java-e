package se.zinokader.spotiq.service;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import se.zinokader.spotiq.constants.ServiceConstants;

public class JobMapper implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case ServiceConstants.TOKEN_RENEWAL_JOB_TAG:
                return new SpotifyService();
            default:
                return null;
        }
    }
}
