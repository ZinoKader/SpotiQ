package se.zinokader.spotiq.ui.party;

import android.os.Bundle;

import javax.inject.Inject;

import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class PartyPresenter extends BasePresenter<PartyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }


}
