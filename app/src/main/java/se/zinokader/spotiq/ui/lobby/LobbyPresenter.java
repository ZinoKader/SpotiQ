package se.zinokader.spotiq.ui.lobby;

import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class LobbyPresenter extends BasePresenter<LobbyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void loadUser() {
        spotifyCommunicatorService.getWebApi().getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                //display name can be null, in that case fallback to the userId
                String userId = userPrivate.id;
                String userName = userPrivate.display_name == null ? userId : userPrivate.display_name;
                String userImageUrl = userPrivate.images.isEmpty() ? "http://blabla.com" : userPrivate.images.get(0).url;
                Log.d("wallahi", userPrivate.href);
                getView().setUserDetails(userName, userImageUrl);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(LogTag.LOG_LOBBY, "Error when getting user data " + error.getMessage());
                error.printStackTrace();
            }
        });
    }

    void joinParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);

    }

    void createParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);
        Observable.just(party)
                .flatMap(partyWithoutId -> {
                    partyWithoutId.setHostSpotifyId(spotifyCommunicatorService.getWebApi().getMe().id);
                    return partiesRepository.createNewParty(partyWithoutId);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Party>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Party party) {
                        getView().goToParty(party.getTitle());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(LogTag.LOG_LOBBY, "Could not create a party: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
