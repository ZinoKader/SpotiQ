package se.zinokader.spotiq.feature.search;

import java.util.List;

import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.Song;

public interface SearchView extends BaseView {
    void updateSearch(List<Song> songs, boolean shouldClear);
}
