package se.zinokader.spotiq.presenter;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Playlist;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.view.SearchView;

public interface SearchPresenter {
    void setView(SearchView view);
    void detach();
    void setResponse(AuthenticationResponse response);
    void setParty(Party party);
    void setUser(User user);
    Party getParty();
    void searchTracks(String querytext);
    void songSelected(Song song);
    void searchPlaylist(Playlist playlist);
    void populateUserPlaylists();
    void previewSong(Song song);
    void pausePreview();
}
