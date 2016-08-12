package se.zinokader.spotiq.view;

import java.util.ArrayList;

import se.zinokader.spotiq.model.Playlist;
import se.zinokader.spotiq.model.Song;

public interface SearchView {
    void updateSearchList(ArrayList<Song> songarray);
    void updatePlaylists(ArrayList<Playlist> playlistarray);
    void showProgressBar();
    void hideProgressBar();
    void showSnackbar(String snacktext, int length);
    void retryWithSnackbar(String snacktext, String actiontext, String querytext, int length);
    void finish();
}
