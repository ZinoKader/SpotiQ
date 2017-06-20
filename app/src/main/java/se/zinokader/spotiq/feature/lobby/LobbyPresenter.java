package se.zinokader.spotiq.feature.lobby;

import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.model.UserPartyInformation;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.util.exception.PartyDoesNotExistException;
import se.zinokader.spotiq.util.exception.PartyExistsException;
import se.zinokader.spotiq.util.exception.PartyNotCreatedException;
import se.zinokader.spotiq.util.exception.PartyWrongPasswordException;
import se.zinokader.spotiq.util.exception.UserNotAddedException;


public class LobbyPresenter extends BasePresenter<LobbyView> {

    @Inject
    SpotifyCommunicatorService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    public static final int LOAD_USER_RESTARTABLE_ID = 1918;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        //load user
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()),
            (lobbyView, userPrivate) -> {
                User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                lobbyView.setUserDetails(user.getUserName(), user.getUserImageUrl());
            }, (lobbyView, throwable) -> {
                Log.d(LogTag.LOG_LOBBY, "Error when getting user Spotify data");
                throwable.printStackTrace();
            });

        if (savedState == null) {
            start(LOAD_USER_RESTARTABLE_ID);
        }
    }

    void joinParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);

        Observable.zip(
            partiesRepository.getParty(party.getTitle()),
            spotifyRepository.getMe(spotifyCommunicatorService.getWebApi()),
            (dbPartySnapshot, spotifyUser) -> {
                if (dbPartySnapshot.exists()) {
                    User user = new User(spotifyUser.id, spotifyUser.display_name, spotifyUser.images);
                    user.setJoinedNowTimeStamp();
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
                    view().subscribe(lobbyViewOptionalView -> {
                        if (lobbyViewOptionalView.view != null) {
                            if (exception instanceof PartyDoesNotExistException) {
                                lobbyViewOptionalView.view.showMessage("Party does not exist, why not create it?");
                            }
                            else if (exception instanceof PartyWrongPasswordException) {
                                lobbyViewOptionalView.view.showMessage("Password invalid");
                            }
                            else {
                                lobbyViewOptionalView.view.showMessage("Something went wrong when joining the party");
                            }
                        }
                    }).dispose();
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
                    party.setCreatedNowTimeStamp();
                    party.setHostSpotifyId(user.getUserId());
                    user.setJoinedNowTimeStamp();
                    user.setHasHostPriviliges();
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
                    view().subscribe(lobbyViewOptionalView -> {
                        if (lobbyViewOptionalView.view != null) {
                            if (exception instanceof PartyExistsException) {
                                lobbyViewOptionalView.view.showMessage("Party \" + partyTitle + \" already exists");
                            }
                            else if (exception instanceof UserNotAddedException) {
                                lobbyViewOptionalView.view.showMessage("Something went wrong when adding you to the party");
                            }
                            else {
                               lobbyViewOptionalView.view.showMessage("Something went wrong when creating the party");
                            }
                        }
                    }).dispose();
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
        Observable.just(ApplicationConstants.SHORT_ACTION_DELAY_SEC)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(next -> view().subscribe(lobbyViewOptionalView -> {
               if (lobbyViewOptionalView.view != null) {
                   lobbyViewOptionalView.view.showMessage("Entering party " + partyTitle + "...");
               }
            }).dispose())
            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe(success -> view().subscribe(lobbyViewOptionalView -> {
                if (lobbyViewOptionalView.view != null) {
                    lobbyViewOptionalView.view.goToParty(partyTitle);
                }
            }).dispose());

    }
}
