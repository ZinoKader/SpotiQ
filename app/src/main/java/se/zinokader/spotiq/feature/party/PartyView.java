package se.zinokader.spotiq.feature.party;

import com.spotify.sdk.android.player.Config;

import se.zinokader.spotiq.feature.base.BaseView;


public interface PartyView extends BaseView {
    void setUserDetails(String userName, String userImageUrl);
    void setHostPriviliges();
    Config setupPlayerConfig(String accessToken);
}
