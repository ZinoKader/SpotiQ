package se.zinokader.spotiq.repository;

import com.google.firebase.database.DatabaseReference;

import io.reactivex.Observable;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.model.Song;

public class TracklistRepository {

    private DatabaseReference databaseReference;

    public TracklistRepository(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public Observable<Boolean> addSong(Song song, String partyTitle) {
        return Observable.create(subscriber -> databaseReference
                .child(partyTitle)
                .child(FirebaseConstants.CHILD_TRACKLIST)
                .push()
                .setValue(song)
                .addOnCompleteListener(task -> {
                    subscriber.onNext(task.isSuccessful());
                    subscriber.onComplete();
                })
                .addOnFailureListener(subscriber::onError));
    }

}
