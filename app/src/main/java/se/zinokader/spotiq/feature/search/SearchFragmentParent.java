package se.zinokader.spotiq.feature.search;

import se.zinokader.spotiq.model.Song;

public interface SearchFragmentParent {
    void addRequest(Song song);
    void removeRequest(Song song);
}
