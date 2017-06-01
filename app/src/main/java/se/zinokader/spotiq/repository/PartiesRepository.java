package se.zinokader.spotiq.repository;

import com.github.b3er.rxfirebase.database.RxFirebaseDatabase;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import io.reactivex.Observable;
import se.zinokader.spotiq.model.Party;

public class PartiesRepository {

    private DatabaseReference databaseReference;

    public PartiesRepository(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public Observable<Party> createNewParty(Party party) {
        return Observable.create(subscriber -> databaseReference.child(party.getTitle()).setValue(party)
                .addOnCompleteListener(task -> {
                    subscriber.onNext(party);
                    subscriber.onComplete();
                })
                .addOnFailureListener(task -> subscriber.onError(new FirebaseException(task.getMessage()))));
    }

    public Observable<DataSnapshot> getParty(String partyName) {
        return RxFirebaseDatabase.data(databaseReference.child(partyName)).toObservable();
    }

    public Observable<Boolean> isHostOfParty(String spotifyUserId, String partyName) {
        return RxFirebaseDatabase.data(databaseReference.child(partyName))
                .map(dataSnapshot -> {
                    Party dbParty = (Party) dataSnapshot.getValue();
                    return dbParty.getHostSpotifyId().equals(spotifyUserId);
                })
                .toObservable();
    }

}
