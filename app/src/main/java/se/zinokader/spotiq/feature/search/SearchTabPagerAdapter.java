package se.zinokader.spotiq.feature.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.search.playlistsearch.PlaylistSearchFragment;
import se.zinokader.spotiq.feature.search.songsearch.SongSearchFragment;

public class SearchTabPagerAdapter extends FragmentStatePagerAdapter {

    private int tabCount;
    private Bundle tabBundle;

    SearchTabPagerAdapter(FragmentManager fm, Bundle tabBundle, int tabCount) {
        super(fm);
        this.tabBundle = tabBundle;
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PlaylistSearchFragment.newInstance(tabBundle.getString(ApplicationConstants.PARTY_NAME_EXTRA));
            case 1:
                return SongSearchFragment.newInstance(tabBundle.getString(ApplicationConstants.PARTY_NAME_EXTRA));
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

}
