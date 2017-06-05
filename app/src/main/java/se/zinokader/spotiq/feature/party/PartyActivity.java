package se.zinokader.spotiq.feature.party;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.appolica.tabcontroller.TabController;
import com.appolica.tabcontroller.fragment.TabControllerFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import icepick.State;
import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivityPartyBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.util.helper.GlideRequestOptions;

@RequiresPresenter(PartyPresenter.class)
public class PartyActivity extends BaseActivity<PartyPresenter> implements BottomBarListener.BottomBarTabListener {

    ActivityPartyBinding binding;
    private TabController tabController;
    private TracklistFragmentProvider tracklistFragmentProvider = new TracklistFragmentProvider();

    @State
    Bundle partyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_party);
        binding.setPresenter(getPresenter());
        partyInfo = getIntent().getExtras();
        if(partyInfo != null) {
            getPresenter().setPartyName(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
        }

        TabControllerFragment tabControllerFragment =
                (TabControllerFragment) getSupportFragmentManager().findFragmentById(R.id.tabFragmentContainer);
        tabController = tabControllerFragment.getTabController();

        binding.bottomBar.setOnTabSelectListener(new BottomBarListener(this), true);

        getPresenter().loadParty();
        getPresenter().loadUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().resume();
    }

    @Override
    protected void onPause() {
        getPresenter().pause();
        super.onPause();
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

    public void setHostDetails(String hostName) {

    }

    public void setHostPriviliges() {

    }

    @Override
    public void onTracklistTabSelected() {
        tabController.switchTo(tracklistFragmentProvider);
    }

    @Override
    public void onPartyMemberTabSelected() {

    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }

}
