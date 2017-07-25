package se.zinokader.spotiq.util.comparator;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.zinokader.spotiq.model.Song;

public class SongSearchSuggestionsBuilder implements SearchSuggestionsBuilder {

    private List<SearchItem> songSearchItems = new ArrayList<>();
    private int maxSuggestionsCount;

    public SongSearchSuggestionsBuilder(List<Song> songSuggestions, int maxSuggestionsCount) {
        buildSearchItems(songSuggestions);
        this.maxSuggestionsCount = maxSuggestionsCount;
    }

    private void buildSearchItems(List<Song> songSuggestions) {
        for (Song song : songSuggestions) {
            songSearchItems.add(new SearchItem(song.getName(), song.getName(), SearchItem.TYPE_SEARCH_ITEM_SUGGESTION));
        }
    }

    @Override
    public Collection<SearchItem> buildEmptySearchSuggestion(int i) {
        if (songSearchItems.size() >= maxSuggestionsCount) {
            return songSearchItems.subList(0, maxSuggestionsCount);
        }
        else {
            return songSearchItems;
        }
    }

    @Override
    public Collection<SearchItem> buildSearchSuggestion(int i, String query) {
        List<SearchItem> filteredSongSearchItems = new ArrayList<>();
        for (SearchItem songSearchItem : songSearchItems) {
            if (songSearchItem.getTitle().toLowerCase().startsWith(query.toLowerCase())) {
                filteredSongSearchItems.add(songSearchItem);
            }
        }
        if (filteredSongSearchItems.size() >= maxSuggestionsCount) {
            return filteredSongSearchItems.subList(0, maxSuggestionsCount);
        }
        else {
            return filteredSongSearchItems;
        }
    }

}
