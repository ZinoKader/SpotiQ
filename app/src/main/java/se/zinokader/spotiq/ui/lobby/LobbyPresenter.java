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
import se.zinokader.spotiq.constants.FirebaseConstants;
import se.zinokader.spotiq.constants.LogTag;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.model.UserPartyInformation;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.ui.base.BasePresenter;
import se.zinokader.spotiq.util.exception.PartyDoesNotExistException;
import se.zinokader.spotiq.util.exception.PartyExistsException;
import se.zinokader.spotiq.util.exception.PartyNotCreatedException;
import se.zinokader.spotiq.util.exception.PartyWrongPasswordException;
import se.zinokader.spotiq.util.exception.UserNotAddedException;


public class LobbyPresenter extends BasePresenter<LobbyActivity> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

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
                User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                getView().setUserDetails(user.getUserName(), user.getUserImageUrl());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(LogTag.LOG_LOBBY, "Error when getting user data");
                error.printStackTrace();
            }
        });
    }

    void joinParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);

        Observable.zip(
                partiesRepository.getParty(party.getTitle()),
                spotifyRepository.getMe(spotifyCommunicatorService.getWebApi()),
                (dbPartySnapshot, spotifyUser) -> {
                    if (dbPartySnapshot.exists()) {
                        User user = new User(spotifyUser.id, spotifyUser.display_name, spotifyUser.images);
                        boolean userAlreadyExists = dbPartySnapshot.child(FirebaseConstants.CHILD_USERS).hasChild(user.getUserId());
                        Party dbParty = dbPartySnapshot.child(FirebaseConstants.CHILD_PARTYINFO).getValue(Party.class);
                        return new UserPartyInformation(user, userAlreadyExists, dbParty);
                    }
                    else {
                        throw new PartyDoesNotExistException();
                    }
                })
                .map(userPartyInformation -> {
                    if (userPartyInformation.getParty().getPassword().equals(partyPassword)) {
                        if (!userPartyInformation.userAlreadyExists()) { //not important that this is synchronous
                            partiesRepository.addUserToParty(userPartyInformation.getUser(), userPartyInformation.getParty().getTitle()).subscribe();
                        }
                        return userPartyInformation.getParty();
                    }
                    else {
                        throw new PartyWrongPasswordException();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Party>() {
                    @Override
                    public void onNext(Party party) {
                        navigateToParty(party.getTitle());
                    }

                    @Override
                    public void onError(Throwable exception) {
                        if (exception instanceof PartyDoesNotExistException) {
                            getView().showMessage("Party does not exist, why not create it?");
                        }
                        else if (exception instanceof PartyWrongPasswordException) {
                            getView().showMessage("Password incorrect");
                        }
                        else {
                            getView().showMessage("Something went wrong when joining the party");
                        }
                        Log.d(LogTag.LOG_LOBBY, "Could not join party");
                        exception.printStackTrace();
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

        Observable.zip(
                partiesRepository.getParty(party.getTitle()),
                spotifyRepository.getMe(spotifyCommunicatorService.getWebApi()),
                (dbParty, spotifyUser) -> {
                    if (dbParty.exists()) {
                        throw new PartyExistsException();
                    }
                    else {
                        User user = new User(spotifyUser.id, spotifyUser.display_name, spotifyUser.images);
                        party.setHostSpotifyId(user.getUserId());
                        return new UserPartyInformation(user, party);
                    }
                })
                .flatMap(userPartyInformation -> Observable.zip(
                        partiesRepository.createNewParty(userPartyInformation.getParty()),
                        partiesRepository.addUserToParty(userPartyInformation.getUser(), userPartyInformation.getParty().getTitle()),
                        (partyWasCreated, userWasAdded) -> {
                            if (!partyWasCreated) throw new PartyNotCreatedException();
                            if (!userWasAdded) throw new UserNotAddedException();
                            return userPartyInformation.getParty();
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Party>() {
                    @Override
                    public void onNext(Party party) {
                        navigateToParty(party.getTitle());
                    }

                    @Override
                    public void onError(Throwable exception) {
                        if (exception instanceof PartyExistsException) {
                            getView().showMessage("Party " + partyTitle + " already exists");
                        }
                        else if (exception instanceof UserNotAddedException) {
                            getView().showMessage("Something went wrong when adding you to the party");
                        }
                        else {
                            getView().showMessage("Something went wrong when creating the party");
                        }
                        Log.d(LogTag.LOG_LOBBY, "Could not create party");
                        exception.printStackTrace();
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
        Observable.just(ApplicationConstants.SHORT_ACTION_DELAY)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(next -> getView().showMessage("Entering party " + partyTitle + "..."))
                .delay(ApplicationConstants.SHORT_ACTION_DELAY, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(success -> getView().goToParty(partyTitle));

    }
}
