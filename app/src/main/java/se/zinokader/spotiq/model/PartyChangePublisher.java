package se.zinokader.spotiq.model;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import se.zinokader.spotiq.util.type.Empty;

public class PartyChangePublisher {

    private static final PublishSubject<Song> newTrackPublisher = PublishSubject.create();
    private static final PublishSubject<Empty> removeLatestTrackPublisher = PublishSubject.create();
    private static final PublishSubject<User> newPartyMemberPublisher = PublishSubject.create();
    private static final PublishSubject<User> changedPartyMemberPublisher = PublishSubject.create();

    public PartyChangePublisher() {}

    public PublishSubject<Song> getNewTrackPublisher() {
        return newTrackPublisher;
    }

    public PublishSubject<Empty> getRemoveLatestTrackPublisher() {
        return removeLatestTrackPublisher;
    }

    public PublishSubject<User> getNewPartyMemberPublisher() {
        return newPartyMemberPublisher;
    }

    public PublishSubject<User> getChangedPartyMemberPublisher() {
        return changedPartyMemberPublisher;
    }

    public Observable<Song> observeNewSongs() {
        return newTrackPublisher;
    }

    public Observable<Empty> observeFirstSongFinished() {
        return removeLatestTrackPublisher;
    }

    public Observable<User> observeNewPartyMembers() {
        return newPartyMemberPublisher;
    }

    public Observable<User> observePartyMemberChanges() {
        return changedPartyMemberPublisher;
    }
}
