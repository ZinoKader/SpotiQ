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

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;

import org.cryse.widget.persistentsearch.SimpleSearchListener;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivitySearchBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.search.searchlist.SearchRecyclerAdapter;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.listener.Debouncer;

public class SearchActivity extends BaseActivity implements SearchView {

    ActivitySearchBinding binding;
    private Bundle partyInfo;
    private SearchPresenter presenter;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private Vibrator vibrator;
    private Debouncer debouncer = new Debouncer();

    public SearchActivity() {
        addPlugin(new TiActivityPlugin<>(SearchPresenter::new));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        partyInfo = getIntent().getExtras();

        searchRecyclerAdapter = new SearchRecyclerAdapter();
        binding.songSearchRecyclerView.setHasFixedSize(true);
        binding.songSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.songSearchRecyclerView.setAdapter(searchRecyclerAdapter);

        binding.searchBar.openSearch();
        binding.searchBar.setHomeButtonListener(this::finish);
        binding.searchBar.setSearchListener(new SimpleSearchListener() {
            @Override
            public void onSearchTermChanged(String query) {
                debouncer.debounce(() -> presenter.searchTracks(query), ApplicationConstants.DEFAULT_DEBOUNCE_MS);
            }
        });

        searchRecyclerAdapter.observeClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showConfirmSongRequest);

        searchRecyclerAdapter.observeLongClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(song -> {
                vibrator.vibrate(ApplicationConstants.SHORT_VIBRATION_DURATION);
                if (song.getPreviewUrl() != null) {
                    presenter.startPreview(song.getPreviewUrl());
                }
                else {
                    showMessage("Song preview not available");
                }
            });

        //Stop preview when the user lets go from with the item's bounds
        searchRecyclerAdapter.observeLongClickEnd()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ended -> presenter.stopPreview());

        //Also stop preview if user lifts finger from outside of the item's bounds
        binding.songSearchRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                presenter.stopPreview();
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
            .actionClickListener(confirmed -> {
                presenter.requestSong(song);
                finish();
            })
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
    public void setPresenter(TiPresenter presenter) {
        this.presenter = (SearchPresenter) presenter;
        ((Injector) getApplication()).inject(presenter);
        this.presenter.init();
        if(partyInfo != null) {
            this.presenter.setPartyTitle(partyInfo.getString(ApplicationConstants.PARTY_NAME_EXTRA));
        }
    }

    @Override
    public boolean isPresenterAttached() {
        return presenter != null;
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }

}
