package se.zinokader.spotiq.util.mapper;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import se.zinokader.spotiq.constants.ServiceConstants;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public class JobMapper implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case ServiceConstants.TOKEN_RENEWAL_JOB_TAG:
                return new SpotifyCommunicatorService();
            default:
                return null;
        }
    }
}
