package se.zinokader.spotiq.feature.party;

import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.User;


public interface PartyView extends BaseView {
    void addPartyMember(User partyMember);
    void setUserDetails(String userName, String userImageUrl);
    void setHostDetails(String hostName);
    void setHostPriviliges();
}
