package se.zinokader.spotiq.feature.party;

import android.os.Bundle;

import com.spotify.sdk.android.player.Spotify;

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

    private static final int LOAD_USER_RESTARTABLE_ID = 9814;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        //listen to user changes
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.NETWORK_RETRY_DELAY_SEC, TimeUnit.SECONDS)),
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
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    void setPartyTitle(String partyTitle) {
        this.partyTitle = partyTitle;
    }

    private void loadHost(String userId) {
        add(partiesRepository.isHostOfParty(userId, partyTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .retryWhen(throwable -> throwable.delay(ApplicationConstants.NETWORK_RETRY_DELAY_SEC, TimeUnit.SECONDS))
            .subscribe(resultDelivery -> resultDelivery.split((partyView, userIsHost) -> {
                if (userIsHost) {
                    //loadPlayer();
                    partyView.setHostPriviliges();
                    partyView.showMessage("Connected as a party host");
                }
            }, (partyView, throwable) -> {
                partyView.showMessage("Could not load host priviliges, retrying...");
            })));
    }

}
