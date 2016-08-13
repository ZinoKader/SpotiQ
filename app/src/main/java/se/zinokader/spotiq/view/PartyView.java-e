package se.zinokader.spotiq.view;

import java.util.ArrayList;

import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.Stickynotification;

public interface PartyView {
    void removePlaylistItem(Song song);
    void addPlaylistItem(Song song);
    void attachPlaylist(ArrayList<Song> songs);
    void updateEmptyView();
    void updateNotificationService(Stickynotification stickynotification);
    void onPlayPauseFabClick();
    void onItemLongClicked(Song song);
    void onItemLongClickEnded();
    void onItemLikeClicked(Song song, Boolean liked);
    void updateSongProgress(int progress);
    void onAddSongFabClick();
    void showSnackbar(String snacktext, int length);
    void showSessionExpired();
    void enablePlayPauseButton();
}
