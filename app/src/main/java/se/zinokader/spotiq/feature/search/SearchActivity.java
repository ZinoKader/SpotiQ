package se.zinokader.spotiq.feature.search;

import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.View;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.ActivitySearchBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.base.BasePresenter;
import se.zinokader.spotiq.feature.base.BaseView;

public class SearchActivity extends BaseActivity<BasePresenter> implements BaseView {

    ActivitySearchBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        Bundle partyInfo = getIntent().getExtras();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        binding.tabHolder.addTab(binding.tabHolder.newTab().setText("Playlists"));
        binding.tabHolder.addTab(binding.tabHolder.newTab().setText("Search"));

        SearchTabPagerAdapter searchTabPagerAdapter = new SearchTabPagerAdapter(getSupportFragmentManager(),
            partyInfo, binding.tabHolder.getTabCount());
        binding.fragmentPager.setAdapter(searchTabPagerAdapter);
        binding.fragmentPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabHolder));
        binding.tabHolder.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.fragmentPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        super.startForegroundTokenRenewalService();
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopForegroundTokenRenewalService();
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }

}
