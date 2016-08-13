package se.zinokader.spotiq.presenter;

import android.content.Context;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.view.PartyView;

public interface PartyPresenter {
    void setView(PartyView view);
    void detach();
    void inBackground(Boolean inbackground);
    void wentToBackground();
    Party getParty();
    void setParty(Party party);
    void authenticate(Context context, String userid, AuthenticationResponse response);
    Boolean isHost();
    void playOrPauseEvent();
    void previewSong(Song song);
    void pausePreview();
    void setSongLiked(Song song, Boolean liked);
    void startPlaylistListener();
}
