package se.zinokader.spotiq.feature.lobby;

import se.zinokader.spotiq.feature.base.BaseView;

public interface LobbyView extends BaseView {
    void setUserDetails(String userName, String userImageUrl);
    void goToParty(String partyTitle);

}
