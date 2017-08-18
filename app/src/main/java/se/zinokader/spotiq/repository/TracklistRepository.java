package se.zinokader.spotiq.repository;

import android.support.v4.util.Pair;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Observable;
import io.reactivex.Single;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.constant.LogTag;
import se.zinokader.spotiq.model.ChildEvent;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.exception.EmptyTracklistException;

public class TracklistRepository {

    private DatabaseReference databaseReference;

    public TracklistRepository(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public Observable<Pair<Song, Boolean>> checkSongInDbPlaylist(Song song, String partyTitle) {
        return Observable.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                        Song dbSong = songSnapshot.getValue(Song.class);
                        if (dbSong == null) continue;
                        if (song.getSongSpotifyId().equals(dbSong.getSongSpotifyId())) {
                            subscriber.onNext(new Pair<>(song, true));
                            subscriber.onComplete();
                            break;
                        }
                    }
                    subscriber.onNext(new Pair<>(song, false));
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
            .addOnCompleteListener(task -> subscriber.onNext(task.isSuccessful()))
            .addOnFailureListener(subscriber::onError));
    }

    public Single<Song> getFirstSong(String partyTitle) {
        return Single.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
            .orderByKey()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildren().iterator().hasNext()) {
                        subscriber.onSuccess(dataSnapshot.getChildren().iterator().next().getValue(Song.class));
                    }
                    else {
                        subscriber.onError(new EmptyTracklistException());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            }));
    }

    public Single<Boolean> removeFirstSong(String partyTitle) {
        return Single.create(subscriber -> getFirstSongKey(partyTitle)
            .subscribe(songKey -> databaseReference
                    .child(partyTitle)
                    .child(FirebaseConstants.CHILD_TRACKLIST)
                    .child(songKey)
                    .removeValue()
                    .addOnSuccessListener(removeSuccess -> subscriber.onSuccess(true))
                    .addOnFailureListener(failedRemoveException -> subscriber.onSuccess(false)),
                subscriber::onError));
    }

    private Single<String> getFirstSongKey(String partyTitle) {
        return Single.create(subscriber -> databaseReference
            .child(partyTitle)
            .child(FirebaseConstants.CHILD_TRACKLIST)
            .orderByKey()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildren().iterator().hasNext()) {
                        subscriber.onSuccess(dataSnapshot.getChildren().iterator().next().getKey());
                    }
                    else {
                        subscriber.onError(new EmptyTracklistException());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
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
