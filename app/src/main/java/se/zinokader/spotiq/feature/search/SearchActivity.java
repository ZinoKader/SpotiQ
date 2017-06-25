package se.zinokader.spotiq.feature.search;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;

import org.cryse.widget.persistentsearch.SimpleSearchListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivitySearchBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.search.searchlist.SearchRecyclerAdapter;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.listener.Debouncer;

@RequiresPresenter(SearchPresenter.class)
public class SearchActivity extends BaseActivity<SearchPresenter> implements SearchView {

    ActivitySearchBinding binding;
    private Bundle partyInfo;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private Vibrator vibrator;
    private Debouncer debouncer = new Debouncer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        partyInfo = getIntent().getExtras();
        getPresenter().setPartyTitle(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));

        searchRecyclerAdapter = new SearchRecyclerAdapter();
        binding.songSearchRecyclerView.setHasFixedSize(true);
        binding.songSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.songSearchRecyclerView.setAdapter(searchRecyclerAdapter);

        binding.searchBar.openSearch();
        binding.searchBar.setHomeButtonListener(this::finish);
        binding.searchBar.setSearchListener(new SimpleSearchListener() {
            @Override
            public void onSearchTermChanged(String query) {
                updateSearch(new ArrayList<>(), true);
                debouncer.debounce(() -> getPresenter().searchTracks(query), ApplicationConstants.DEFAULT_DEBOUNCE_MS);
            }
        });

        searchRecyclerAdapter.observeClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showConfirmSongRequest);

        searchRecyclerAdapter.observeLongClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(song -> {
                vibrator.vibrate(ApplicationConstants.SHORT_VIBRATION_DURATION_MS);
                if (song.getPreviewUrl() != null) {
                    getPresenter().startPreview(song.getPreviewUrl());
                }
                else {
                    showMessage("Song preview not available");
                }
            });

        //Stop preview when the user lets go from with the item's bounds
        searchRecyclerAdapter.observeLongClickEnd()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ended -> getPresenter().stopPreview());

        //Also stop preview if user lifts finger from outside of the item's bounds
        binding.songSearchRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                getPresenter().stopPreview();
            }
            view.performClick();
            return false;
        });
    }

    public void showConfirmSongRequest(Song song) {
        new SnackbarBuilder(getRootView())
            .duration(Snackbar.LENGTH_LONG)
            .message("Confirm song request")
            .actionText("Queue song")
            .actionTextColor(getResources().getColor(R.color.colorAccent))
            .actionClickListener(confirmed -> getPresenter().requestSong(song))
            .build()
            .show();
    }


    @Override
    public void updateSearch(List<Song> songs, boolean shouldClear) {
        if (shouldClear) searchRecyclerAdapter.clearSongs();
        searchRecyclerAdapter.addSongs(songs);
        searchRecyclerAdapter.notifyDataSetChanged();
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
