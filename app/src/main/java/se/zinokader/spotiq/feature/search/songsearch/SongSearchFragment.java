package se.zinokader.spotiq.feature.search.songsearch;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.andrewlord1990.snackbarbuilder.SnackbarBuilder;

import org.cryse.widget.persistentsearch.SimpleSearchListener;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.FragmentSongSearchBinding;
import se.zinokader.spotiq.feature.base.BaseFragment;
import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.feature.search.SongRecyclerAdapter;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.listener.Debouncer;

@RequiresPresenter(SongSearchPresenter.class)
public class SongSearchFragment extends BaseFragment<SongSearchPresenter> implements SongSearchView {

    FragmentSongSearchBinding binding;

    private SongRecyclerAdapter songRecyclerAdapter;
    private Vibrator vibrator;
    private Debouncer debouncer = new Debouncer();

    public static SongSearchFragment newInstance(String partyTitle) {
        SongSearchFragment songSearchFragment = new SongSearchFragment();
        Bundle newInstanceArguments = new Bundle();
        newInstanceArguments.putString(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        songSearchFragment.setArguments(newInstanceArguments);
        return songSearchFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(ApplicationConstants.PARTY_NAME_EXTRA, getArguments().getString(ApplicationConstants.PARTY_NAME_EXTRA));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_song_search, container, false);
        getPresenter().setPartyTitle(getArguments().getString(ApplicationConstants.PARTY_NAME_EXTRA));

        songRecyclerAdapter = new SongRecyclerAdapter();
        binding.songSearchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        songRecyclerAdapter.observeClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showConfirmSongRequest);

        songRecyclerAdapter.observeLongClicks()
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
        songRecyclerAdapter.observeLongClickEnd()
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

        songRecyclerAdapter.setHasStableIds(true);
        binding.songSearchRecyclerView.setHasFixedSize(true);
        binding.songSearchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.searchBar.setHomeButtonListener(() -> binding.searchBar.hideSuggestions());
        binding.searchBar.setSearchListener(new SimpleSearchListener() {
            @Override
            public void onSearchTermChanged(String query) {
                songRecyclerAdapter.clearResults();
                songRecyclerAdapter.notifyDataSetChanged();
                if (!query.isEmpty()) {
                    debouncer.debounce(() -> getPresenter().searchTracks(query), ApplicationConstants.DEFAULT_DEBOUNCE_MS);
                }
            }
        });

        AlphaInAnimationAdapter animatedAdapter =
            new AlphaInAnimationAdapter(songRecyclerAdapter);
        animatedAdapter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatedAdapter.setHasStableIds(true);
        animatedAdapter.setStartPosition(ApplicationConstants.DEFAULT_LIST_ANIMATION_ITEM_POSITION_START);
        animatedAdapter.setDuration(ApplicationConstants.DEFAULT_LIST_ANIMATION_DURATION_MS);

        binding.songSearchRecyclerView.setAdapter(animatedAdapter);
        return binding.getRoot();
    }

    public void showConfirmSongRequest(Song song) {
        new SnackbarBuilder(((BaseView) getActivity()).getRootView())
            .duration(Snackbar.LENGTH_LONG)
            .message("Confirm song request")
            .actionText("Queue song")
            .actionTextColor(getResources().getColor(R.color.colorAccent))
            .actionClickListener(confirmed -> getPresenter().requestSong(song))
            .build()
            .show();
    }

    @Override
    public void updateSearch(List<Song> songs) {
        songRecyclerAdapter.updateSongs(songs);
        songRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSearchSuggestions(SongSearchSuggestionsBuilder searchSuggestionsBuilder) {
        searchSuggestionsBuilder.buildSuggestionItems(getContext());
        binding.searchBar.setSuggestionBuilder(searchSuggestionsBuilder);
    }

}
