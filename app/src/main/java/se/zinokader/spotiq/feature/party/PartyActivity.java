package se.zinokader.spotiq.feature.party;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.constant.ServiceConstants;
import se.zinokader.spotiq.databinding.ActivityPartyBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.party.partymember.PartyMemberFragment;
import se.zinokader.spotiq.feature.party.tracklist.TracklistFragment;
import se.zinokader.spotiq.feature.search.SearchActivity;
import se.zinokader.spotiq.service.SpotiqPlayerService;
import se.zinokader.spotiq.util.ShortcutUtil;
import se.zinokader.spotiq.util.listener.FabListener;

@RequiresPresenter(PartyPresenter.class)
public class PartyActivity extends BaseActivity<PartyPresenter> implements PartyView, FabListener {

    ActivityPartyBinding binding;
    private String partyTitle;
    private boolean userDetailsLoaded = false;
    private boolean displayHostControls = false;

    private Fragment selectedFragment;
    private SelectedTab selectedTab = SelectedTab.TRACKLIST_TAB;
    private enum SelectedTab {TRACKLIST_TAB, PARTY_MEMBERS_TAB}

    private SpotiqPlayerService playerService;
    private boolean playerServiceBound = false;

    private ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder serviceBinder) {
            playerServiceBound = true;
            SpotiqPlayerService.PlayerServiceBinder playerServiceBinder =
                (SpotiqPlayerService.PlayerServiceBinder) serviceBinder;
            playerService = playerServiceBinder.getService();

            //set the play/pause button state accordingly to the player service's playing status
            if (playerService.isPlaying()) { //switch to play icon
                if (!binding.playPauseFab.getCurrentMode().isShowingPlayIcon()) {
                    binding.playPauseFab.playAnimation();
                }
            }
            else { //switch to pause icon
                if (!binding.playPauseFab.getCurrentMode().isShowingPlayIcon()) {
                    binding.playPauseFab.playAnimation();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            playerServiceBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_party);

        Bundle partyInfo = getIntent().getExtras();
        if (partyInfo != null) {
            partyTitle = partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA);
            getPresenter().setPartyTitle(partyTitle);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.addSearchShortcut(this, partyTitle);
        }

        supportPostponeEnterTransition();

        binding.partyTitle.setText(partyTitle);
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
                    selectedFragment = TracklistFragment.newInstance(partyTitle);
                    selectedTab = SelectedTab.TRACKLIST_TAB;
                    showControls();
                    break;
                case R.id.tab_party_members:
                    selectedFragment = PartyMemberFragment.newInstance(partyTitle);
                    selectedTab = SelectedTab.PARTY_MEMBERS_TAB;
                    hideControls();
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentHolder, selectedFragment);
            transaction.commit();
        });

        binding.bottomBar.setOnTabReselectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_tracklist:
                    ((TracklistFragment) selectedFragment).scrollToTop();
                    break;
                case R.id.tab_party_members:
                    ((PartyMemberFragment) selectedFragment).scrollToTop();
            }
        });

        binding.playPauseFab.setOnMusicFabClickListener(view -> {
            if (playerService.isPlaying()) {
                playerService.pause();
            }
            else {
                playerService.play();
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.removeAllShortcuts(this);
        }
        if (playerServiceBound) {
            unbindService(playerServiceConnection);
            playerServiceBound = false;
        }
        super.onDestroy();
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
    public void showControls() {
        if (!binding.searchFab.isShown() && selectedTab.equals(SelectedTab.TRACKLIST_TAB)) {
            binding.searchFab.show();
            if (displayHostControls) binding.playPauseFab.show();
        }
    }

    @Override
    public void hideControls() {
        binding.searchFab.hide();
        binding.playPauseFab.hide();
    }

    @Override
    public void setUserDetails(String userName, String userImageUrl) {
        if (!userDetailsLoaded) {
            binding.userName.setText(userName);
            Glide.with(this)
                .load(userImageUrl)
                .placeholder(R.drawable.image_profile_placeholder)
                .dontAnimate()
                .dontTransform()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(binding.userImage);
            userDetailsLoaded = true;
        }
    }

    @Override
    public void setHostPriviliges() {
        displayHostControls = true;
        binding.playPauseFab.setVisibility(View.VISIBLE);
        Intent playerServiceIntent = new Intent(this, SpotiqPlayerService.class);
        bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        Intent initPlayerServiceIntent = new Intent(this, SpotiqPlayerService.class);
        initPlayerServiceIntent.setAction(ServiceConstants.ACTION_INIT);
        initPlayerServiceIntent.putExtra(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        startService(initPlayerServiceIntent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Confirm exit")
            .setMessage("Are you sure you want to exit the party?")
            .setPositiveButton("Yes", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                unbindService(playerServiceConnection);
                playerServiceBound = false;
                stopService(new Intent(this, SpotiqPlayerService.class));
                super.onBackPressed();
            })
            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
            .create()
            .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ApplicationConstants.SEARCH_INTENT_REQUEST_CODE) {
            binding.searchTransitionSheet.contractFab();
        }
    }

    @Override
    public View getRootView() {
        return binding.coordinatorContainer;
    }

}
