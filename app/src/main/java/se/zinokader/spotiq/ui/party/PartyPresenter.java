package se.zinokader.spotiq.ui.party;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.zinokader.spotiq.constants.ApplicationConstants;
import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class PartyPresenter extends BasePresenter<PartyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    private String partyName;

    private Disposable partyMemberSubscription;
    private CompositeDisposable disposableSubscriptions = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void resume() {
        spotifyCommunicatorService.startForegroundTokenRenewalJob();
        subscribeToPartyMemberChanges();
    }

    void pause() {
        spotifyCommunicatorService.pauseForegroundTokenRenewalJob();
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
                    .subscribeWith(new DisposableObserver<DataSnapshot>() {
                        @Override
                        public void onNext(DataSnapshot dataSnapshot) {
                            //TODO: First make a user push his name to the party members list when joining a party,
                            //Second, display party members in a list somewhee in the party
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

    void loadUser() {
        spotifyCommunicatorService.getWebApi().getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                //display name can be null, in that case fallback to the userId
                String userId = userPrivate.id;
                String userName = userPrivate.display_name == null
                        ? userId
                        : userPrivate.display_name;
                String userImageUrl = userPrivate.images.isEmpty()
                        ? ApplicationConstants.PROFILE_IMAGE_PLACEHOLDER_URL
                        : userPrivate.images.get(0).url;
                getView().setUserDetails(userName, userImageUrl);
                loadHost(userId, userName);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(LogTag.LOG_LOBBY, "Error when getting user data " + error.getMessage());
                error.printStackTrace();
            }
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
                .subscribe(isHost -> {
                    getView().setHostDetails(userName);
                    if (isHost) {
                        getView().setHostPriviliges();
                    }
                });
    }

}
