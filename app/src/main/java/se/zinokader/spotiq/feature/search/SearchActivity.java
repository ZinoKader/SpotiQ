package se.zinokader.spotiq.feature.search;

import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;
import com.rw.keyboardlistener.KeyboardUtils;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivitySearchBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.model.Song;

@RequiresPresenter(SearchPresenter.class)
public class SearchActivity extends BaseActivity<SearchPresenter> implements SearchView, SearchFragmentParent {

    ActivitySearchBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private SongRequestArrayAdapter songRequestArrayAdapter;
    private PublishSubject<Song> removeFromRequestsPublisher = PublishSubject.create();
    private ArrayList<Song> songRequests;

    private CompositeDisposable subscriptions = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.songRequestsBottomSheet);
        Bundle partyInfo = getIntent().getExtras();
        songRequests = new ArrayList<>();

        if (partyInfo != null) {
            getPresenter().setPartyTitle(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //setup viewpager and tabs
        binding.tabHolder.addTab(binding.tabHolder.newTab());
        binding.tabHolder.addTab(binding.tabHolder.newTab());
        SearchTabPagerAdapter searchTabPagerAdapter = new SearchTabPagerAdapter(getSupportFragmentManager(),
            partyInfo, binding.tabHolder.getTabCount());
        binding.fragmentPager.setAdapter(searchTabPagerAdapter);
        binding.fragmentPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabHolder));
        binding.fragmentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                KeyboardUtils.forceCloseKeyboard(binding.getRoot()); //close keyboard when swiping between pager tabs
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        binding.tabHolder.setupWithViewPager(binding.fragmentPager);

        //bottom sheet setup
        binding.addFab.setOnClickListener(view -> getPresenter().queueRequestedSongs());
        binding.getRoot().post(() -> { //set the height of the content to span to the top of the bottom-sheet
            ViewGroup.LayoutParams adjustedParams = binding.fragmentHolder.getLayoutParams();
            adjustedParams.height = getRootView().getHeight() - bottomSheetBehavior.getPeekHeight();
            binding.fragmentHolder.setLayoutParams(adjustedParams);
        });

        //bottom sheet listview setup
        songRequestArrayAdapter = new SongRequestArrayAdapter(this, songRequests);
        songRequestArrayAdapter.setRemovalPublisher(removeFromRequestsPublisher);
        binding.bottomSheetContent.requestedSongsListView.setAdapter(songRequestArrayAdapter);

        //bottom sheet listview request removal-listener
        subscriptions.add(removeFromRequestsPublisher.subscribe(this::removeRequest));

        //handle hiding/showing bottom sheet on keyboard show/dismiss
        KeyboardUtils.addKeyboardToggleListener(this, isShowing -> {
            if (isShowing) {
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                binding.addFab.setVisibility(View.INVISIBLE);
            }
            else {
                bottomSheetBehavior.setHideable(false);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                binding.addFab.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        subscriptions.clear();
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
    public void finishRequest() {
        //hide bottom sheet
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        binding.addFab.setVisibility(View.INVISIBLE);

        //show snackbar and finish
        new SnackbarBuilder(getRootView())
            .duration(Snackbar.LENGTH_LONG)
            .message("Requesting songs...")
            .dismissCallback((snackbar, event) -> finish())
            .build()
            .show();
    }

    @Override
    public void addRequest(Song song) {
        getPresenter().addRequest(song);
    }

    @Override
    public void removeRequest(Song song) {
        getPresenter().removeRequest(song);
    }

    @Override
    public void updateRequestList(ArrayList<Song> songRequests) {
        this.songRequests.clear();
        this.songRequests.addAll(songRequests);
        songRequestArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSongRequestsLabel() {
        if (songRequests.isEmpty()) {
            binding.bottomSheetContent.requestedSongsLabel.setText(R.string.selected_songs_hint_label);
        }
        else {
            binding.bottomSheetContent.requestedSongsLabel.setText(getResources().getQuantityString(
                R.plurals.selected_songs_amount_label, songRequests.size(), songRequests.size()));
        }
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }

}
