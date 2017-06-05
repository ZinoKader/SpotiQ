package se.zinokader.spotiq.feature.party;

import android.support.annotation.IdRes;

import com.roughike.bottombar.OnTabSelectListener;

import se.zinokader.spotiq.R;

public class BottomBarListener implements OnTabSelectListener {

    private BottomBarTabListener listener;

    BottomBarListener(BottomBarTabListener listener) {
        this.listener = listener;
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_tracklist:
                listener.onTracklistTabSelected();
                break;
            case R.id.tab_party_members:
                listener.onPartyMemberTabSelected();
                break;
        }
    }

    public interface BottomBarTabListener {
        void onTracklistTabSelected();
        void onPartyMemberTabSelected();
    }
}

