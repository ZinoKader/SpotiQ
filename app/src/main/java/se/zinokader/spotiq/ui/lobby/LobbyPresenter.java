package se.zinokader.spotiq.ui.lobby;

import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.TimeUnit;

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
import se.zinokader.spotiq.constants.ApplicationConstants;
import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.ui.base.BasePresenter;
import se.zinokader.spotiq.util.exception.PartyDoesNotExistException;
import se.zinokader.spotiq.util.exception.PartyExistsException;


public class LobbyPresenter extends BasePresenter<LobbyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

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
        Observable.just(party)
                .flatMap(partyExists -> partiesRepository.getParty(partyTitle).blockingFirst().exists()
                        ? Observable.just(partyExists)
                        : Observable.error(new PartyDoesNotExistException()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(ApplicationConstants.RETRY_CONNECTION_FREQUENCY)
                .subscribe(new Observer<Party>() {
                    @Override
                    public void onNext(Party party) {
                        navigateToParty(party.getTitle());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof PartyDoesNotExistException) {
                            getView().showMessage("Party does not exist, why not create one instead?");
                        }
                        else {
                            getView().showMessage("Something went wrong when joining the party");
                        }
                        Log.d(LogTag.LOG_LOBBY, "Could not join party: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                });
    }

    void createParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);
        Observable.just(party)
                .flatMap(partyWithoutId -> {
                    partyWithoutId.setHostSpotifyId(spotifyCommunicatorService.getWebApi().getMe().id);
                    return partiesRepository.getParty(partyTitle).blockingFirst().exists()
                            ? Observable.error(new PartyExistsException())
                            : partiesRepository.createNewParty(partyWithoutId);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(ApplicationConstants.RETRY_CONNECTION_FREQUENCY)
                .subscribe(new Observer<Party>() {
                    @Override
                    public void onNext(Party party) {
                        navigateToParty(party.getTitle());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof PartyExistsException) {
                            getView().showMessage("Party " + partyTitle + " already exists");
                        }
                        else {
                            getView().showMessage("Something went wrong when creating the party");
                        }
                        Log.d(LogTag.LOG_LOBBY, "Could not create a party: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                });

    }

    private void navigateToParty(String partyTitle) {
        Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(next -> {
                    getView().showMessage("Navigating to party " + partyTitle);
                })
                .delay(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    getView().goToParty(partyTitle);
                });
    }
}
