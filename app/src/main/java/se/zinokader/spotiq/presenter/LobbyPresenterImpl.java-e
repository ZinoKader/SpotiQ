package se.zinokader.spotiq.presenter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.tinydb.TinyDB;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import kaaes.spotify.webapi.android.models.UserPrivate;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.zinokader.spotiq.constants.Constants;
import se.zinokader.spotiq.misc.BeerProgress;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.spotify.SpotifyWebAPIHelper;
import se.zinokader.spotiq.view.LobbyView;


public class LobbyPresenterImpl implements LobbyPresenter, Constants {

    private LobbyView view;

    @Override
    public void setView(LobbyView view) {
        this.view = view;
    }

    @Override
    public void detach() {
    }

    @Override
    public void setUserId(final TinyDB datastore, AuthenticationResponse response) {
        SpotifyWebAPIHelper spotifywebapihelper = new SpotifyWebAPIHelper(response.getAccessToken());
        spotifywebapihelper.getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserPrivate>() {
                    @Override
                    public void onNext(UserPrivate userinformation) {
                        datastore.putString(Constants.USER_ID, userinformation.id);
                    }
                    @Override
                    public void onCompleted() {
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @Override
    public void createParty(String partyname, String partypassword, String userid, final AuthenticationResponse response) {

        final Party party = new Party();
        party.setPartyName(partyname.trim());
        party.setPartyPassword(partypassword.trim());
        party.setPartyHost(userid);

        final BeerProgress beerprogress = new BeerProgress();

        if(party.getPartyName().length() >= 3 && party.getPartyPassword().length() >= 3 && party.getPartyName().length() <= 16) {

            beerprogress.startPouring(view);

            view.showSnackbar("Creating \"" + party.getPartyName() + "\"...", SNACKBAR_LENGTH_LONG);
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(party.getPartyName());

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    beerprogress.stopPouring(view);

                    if(dataSnapshot.hasChild(party.getPartyName())) {
                        view.showSnackbar("Party already exists, try joining it instead", SNACKBAR_LENGTH_LONG);
                    }
                    else {
                        partyReference.setValue(party, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                view.goToParty(party, response);
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    beerprogress.stopPouring(view);
                }
            });

        } else if(party.getPartyName().length() >= 16) {
            view.showSnackbar("Failed to create party: name too long", SNACKBAR_LENGTH_LONG);
        } else {
            view.showSnackbar("Failed to create party: name or password too short", SNACKBAR_LENGTH_LONG);
        }

    }

    @Override
    public void joinParty(String partyname, String partypassword, final AuthenticationResponse response) {

        final String partynamesanitized = partyname.trim();
        final String partypasswordsanitized = partypassword.trim();

        final BeerProgress beerprogress = new BeerProgress();

        if(partynamesanitized.length() >= 3 && partypasswordsanitized.length() >= 3) {

            beerprogress.startPouring(view);

            view.showSnackbar("Joining \"" + partynamesanitized + "\"...", SNACKBAR_LENGTH_LONG);
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(partynamesanitized);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild(partynamesanitized)) {
                        partyReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                beerprogress.stopPouring(view);

                                Party dbparty = new Party();
                                dbparty.setPartyName(dataSnapshot.child("partyName").getValue().toString());
                                dbparty.setPartyPassword(dataSnapshot.child("partyPassword").getValue().toString());

                                if(dbparty.getPartyName().contentEquals(partynamesanitized) && dbparty.getPartyPassword().contentEquals(partypasswordsanitized)) {
                                    view.goToParty(dbparty, response);
                                } else {
                                    view.showSnackbar("Invalid password", SNACKBAR_LENGTH_LONG);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                beerprogress.stopPouring(view);
                            }
                        });
                    } else {
                        beerprogress.stopPouring(view);
                        view.showSnackbar("Party doesn't exist, try creating it instead", SNACKBAR_LENGTH_LONG);
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    beerprogress.stopPouring(view);
                }
            });

        } else {
            view.showSnackbar("Failed to join party: name or password too short", SNACKBAR_LENGTH_LONG);
        }

    }


}
