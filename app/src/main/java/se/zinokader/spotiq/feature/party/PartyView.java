package se.zinokader.spotiq.feature.party;

import com.spotify.sdk.android.player.Config;

import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.model.User;


public interface PartyView extends BaseView {
    void addPartyMember(User partyMember);
    void setUserDetails(String userName, String userImageUrl);
    void setHostPriviliges();
    Config setupPlayerConfig(String accessToken);
}
