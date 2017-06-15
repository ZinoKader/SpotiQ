package se.zinokader.spotiq.feature.party;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.spotify.sdk.android.player.Config;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;

import java.util.Arrays;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.SpotifyConstants;
import se.zinokader.spotiq.databinding.ActivityPartyBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.party.navigation.PartyViewPagerAdapter;
import se.zinokader.spotiq.feature.party.partymember.PartyMemberFragment;
import se.zinokader.spotiq.feature.party.partymember.PartyMemberFragmentBuilder;
import se.zinokader.spotiq.feature.party.tracklist.TracklistFragment;
import se.zinokader.spotiq.feature.party.tracklist.TracklistFragmentBuilder;
import se.zinokader.spotiq.feature.search.SearchActivity;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.listener.FabListener;

public class PartyActivity extends BaseActivity implements PartyView, FabListener {

    ActivityPartyBinding binding;
    private PartyPresenter presenter;
    private Bundle partyInfo;

    private PartyViewPagerAdapter partyViewPagerAdapter;
    private TracklistFragment tracklistFragment;
    private PartyMemberFragment partyMemberFragment;

    public PartyActivity() {
        addPlugin(new TiActivityPlugin<>(PartyPresenter::new));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_party);
        partyInfo = getIntent().getExtras();
        if (partyInfo != null) {
            binding.partyTitle.setText(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
        }

        postponeEnterTransition();

        partyViewPagerAdapter = new PartyViewPagerAdapter(getSupportFragmentManager());
        tracklistFragment = TracklistFragmentBuilder.newInstance();
        partyMemberFragment = PartyMemberFragmentBuilder.newInstance();

        partyViewPagerAdapter.addFragments(Arrays.asList(tracklistFragment, partyMemberFragment));
        binding.tabPager.setAdapter(partyViewPagerAdapter);

        binding.searchTransitionSheet.setFab(binding.searchFab);
        binding.searchFab.setOnClickListener(view -> binding.searchTransitionSheet.expandFab());
        binding.searchTransitionSheet.setFabAnimationEndListener(() -> {
            Intent searchActivityIntent = new Intent(this, SearchActivity.class);
            searchActivityIntent.putExtras(partyInfo);
            startActivityForResult(searchActivityIntent, ApplicationConstants.SEARCH_INTENT_REQUEST_CODE);
        });

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
        super.startForegroundTokenRenewalService();
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopForegroundTokenRenewalService();
    }

    @Override
    public void setPresenter(TiPresenter presenter) {
        this.presenter = (PartyPresenter) presenter;
        ((Injector) getApplication()).inject(presenter);
        this.presenter.setPartyTitle(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
        this.presenter.init();
    }

    @Override
    public boolean isPresenterAttached() {
        return presenter != null;
    }

    @Override
    public void setUserDetails(String userName, String userImageUrl) {
        binding.userName.setText(userName);
        Glide.with(this)
            .load(userImageUrl)
            .placeholder(R.drawable.image_profile_placeholder)
            .dontAnimate()
            .dontTransform()
            .listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    startPostponedEnterTransition();
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    startPostponedEnterTransition();
                    return false;
                }
            })
            .into(binding.userImage);
    }

    @Override
    public void addSong(Song song) {
        tracklistFragment.addSong(song);
    }

    @Override
    public void addPartyMember(User partyMember) {
        partyMemberFragment.addMember(partyMember);
    }

    @Override
    public void changePartyMember(User partyMember) {
        partyMemberFragment.changePartyMember(partyMember);
    }

    @Override
    public void setHostPriviliges() {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Confirm exit")
            .setMessage("Are you sure you want to exit the party?")
            .setPositiveButton("Yes", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                super.onBackPressed();
            })
            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
            .create()
            .show();
    }

    @Override
    public Config setupPlayerConfig(String accessToken) {
        return new Config(this, accessToken, SpotifyConstants.CLIENT_ID);
    }

    @Override
    public void showFab() {
        binding.searchFab.show();
    }

    @Override
    public void hideFab() {
        binding.searchFab.hide();
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ApplicationConstants.SEARCH_INTENT_REQUEST_CODE) {
            binding.searchTransitionSheet.contractFab();
        }
    }

}
