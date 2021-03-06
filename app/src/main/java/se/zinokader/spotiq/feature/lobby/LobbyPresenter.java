package se.zinokader.spotiq.feature.lobby;

import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.util.VersionUtil;
import se.zinokader.spotiq.util.exception.PartyDoesNotExistException;
import se.zinokader.spotiq.util.exception.PartyExistsException;
import se.zinokader.spotiq.util.exception.PartyNotCreatedException;
import se.zinokader.spotiq.util.exception.PartyVersionHigherException;
import se.zinokader.spotiq.util.exception.PartyVersionLowerException;
import se.zinokader.spotiq.util.exception.PartyWrongPasswordException;
import se.zinokader.spotiq.util.exception.UserNotAddedException;


public class LobbyPresenter extends BasePresenter<LobbyView> {

    @Inject
    SpotifyAuthenticationService spotifyCommunicatorService;

    @Inject
    PartiesRepository partiesRepository;

    @Inject
    SpotifyRepository spotifyRepository;

    static final int LOAD_USER_RESTARTABLE_ID = 1918;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        //load user
        restartableLatestCache(LOAD_USER_RESTARTABLE_ID,
            () -> spotifyRepository.getMe(spotifyCommunicatorService.getWebApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable -> throwable.delay(ApplicationConstants.REQUEST_RETRY_DELAY_SEC, TimeUnit.SECONDS)),
            (lobbyView, userPrivate) -> {
                User user = new User(userPrivate.id, userPrivate.display_name, userPrivate.images);
                lobbyView.setUserDetails(user.getUserName(), user.getUserImageUrl());
            }, (lobbyView, throwable) -> {
                Log.d(LogTag.LOG_LOBBY, "Error when getting user Spotify data");
            });

        start(LOAD_USER_RESTARTABLE_ID);
    }

    void joinParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);

        add(Observable.zip(
            partiesRepository.getParty(party.getTitle()),
            spotifyRepository.getMe(spotifyCommunicatorService.getWebApi()),
            (dbPartySnapshot, spotifyUser) -> {
                if (!dbPartySnapshot.exists()) throw new PartyDoesNotExistException();
                Party dbParty = dbPartySnapshot.child(FirebaseConstants.CHILD_PARTYINFO).getValue(Party.class);
                if (dbParty.getPartyVersionCode() > VersionUtil.getCurrentAppVersionCode()) {
                    throw new PartyVersionHigherException();
                }
                else if (dbParty.getPartyVersionCode() < VersionUtil.getCurrentAppVersionCode()) {
                    throw new PartyVersionLowerException();
                }
                User user = new User(spotifyUser.id, spotifyUser.display_name, spotifyUser.images);
                user.setJoinedNowTimeStamp();
                boolean userAlreadyExists = dbPartySnapshot.child(FirebaseConstants.CHILD_USERS).hasChild(user.getUserId());
                return new UserPartyInformation(user, userAlreadyExists, dbParty);

            })
            .flatMap(userPartyInformation -> {
                if (!userPartyInformation.getParty().getPassword().equals(partyPassword)) throw new PartyWrongPasswordException();
                if (userPartyInformation.userAlreadyExists()) {
                    return Observable.just(true);
                }
                else {
                    return partiesRepository.addUserToParty(userPartyInformation.getParty().getTitle(), userPartyInformation.getUser());
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(lobbyViewPartyDelivery -> lobbyViewPartyDelivery.split(
                (lobbyView, userWasAdded) -> {
                    navigateToParty(partyTitle);
                },
                (lobbyView, exception) -> {
                    if (exception instanceof PartyDoesNotExistException) {
                        lobbyView.showMessage("Party does not exist, why not create it?");
                    }
                    else if (exception instanceof PartyWrongPasswordException) {
                        lobbyView.showMessage("Invalid password");
                    }
                    else if (exception instanceof PartyVersionHigherException) {
                        lobbyView.showMessage("This party was created with a newer version of SpotiQ");
                    }
                    else if(exception instanceof PartyVersionLowerException) {
                        lobbyView.showMessage("This party was created with an older version of SpotiQ");
                    }
                    else {
                        lobbyView.showMessage("Something went wrong when joining the party");
                    }
                    Log.d(LogTag.LOG_LOBBY, "Could not join party");
                })));
    }

    void createParty(String partyTitle, String partyPassword) {
        Party party = new Party(partyTitle, partyPassword);

        add(Observable.zip(
            partiesRepository.getParty(party.getTitle()),
            spotifyRepository.getMe(spotifyCommunicatorService.getWebApi()),
            (dbParty, spotifyUser) -> {
                if (dbParty.exists()) {
                    throw new PartyExistsException();
                }
                User user = new User(spotifyUser.id, spotifyUser.display_name, spotifyUser.images);
                party.setCreatedNowTimeStamp();
                party.setPartyVersionCode(VersionUtil.getCurrentAppVersionCode());
                party.setHostSpotifyId(user.getUserId());
                party.setHostMarket(spotifyUser.country);
                user.setJoinedNowTimeStamp();
                user.setHasHostPrivileges();
                return new UserPartyInformation(user, party);
            })
            .flatMap(userPartyInformation -> Observable.zip(
                partiesRepository.createNewParty(userPartyInformation.getParty()),
                partiesRepository.addUserToParty(userPartyInformation.getParty().getTitle(), userPartyInformation.getUser()),
                (partyWasCreated, userWasAdded) -> {
                    if (!partyWasCreated) throw new PartyNotCreatedException();
                    if (!userWasAdded) throw new UserNotAddedException();
                    return userPartyInformation.getParty();
                }))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .subscribe(lobbyViewPartyDelivery -> lobbyViewPartyDelivery.split(
                (lobbyView, confirmedParty) -> {
                    navigateToParty(confirmedParty.getTitle());
                },
                (lobbyView, exception) -> {
                    if (exception instanceof PartyExistsException) {
                        lobbyView.showMessage("Party " + partyTitle + " already exists");
                    }
                    else if (exception instanceof UserNotAddedException) {
                        lobbyView.showMessage("Something went wrong when adding you to the party");
                    }
                    else {
                        lobbyView.showMessage("Something went wrong when creating the party");
                    }
                    Log.d(LogTag.LOG_LOBBY, "Could not create party");
                })));

    }

    private void navigateToParty(String partyTitle) {
        Observable.just(ApplicationConstants.SHORT_ACTION_DELAY_SEC)
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.deliverFirst())
            .doOnNext(firstDelayDelivery -> firstDelayDelivery.split((lobbyView, integer) -> {
                lobbyView.showMessage("Entering party " + partyTitle + "...");
            }, (lobbyView, throwable) -> {
            }))
            .delay(ApplicationConstants.SHORT_ACTION_DELAY_SEC, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe(secondDelayDelivery -> secondDelayDelivery.split((lobbyView, integer) -> {
                lobbyView.goToParty(partyTitle);
            }, (lobbyView, throwable) -> {
            }));

    }
}
