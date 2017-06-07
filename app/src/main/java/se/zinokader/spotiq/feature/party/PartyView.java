package se.zinokader.spotiq.feature.party;

import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;

import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.User;


public interface PartyView extends BaseView {
    void setPresenter(PartyPresenter presenter);
    @DistinctUntilChanged
    void addPartyMember(User partyMember);
    @DistinctUntilChanged
    void setUserDetails(String userName, String userImageUrl);
    @DistinctUntilChanged
    void setHostDetails(String hostName);
    void setHostPriviliges();
}
