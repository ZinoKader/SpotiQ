package se.zinokader.spotiq.repository;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Observable;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.model.ChildEvent;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.User;

public class PartiesRepository {

    private DatabaseReference databaseReference;

    public PartiesRepository(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public Observable<Boolean> createNewParty(Party party) {
        return Observable.create(subscriber -> databaseReference
            .child(party.getTitle())
            .child(FirebaseConstants.CHILD_PARTYINFO)
            .setValue(party)
            .addOnCompleteListener(task -> {
                subscriber.onNext(task.isSuccessful());
                subscriber.onComplete();
            })
            .addOnFailureListener(subscriber::onError));
    }

    public Observable<DataSnapshot> getParty(String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subscriber.onNext(dataSnapshot);
                    subscriber.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            }));
    }

    public Observable<Boolean> addUserToParty(User user, String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_USERS)
            .child(user.getUserId())
            .setValue(user)
            .addOnCompleteListener(task -> {
                subscriber.onNext(task.isSuccessful());
                subscriber.onComplete();
            })
            .addOnFailureListener(subscriber::onError));
    }

    public void incrementUserSongRequestCount(User user, String partyTitle) {
        databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_USERS)
            .child(user.getUserId())
            .child(FirebaseConstants.CHILD_USER_SONGS_REQUESTED)
            .runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.setValue(1);
                    }
                    else {
                        currentData.setValue((Long) currentData.getValue() + 1);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });
    }

    public Observable<ChildEvent> listenToPartyMemberChanges(String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_USERS)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    subscriber.onNext(new ChildEvent(dataSnapshot, ChildEvent.Type.ADDED, previousChildName));
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    subscriber.onNext(new ChildEvent(dataSnapshot, ChildEvent.Type.CHANGED, previousChildName));
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    subscriber.onNext(new ChildEvent(dataSnapshot, ChildEvent.Type.REMOVED));
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    subscriber.onNext(new ChildEvent(dataSnapshot, ChildEvent.Type.MOVED, previousChildName));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            }));
    }

    public Observable<Boolean> isHostOfParty(String spotifyUserId, String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_PARTYINFO)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Party dbParty = dataSnapshot.getValue(Party.class);
                    boolean isHost = dbParty != null && dbParty.getHostSpotifyId().equals(spotifyUserId);
                    subscriber.onNext(isHost);
                    subscriber.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            }));
    }

}
