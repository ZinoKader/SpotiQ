package se.zinokader.spotiq.feature.party;

import android.support.annotation.NonNull;
import android.util.Log;

import net.grandcentrix.thirtyinch.TiPresenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

public class PartyPresenter extends TiPresenter<PartyView> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    private String partyName;

    @Override
    protected void onAttachView(@NonNull PartyView view) {
        super.onAttachView(view);
        if (!view.isPresenterAttached()) {
            view.setPresenter(this);
        }
    }

    void init() {
        loadParty();
        loadUser();
    }

    void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    private void loadParty() {
        partiesRepository.getPartyMembers(partyName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(childEvent -> {
                    User partyMember = childEvent.dataSnapshot().getValue(User.class);
                    sendToView(view -> view.addPartyMember(partyMember));
                    Log.d(LogTag.LOG_PARTY, "USER FROM DB: " + partyMember.getUserId());
                });
    }

    private void loadUser() {
        spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPrivate -> {
                    User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                    sendToView(view -> view.setUserDetails(user.getUserName(), user.getUserImageUrl()));
                    loadHost(user.getUserId(), user.getUserName());
                });
    }

    private void loadHost(String userId, String userName) {
        partiesRepository.isHostOfParty(userId, partyName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userIsHost -> {
                    sendToView(view -> view.setHostDetails(userName));
                    if (userIsHost) {
                        sendToView(PartyView::setHostPriviliges);
                    }
                });
    }

}
