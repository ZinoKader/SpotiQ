package se.zinokader.spotiq.ui.lobby;

import android.os.Bundle;

import javax.inject.Inject;

import se.zinokader.spotiq.service.SpotifyService;
import se.zinokader.spotiq.ui.base.BasePresenter;


public class LobbyPresenter extends BasePresenter<LobbyActivity> {

    @Inject
    SpotifyService spotifyService;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    void joinParty(String partyTitle, String partyPassword) {

    }

    void createParty(String partyTitle, String partyPassword) {

    }
}
