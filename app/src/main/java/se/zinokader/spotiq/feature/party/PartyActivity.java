package se.zinokader.spotiq.feature.party;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;

import java.util.Arrays;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivityPartyBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.party.navigation.PartyViewPagerAdapter;
import se.zinokader.spotiq.feature.party.partymembers.PartyMembersFragment;
import se.zinokader.spotiq.feature.party.partymembers.PartyMembersFragmentBuilder;
import se.zinokader.spotiq.feature.party.tracklist.TracklistFragment;
import se.zinokader.spotiq.feature.party.tracklist.TracklistFragmentBuilder;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.helper.GlideRequestOptions;

public class PartyActivity extends BaseActivity implements PartyView {

    ActivityPartyBinding binding;
    private PartyPresenter presenter;
    private Bundle partyInfo;

    private PartyViewPagerAdapter partyViewPagerAdapter;
    private TracklistFragment tracklistFragment;
    private PartyMembersFragment partyMembersFragment;

    public PartyActivity() {
        addPlugin(new TiActivityPlugin<>(PartyPresenter::new));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_party);
        partyInfo = getIntent().getExtras();
        if(partyInfo != null) {
            binding.partyTitle.setText(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
        }

        partyViewPagerAdapter = new PartyViewPagerAdapter(getSupportFragmentManager());
        tracklistFragment = TracklistFragmentBuilder.newInstance();
        partyMembersFragment = PartyMembersFragmentBuilder.newInstance();
        partyViewPagerAdapter.addFragments(Arrays.asList(tracklistFragment, partyMembersFragment));
        binding.tabPager.setAdapter(partyViewPagerAdapter);

        binding.bottomBar.setOnTabSelectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_tracklist:
                    binding.tabPager.setCurrentItem(ApplicationConstants.TAB_TRACKLIST_INDEX);
                    break;
                case R.id.tab_party_members:
                    binding.tabPager.setCurrentItem(ApplicationConstants.TAB_PARTY_MEMBERS_INDEX);
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startForegroundTokenRenewalService();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopForegroundTokenRenewalService();
    }

    @Override
    public void setPresenter(PartyPresenter presenter) {
        this.presenter = presenter;
        ((Injector) getApplication()).inject(presenter);
        presenter.setPartyName(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
    }

    public void updatePartyDetails() {

    }

    public void setUserDetails(String userName, String userImageUrl) {
        binding.userName.setText(userName);
        Glide.with(this)
                .load(userImageUrl)
                .apply(GlideRequestOptions.getProfileImageOptions())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(binding.userImage);
    }

    public void addPartyMember(User member) {
        partyMembersFragment.addMember(member);
    }

    public void setHostDetails(String hostName) {

    }

    public void setHostPriviliges() {

    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }
}