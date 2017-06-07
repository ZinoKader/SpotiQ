package se.zinokader.spotiq.feature.lobby;

import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;

import se.zinokader.spotiq.feature.base.BaseView;

public interface LobbyView extends BaseView {
    void setPresenter(LobbyPresenter presenter);
    @DistinctUntilChanged
    void setUserDetails(String userName, String userImageUrl);
    void goToParty(String partyTitle);

}
