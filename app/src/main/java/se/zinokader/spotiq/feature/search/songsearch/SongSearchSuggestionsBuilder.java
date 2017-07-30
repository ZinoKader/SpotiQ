package se.zinokader.spotiq.feature.search.songsearch;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.model.Song;

public class SongSearchSuggestionsBuilder implements SearchSuggestionsBuilder {

    private List<Song> songSuggestions;
    private List<SearchItem> songSearchItems;
    private int maxSuggestionsCount;

    SongSearchSuggestionsBuilder(List<Song> songSuggestions, int maxSuggestionsCount) {
        this.songSuggestions = songSuggestions;
        this.maxSuggestionsCount = maxSuggestionsCount;
        songSearchItems = new ArrayList<>();
    }

    void buildSuggestionItems(Context context) {
        for (Song song : songSuggestions) {
            Glide.with(context)
                .load(song.getAlbumArtUrl())
                .fitCenter()
                .override(ApplicationConstants.MEDIUM_QUALITY_ALBUM_ART_DIMENSION, ApplicationConstants.MEDIUM_QUALITY_ALBUM_ART_DIMENSION)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable albumDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        songSearchItems.add(new SearchItem(song.getName(), song.getName(), SearchItem.TYPE_SEARCH_ITEM_SUGGESTION, albumDrawable));
                    }
                });
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
