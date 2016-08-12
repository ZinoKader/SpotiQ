package se.zinokader.spotiq.presenter;
import com.mukesh.tinydb.TinyDB;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import se.zinokader.spotiq.view.LobbyView;

public interface LobbyPresenter {
    void setView(LobbyView view);
    void detach();
    void setUserId(TinyDB datastore, AuthenticationResponse response);
    void createParty(String partyname, String partypassword, String userid, AuthenticationResponse response);
    void joinParty(String partyname, String partypassword, AuthenticationResponse response);
}
