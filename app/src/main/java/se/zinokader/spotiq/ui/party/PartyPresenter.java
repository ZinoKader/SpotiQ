package se.zinokader.spotiq.ui.party;

import android.os.Bundle;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constants.ApplicationConstants;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class PartyPresenter extends BasePresenter<PartyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    private String partyName;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void resume() {
        spotifyCommunicatorService.startForegroundTokenRenewalJob();
    }

    void pause() {
        spotifyCommunicatorService.pauseForegroundTokenRenewalJob();
    }

    void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    void loadUser() {
        SpotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(userPrivate -> {
                    String userId = userPrivate.id;
                    String userName = userPrivate.display_name == null
                            ? userId
                            : userPrivate.display_name;
                    String userImageUrl = userPrivate.images.isEmpty()
                            ? ApplicationConstants.PROFILE_IMAGE_PLACEHOLDER_URL
                            : userPrivate.images.get(0).url;
                    getView().setUserDetails(userName, userImageUrl);
                })
                .switchMap(userPrivate -> partiesRepository.isHostOfParty(userPrivate.id, partyName))
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean isHost) {
                        if (isHost) {
                            getView().setUserDetails("host", "ffdada");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
