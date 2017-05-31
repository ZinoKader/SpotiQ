package se.zinokader.spotiq.repository;

import com.google.firebase.FirebaseException;
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

    public Observable getParty(Party party) {
        return null;
    }

}
