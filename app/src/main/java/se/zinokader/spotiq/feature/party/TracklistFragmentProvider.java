package se.zinokader.spotiq.feature.party;

import android.support.v4.app.Fragment;

import com.appolica.tabcontroller.FragmentProvider;

public class TracklistFragmentProvider implements FragmentProvider {

    @Override
    public String getTag() {
        return TracklistFragment.FRAGMENT_TAG;
    }

    @Override
    public Fragment getInstance() {
        return TracklistFragment.getInstance();
    }
}
