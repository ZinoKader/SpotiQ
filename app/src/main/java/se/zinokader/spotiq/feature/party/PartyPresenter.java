package se.zinokader.spotiq.feature.party;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public class PartyPresenter extends BasePresenter<PartyView> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private String partyTitle;
    private boolean memberTypeMessageShown = false;

    private static final int LOAD_USER_RESTARTABLE_ID = 9814;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (savedState != null) {
            partyTitle = savedState.getString(ApplicationConstants.PARTY_NAME_EXTRA);
            memberTypeMessageShown = savedState.getBoolean(ApplicationConstants.MEMBER_TYPE_MESSAGE_EXTRA);
        }

        //listen to user changes
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS)),
            (partyView, userPrivate) -> {
                User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                partyView.setUserDetails(user.getUserName(), user.getUserImageUrl());
                loadHost(user.getUserId());
            });

        if (savedState == null) {
            start(LOAD_USER_RESTARTABLE_ID);
        }

    }

    @Override
    protected void onSave(@NonNull Bundle state) {
        super.onSave(state);
        state.putString(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        state.putBoolean(ApplicationConstants.MEMBER_TYPE_MESSAGE_EXTRA, memberTypeMessageShown);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    private void loadHost(String userId) {
        partiesRepository.isHostOfParty(userId, partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS))
            .subscribe(resultDelivery -> resultDelivery.split((partyView, userIsHost) -> {
                if (userIsHost) {
                    partyView.setHostPriviliges();
                    if (!memberTypeMessageShown) partyView.showMessage("Connected as a party host");
                }
                else {
                    if (!memberTypeMessageShown) partyView.showMessage("Connected as a party member");
                }
                memberTypeMessageShown = true;
            }, (partyView, throwable) -> {
                partyView.showMessage("Could not load host priviliges, retrying...");
            }));
    }

}
