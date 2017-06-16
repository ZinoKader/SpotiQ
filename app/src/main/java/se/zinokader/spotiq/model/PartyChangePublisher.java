package se.zinokader.spotiq.model;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import se.zinokader.spotiq.util.type.Ignore;

public class PartyChangePublisher {

    private static final PublishSubject<Song> newTrackPublisher = PublishSubject.create();
    private static final PublishSubject<Ignore> removeLatestTrackPublisher = PublishSubject.create();
    private static final PublishSubject<User> newPartyMemberPublisher = PublishSubject.create();
    private static final PublishSubject<User> changedPartyMemberPublisher = PublishSubject.create();

    public PartyChangePublisher() {}

    public PublishSubject<Song> getNewTrackPublisher() {
        return newTrackPublisher;
    }

    public PublishSubject<Ignore> getRemoveLatestTrackPublisher() {
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

    public Observable<Ignore> observeFirstSongFinished() {
        return removeLatestTrackPublisher;
    }

    public Observable<User> observeNewPartyMembers() {
        return newPartyMemberPublisher;
    }

    public Observable<User> observePartyMemberChanges() {
        return changedPartyMemberPublisher;
    }
}
