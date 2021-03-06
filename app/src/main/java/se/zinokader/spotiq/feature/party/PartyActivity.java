package se.zinokader.spotiq.feature.party;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import se.zinokader.spotiq.service.player.SpotiqHostService;
import se.zinokader.spotiq.util.ShortcutUtil;

@RequiresPresenter(PartyPresenter.class)
public class PartyActivity extends BaseActivity<PartyPresenter> implements PartyView {

    ActivityPartyBinding binding;
    private String partyTitle;
    private boolean userDetailsLoaded = false;
    private boolean hostProvilegesLoaded = false;

    private SpotiqHostService hostService;
    private boolean isHostServiceBound = false;

    private ServiceConnection hostServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder serviceBinder) {
            isHostServiceBound = true;
            SpotiqHostService.HostServiceBinder hostServiceBinder =
                (SpotiqHostService.HostServiceBinder) serviceBinder;
            hostService = hostServiceBinder.getService();
            //restart service if service was corrupted
            if (hostService.isPartyInformationMissing()) {
                hostService.stopSelf();
                startHostService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isHostServiceBound = false;
        }
    };

    private void bindHostService() {
        Intent hostServiceIntent = new Intent(this, SpotiqHostService.class);
        bindService(hostServiceIntent, hostServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver playingStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra(ServiceConstants.PLAYING_STATUS_ISPLAYING_EXTRA, false);
            synchronizePlayButton(isPlaying);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_party);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Bundle partyInfo = getIntent().getExtras();
        if (partyInfo != null) {
            partyTitle = partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA);
            getPresenter().setPartyTitle(partyTitle);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.addSearchShortcut(this, partyTitle);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            playingStatusReceiver, new IntentFilter(ServiceConstants.PLAYING_STATUS_BROADCAST_NAME));

        binding.partyTitle.setText(partyTitle);
        binding.searchTransitionSheet.setFab(binding.searchFab);
        binding.searchFab.setOnClickListener(view -> binding.searchTransitionSheet.expandFab());
        binding.searchTransitionSheet.setFabAnimationEndListener(() -> {
            Intent searchActivityIntent = new Intent(this, SearchActivity.class);
            searchActivityIntent.putExtras(partyInfo);
            startActivityForResult(searchActivityIntent, ApplicationConstants.SEARCH_INTENT_REQUEST_CODE);
        });

        binding.bottomBar.setOnTabSelectListener(tabId -> {
            Fragment selectedFragment;
            switch (tabId) {
                default:
                case R.id.tab_tracklist:
                    selectedFragment = TracklistFragment.newInstance(partyTitle);
                    break;
                case R.id.tab_party_members:
                    selectedFragment = PartyMemberFragment.newInstance(partyTitle);
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentHolder, selectedFragment);
            transaction.commit();
        });

        binding.bottomBar.setOnTabReselectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_tracklist:
                    ((TracklistFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentHolder)).scrollToTop();
                    break;
                case R.id.tab_party_members:
                    ((PartyMemberFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentHolder)).scrollToTop();
            }
        });

        binding.playPauseFab.setOnMusicFabClickListener(view -> {
            debouncePlayButton();
            hostService.musicAction();
        });
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.removeAllShortcuts(this);
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        super.startForegroundTokenRenewalService();
        if (hostProvilegesLoaded) {
            bindHostService();
        }
    }

    @Override
    public void onPause() {
        super.stopForegroundTokenRenewalService();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hostProvilegesLoaded) {
            bindHostService();
        }
    }

    @Override
    protected void onStop() {
        if (isHostServiceBound) {
            unbindService(hostServiceConnection);
            isHostServiceBound = false;
        }
        super.onStop();
    }

    /**
     * Set the play/pause button state accordingly to the host service's playing status
     * Delayed to allow the button to play the animation from user input before synchronizing with
     * actual playing status result
     */
    private void synchronizePlayButton(boolean isPlaying) {
        new Handler().postDelayed(() -> {
            //switch to show pause icon
            if (isPlaying && binding.playPauseFab.getCurrentMode().isShowingPlayIcon()) {
                binding.playPauseFab.playAnimation();
            }
            //switch to show play icon
            else if (!isPlaying && !binding.playPauseFab.getCurrentMode().isShowingPlayIcon()) {
                binding.playPauseFab.playAnimation();
            }
        }, ApplicationConstants.PLAY_PAUSE_BUTTON_SYNCHRONIZATION_DELAY_MS);
    }

    private void debouncePlayButton() {
        binding.playPauseFab.setClickable(false);
        new Handler().postDelayed(() -> binding.playPauseFab.setClickable(true), 500);
    }

    @Override
    public void setUserDetails(String userName, String userImageUrl) {
        if (!userDetailsLoaded) {
            binding.userName.setText(userName);
            Glide.with(this)
                .load(userImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
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
    public void setHostPrivileges() {
        if (!hostProvilegesLoaded) {
            binding.playPauseFab.setVisibility(View.VISIBLE);
            bindHostService();
            startHostService();
            hostProvilegesLoaded = true;
        }
    }

    private void startHostService() {
        Intent hostServiceIntent = new Intent(this, SpotiqHostService.class);
        hostServiceIntent.setAction(ServiceConstants.ACTION_INIT);
        hostServiceIntent.putExtra(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        startService(hostServiceIntent);
    }

    private void stopHostService() {
        stopService(new Intent(this, SpotiqHostService.class));
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Confirm exit")
            .setMessage("Are you sure you want to exit the party?")
            .setPositiveButton("Yes", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                if (hostProvilegesLoaded) stopHostService();
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
