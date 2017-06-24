package se.zinokader.spotiq.repository;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Observable;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.model.ChildEvent;
import se.zinokader.spotiq.model.Song;

public class TracklistRepository {

    private DatabaseReference databaseReference;

    public TracklistRepository(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public Observable<Boolean> checkSongInDbPlaylist(Song song, String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                        Song dbSong = songSnapshot.getValue(Song.class);
                        if (song.getSongSpotifyId().equals(dbSong.getSongSpotifyId())) {
                            subscriber.onNext(true);
                            subscriber.onComplete();
                            break;
                        }
                    }
                    subscriber.onNext(false);
                    subscriber.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(LogTag.LOG_TRACKLIST_REPOSITORY, "Something went wrong when checking " +
                        "if song exists in db: " + databaseError.getMessage());
                    databaseError.toException().printStackTrace();
                }
            }));
    }

    public Observable<Boolean> addSong(Song song, String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
            .push() //push is timestamp-based, items are chronologically ordered
            .setValue(song)
            .addOnCompleteListener(task -> {
                subscriber.onNext(task.isSuccessful());
                subscriber.onComplete();
            })
            .addOnFailureListener(subscriber::onError));
    }

    public Observable<Boolean> removeFirstSong(String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
            .orderByKey()
            .limitToFirst(1)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    dataSnapshot.getRef().removeValue((databaseError, databaseReference) -> {
                        subscriber.onNext(true);
                        subscriber.onComplete();
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onNext(false);
                    subscriber.onComplete();
                }
            }));
    }

    public Observable<ChildEvent> listenToTracklistChanges(String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
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

}
