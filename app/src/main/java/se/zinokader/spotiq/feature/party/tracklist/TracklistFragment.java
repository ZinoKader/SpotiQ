package se.zinokader.spotiq.feature.party.tracklist;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.FragmentTracklistBinding;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.listener.FabListener;
import se.zinokader.spotiq.util.view.DividerItemDecoration;
import su.j2e.rvjoiner.JoinableAdapter;
import su.j2e.rvjoiner.JoinableLayout;
import su.j2e.rvjoiner.RvJoiner;

public class TracklistFragment extends Fragment {

    FragmentTracklistBinding binding;

    @Inject
    TracklistRepository tracklistRepository;

    private FabListener fabListener;

    private RvJoiner recyclerViewJoiner = new RvJoiner(true);
    private JoinableLayout emptyTracklistLayout = new JoinableLayout(R.layout.recyclerview_empty_placeholder_view);
    private boolean emptyTracklistNoticeAttached = true;
    private JoinableLayout upNextHeaderLayout = new JoinableLayout(R.layout.recyclerview_row_tracklist_upnext_header);
    private boolean upNextHeaderAttached = false;

    private String partyTitle;
    private List<Song> songs = new ArrayList<>();

    public static TracklistFragment newInstance(String partyTitle) {
        TracklistFragment tracklistFragment = new TracklistFragment();
        Bundle newInstanceArguments = new Bundle();
        newInstanceArguments.putString("partyTitle", partyTitle);
        tracklistFragment.setArguments(newInstanceArguments);
        return tracklistFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((Injector) getContext().getApplicationContext()).inject(this);
        super.onCreate(savedInstanceState);
        this.partyTitle = getArguments().getString("partyTitle");

        tracklistRepository.listenToTracklistChanges(partyTitle)
            .delay(ApplicationConstants.DEFAULT_DELAY_MS, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(childEvent -> {
                Song song = childEvent.getDataSnapshot().getValue(Song.class);
                switch (childEvent.getChangeType()) {
                    case ADDED:
                        addSong(song);
                        break;
                    case REMOVED:
                        removeSong(song);
                        break;
                }});
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("partyTitle", getArguments().getString("partyTitle"));
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof FabListener) {
            fabListener = (FabListener) context;
        }
        else {
            throw new ClassCastException(context.getClass().getSimpleName()
                + " must implement " + fabListener.getClass().getSimpleName());
        }
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracklist, container, false);

        binding.tracklistRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fabListener.hideControls();
                }
                else if (dy < 0) {
                    fabListener.showControls();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        binding.tracklistRecyclerView.addItemDecoration(new DividerItemDecoration(
            getResources().getDrawable(R.drawable.track_list_divider), false, true));
        binding.tracklistRecyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        JoinableAdapter nowPlayingJoinable = new JoinableAdapter(new NowPlayingRecyclerAdapter(songs), true);
        JoinableAdapter upNextJoinable = new JoinableAdapter(new UpNextRecyclerAdapter(songs), true);

        recyclerViewJoiner.add(emptyTracklistLayout, 0);
        recyclerViewJoiner.add(nowPlayingJoinable);
        recyclerViewJoiner.add(upNextJoinable);

        SlideInBottomAnimationAdapter animatedAdapter =
            new SlideInBottomAnimationAdapter(recyclerViewJoiner.getAdapter());
        animatedAdapter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatedAdapter.setHasStableIds(true);
        animatedAdapter.setStartPosition(ApplicationConstants.DEFAULT_LIST_ANIMATION_ITEM_POSITION_START);
        animatedAdapter.setDuration(ApplicationConstants.DEFAULT_LIST_ANIMATION_DURATION_MS);

        FadeInDownAnimator itemAnimator = new FadeInDownAnimator();
        itemAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        itemAnimator.setAddDuration(ApplicationConstants.DEFAULT_ITEM_ADD_DURATION_MS);
        itemAnimator.setRemoveDuration(ApplicationConstants.DEFAULT_ITEM_REMOVE_DURATION_MS);
        itemAnimator.setMoveDuration(ApplicationConstants.DEFAULT_ITEM_MOVE_DURATION_MS);
        binding.tracklistRecyclerView.setItemAnimator(itemAnimator);

        binding.tracklistRecyclerView.setAdapter(animatedAdapter);

        return binding.getRoot();
    }

    public void scrollToTop() {
        binding.tracklistRecyclerView.smoothScrollToPosition(0);
    }

    private void addSong(Song song) {

        if (emptyTracklistNoticeAttached) {
            Log.d("Removed shit", "okokokok");
            recyclerViewJoiner.remove(emptyTracklistLayout);
            emptyTracklistNoticeAttached = false;
            recyclerViewJoiner.getAdapter().notifyItemRemoved(0);
        }

        songs.add(song);
        int songPosition = getSongPosition(song);

        if (songs.size() == 1) {
            recyclerViewJoiner.getAdapter().notifyItemInserted(songPosition);
        }
        else if (songs.size() >= 2 && !upNextHeaderAttached) {
            recyclerViewJoiner.add(upNextHeaderLayout, 1);
            upNextHeaderAttached = true;
            recyclerViewJoiner.getAdapter().notifyItemRangeInserted(1, 2);
        }
        else {
            recyclerViewJoiner.getAdapter().notifyItemInserted(songPosition + 1);
        }
    }

    private void removeSong(Song song) {
        int songPosition = getSongPosition(song);
        songs.remove(songPosition);

        if (songs.size() <= 1 && upNextHeaderAttached) {
            recyclerViewJoiner.remove(upNextHeaderLayout);
            upNextHeaderAttached = false;
            recyclerViewJoiner.getAdapter().notifyItemRangeRemoved(songPosition, 2);
        }
        else {
            recyclerViewJoiner.getAdapter().notifyItemRemoved(songPosition + 1);
        }


        if (songs.isEmpty() && !emptyTracklistNoticeAttached) {
            recyclerViewJoiner.add(emptyTracklistLayout, 0);
            emptyTracklistNoticeAttached = true;
            recyclerViewJoiner.getAdapter().notifyItemInserted(0);
        }
    }

    private int getSongPosition(Song song) {
        for (int songPosition = 0; songPosition < songs.size(); songPosition++) {
            if (song.getSongSpotifyId().equals(songs.get(songPosition).getSongSpotifyId())) {
                return songPosition;
            }
        }
        return -1;
    }


}
