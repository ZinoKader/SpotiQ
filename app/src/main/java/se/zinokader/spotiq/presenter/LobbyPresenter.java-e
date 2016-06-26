package se.zinokader.spotiq.presenter;
import android.content.Context;
import android.content.Intent;


import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.view.LobbyView;


public interface LobbyPresenter {
    void setView(LobbyView view);
    void detach();
    void setupPlayer(Context context, Intent intent, int resultCode, int requestCode);
    void showPartyDialog();
    void createParty(Party party);
    void playSong(String songURI);
    void pauseSong();
    void nextSong();
}
