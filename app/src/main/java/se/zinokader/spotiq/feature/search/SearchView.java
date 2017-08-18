package se.zinokader.spotiq.feature.search;

import java.util.ArrayList;

import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.Song;

public interface SearchView extends BaseView {
    void updateRequestList(ArrayList<Song> songRequests);
    void updateSongRequestsLabel();
    void finishRequest();
}
