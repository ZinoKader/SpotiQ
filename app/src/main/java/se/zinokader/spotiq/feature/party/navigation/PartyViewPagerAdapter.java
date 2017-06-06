package se.zinokader.spotiq.feature.party.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PartyViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments = new ArrayList<>();

    public PartyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragments(List<Fragment> fragments) {
        this.fragments.addAll(fragments);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}
