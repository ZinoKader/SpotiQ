package se.zinokader.spotiq.feature.search.playlistsearch;

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

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.FragmentPlaylistSearchBinding;
import se.zinokader.spotiq.feature.base.BaseFragment;
import se.zinokader.spotiq.feature.base.BaseView;
import se.zinokader.spotiq.feature.search.SongRecyclerAdapter;
import se.zinokader.spotiq.model.Song;

@RequiresPresenter(PlaylistSearchPresenter.class)
public class PlaylistSearchFragment extends BaseFragment<PlaylistSearchPresenter> implements PlaylistSearchView {

    FragmentPlaylistSearchBinding binding;

    private PlaylistSearchRecyclerAdapter playlistSearchRecyclerAdapter;
    private AlphaInAnimationAdapter animatedPlaylistAdapter;
    private SongRecyclerAdapter songRecylerAdapter;
    private AlphaInAnimationAdapter animatedSongAdapter;
    private Vibrator vibrator;

    public static PlaylistSearchFragment newInstance(String partyTitle) {
        PlaylistSearchFragment playlistSearchFragment = new PlaylistSearchFragment();
        Bundle newInstanceArguments = new Bundle();
        newInstanceArguments.putString(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        playlistSearchFragment.setArguments(newInstanceArguments);
        return playlistSearchFragment;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist_search, container, false);
        getPresenter().setPartyTitle(getArguments().getString(ApplicationConstants.PARTY_NAME_EXTRA));

        playlistSearchRecyclerAdapter = new PlaylistSearchRecyclerAdapter();
        songRecylerAdapter = new SongRecyclerAdapter();
        binding.contentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        playlistSearchRecyclerAdapter.setHasStableIds(true);
        songRecylerAdapter.setHasStableIds(true);
        binding.contentRecyclerView.setHasFixedSize(true);
        binding.contentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        animatedPlaylistAdapter = new AlphaInAnimationAdapter(playlistSearchRecyclerAdapter);
        animatedPlaylistAdapter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatedPlaylistAdapter.setHasStableIds(true);
        animatedPlaylistAdapter.setStartPosition(ApplicationConstants.DEFAULT_LIST_ANIMATION_ITEM_POSITION_START);
        animatedPlaylistAdapter.setDuration(ApplicationConstants.DEFAULT_LIST_ANIMATION_DURATION_MS);

        animatedSongAdapter = new AlphaInAnimationAdapter(songRecylerAdapter);
        animatedSongAdapter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatedSongAdapter.setHasStableIds(true);
        animatedSongAdapter.setStartPosition(ApplicationConstants.DEFAULT_LIST_ANIMATION_ITEM_POSITION_START);
        animatedSongAdapter.setDuration(ApplicationConstants.DEFAULT_LIST_ANIMATION_DURATION_MS);

        binding.contentRecyclerView.setAdapter(animatedPlaylistAdapter);

        setupListeners();

        return binding.getRoot();
    }

    private void setupListeners() {
        playlistSearchRecyclerAdapter.observeClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(playlist -> {
                binding.searchTypeLabel.setVisibility(View.GONE);
                binding.playlistBar.setVisibility(View.VISIBLE);
                binding.playlistName.setText(playlist.name);

                binding.contentRecyclerView.setAdapter(animatedSongAdapter);
                songRecylerAdapter.clearResults();
                getPresenter().loadPlaylistSongs(playlist);
            });

        binding.playlistBackButton.setOnClickListener(view -> {
            binding.contentRecyclerView.setAdapter(animatedPlaylistAdapter);
            binding.playlistBar.setVisibility(View.GONE);
            binding.searchTypeLabel.setVisibility(View.VISIBLE);
            getPresenter().request(PlaylistSearchPresenter.LOAD_PLAYLISTS_RESTARTABLE_ID);
        });

        songRecylerAdapter.observeClicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showConfirmSongRequest);

        songRecylerAdapter.observeLongClicks()
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
        songRecylerAdapter.observeLongClickEnd()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ended -> getPresenter().stopPreview());

        //Also stop preview if user lifts finger from outside of the item's bounds
        binding.contentRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                getPresenter().stopPreview();
            }
            view.performClick();
            return false;
        });
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
    public void updatePlaylists(List<PlaylistSimple> playlists) {
        playlistSearchRecyclerAdapter.updatePlaylists(playlists);
        playlistSearchRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSongs(List<Song> songs) {
        songRecylerAdapter.updateSongs(songs);
        songRecylerAdapter.notifyDataSetChanged();
    }
}
