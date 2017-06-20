package se.zinokader.spotiq.feature.party.tracklist;

import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.Song;

public interface TracklistView extends BaseView {
    void addSong(Song song);
    void removeSong(Song song);
}
