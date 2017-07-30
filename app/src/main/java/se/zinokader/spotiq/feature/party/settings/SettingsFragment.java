package se.zinokader.spotiq.feature.party.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.FragmentSettingsBinding;
import se.zinokader.spotiq.util.di.Injector;

public class SettingsFragment extends Fragment {

    FragmentSettingsBinding binding;

    public static SettingsFragment newInstance(String partyTitle) {
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle newInstanceArguments = new Bundle();
        newInstanceArguments.putString(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        settingsFragment.setArguments(newInstanceArguments);
        return settingsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((Injector) getContext().getApplicationContext()).inject(this);
        super.onCreate(savedInstanceState);
        String partyTitle = getArguments().getString(ApplicationConstants.PARTY_NAME_EXTRA);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(ApplicationConstants.PARTY_NAME_EXTRA, getArguments().getString(ApplicationConstants.PARTY_NAME_EXTRA));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        return binding.getRoot();
    }

}
