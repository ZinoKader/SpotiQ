package se.zinokader.spotiq.repository;

import com.github.b3er.rxfirebase.database.RxFirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import io.reactivex.Observable;
import io.reactivex.Single;
import se.zinokader.spotiq.constants.FirebaseConstants;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.User;

public class PartiesRepository {

    private DatabaseReference databaseReference;

    public PartiesRepository(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public Observable<Boolean> createNewParty(Party party) {
        return Observable.create(subscriber -> databaseReference.child(party.getTitle())
                .child(FirebaseConstants.CHILD_PARTYINFO).setValue(party)
                .addOnCompleteListener(task -> {
                    subscriber.onNext(task.isSuccessful());
                    subscriber.onComplete();
                })
                .addOnFailureListener(subscriber::onError));
    }

    public Observable<DataSnapshot> getParty(String partyTitle) {
        return RxFirebaseDatabase.data(databaseReference.child(partyTitle)).toObservable();
    }

    public Observable<Boolean> addUserToParty(User user, String partyTitle) {
        return Observable.create(subscriber -> databaseReference.child(partyTitle)
                .child(FirebaseConstants.CHILD_USERS).child(user.getUserId()).setValue(user)
                .addOnCompleteListener(task -> {
                    subscriber.onNext(task.isSuccessful());
                    subscriber.onComplete();
                })
                .addOnFailureListener(subscriber::onError));
    }

    public Observable<DataSnapshot> getPartyMembers(String partyTitle) {
        return RxFirebaseDatabase.dataChanges(databaseReference.child(partyTitle)
                .child(FirebaseConstants.CHILD_USERS));
    }

    public Single<Boolean> isHostOfParty(String spotifyUserId, String partyTitle) {
        return RxFirebaseDatabase.data(databaseReference.child(partyTitle)
                .child(FirebaseConstants.CHILD_PARTYINFO))
                .map(dbPartySnapshot -> {
                    Party dbParty = dbPartySnapshot.getValue(Party.class);
                    return dbParty.getHostSpotifyId().equals(spotifyUserId);
                });
    }

}
