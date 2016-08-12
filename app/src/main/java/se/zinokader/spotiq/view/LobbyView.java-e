package se.zinokader.spotiq.view;


import com.spotify.sdk.android.authentication.AuthenticationResponse;

import se.zinokader.spotiq.model.Party;

public interface LobbyView {
    void showSnackbar(String snacktext, int length);
    void showSetUserInformationPrompt();
    void showCreatePartyDialog();
    void showJoinPartyDialog();
    void goToParty(Party party, AuthenticationResponse response);
    void setBeerProgress(int progress);
}
