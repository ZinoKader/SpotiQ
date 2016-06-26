package se.zinokader.spotiq.view;


/*
--- showSnackbar lengths ---
LENGTH_LONG = 0
LENGTH_SHORT = -1
LENGTH_INDEFENITE = -2
*/

import se.zinokader.spotiq.model.Party;

public interface LobbyView {
    void showSnackbar(String snacktext, int length);
    void showPartyDialog();
    void goToParty(Party party);
}
