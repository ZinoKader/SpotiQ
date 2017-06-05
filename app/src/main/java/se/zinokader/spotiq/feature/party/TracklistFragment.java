package se.zinokader.spotiq.feature.party;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.FragmentTracklistBinding;

public class TracklistFragment extends Fragment {

    FragmentTracklistBinding binding;

    static final String FRAGMENT_TAG = "tracklist_fragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracklist, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    static TracklistFragment getInstance() {
        return new TracklistFragment();
    }

}
