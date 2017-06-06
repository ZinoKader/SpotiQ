package se.zinokader.spotiq.feature.party;

import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public class PartyPresenter extends BasePresenter<PartyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private String partyName;

    private Disposable partyMemberSubscription;
    private CompositeDisposable disposableSubscriptions = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void resume() {
        getView().startForegroundTokenRenewalService();
        subscribeToPartyMemberChanges();
    }

    void pause() {
        getView().stopForegroundTokenRenewalService();
        unsubscribeToPartyMemberChanges();
    }

    void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    void loadParty() {
        if (partyMemberSubscription == null) {
            partyMemberSubscription = partiesRepository.getPartyMembers(partyName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(childEvent -> {
                        User partyMember = childEvent.dataSnapshot().getValue(User.class);
                        getView().addPartyMember(partyMember);
                        Log.d(LogTag.LOG_PARTY, "USER FROM DB: " + partyMember.getUserId());
                    });
        }
    }

    void loadUser() {
        spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPrivate -> {
                    User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                    getView().setUserDetails(user.getUserName(), user.getUserImageUrl());
                    loadHost(user.getUserId(), user.getUserName());
                });
    }

    private void subscribeToPartyMemberChanges() {
        disposableSubscriptions.add(partyMemberSubscription);
    }

    private void unsubscribeToPartyMemberChanges() {
        disposableSubscriptions.remove(partyMemberSubscription);
    }

    private void loadHost(String userId, String userName) {
        partiesRepository.isHostOfParty(userId, partyName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userIsHost -> {
                    getView().setHostDetails(userName);
                    if (userIsHost) {
                        getView().setHostPriviliges();
                    }
                });
    }

}
