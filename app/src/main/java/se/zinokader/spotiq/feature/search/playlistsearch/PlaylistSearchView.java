package se.zinokader.spotiq.feature.search.playlistsearch;

import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistSimple;
import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.Song;

public interface PlaylistSearchView extends BaseView {
    void updatePlaylists(List<PlaylistSimple> playlists);
    void updateSongs(List<Song> songs);
}
