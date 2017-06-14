package se.zinokader.spotiq.feature.party.tracklist;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dilpreet2028.fragmenter_annotations.Fragmenter;
import com.dilpreet2028.fragmenter_annotations.annotations.FragModule;

import java.util.ArrayList;
import java.util.List;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.FragmentTracklistBinding;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.view.PreCachingLayoutManager;
import su.j2e.rvjoiner.JoinableAdapter;
import su.j2e.rvjoiner.RvJoiner;

@FragModule
public class TracklistFragment extends Fragment {

    FragmentTracklistBinding binding;
    private RvJoiner recyclerViewJoiner = new RvJoiner(true);
    private NowPlayingRecyclerAdapter nowPlayingRecyclerAdapter;
    private UpNextRecyclerAdapter upNextRecyclerAdapter;
    private List<Song> songs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracklist, container, false);
        Fragmenter.inject(this);

        binding.tracklistRecyclerView.setLayoutManager(new PreCachingLayoutManager(getContext()));
        binding.tracklistRecyclerView.setDrawingCacheEnabled(true);
        binding.tracklistRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        nowPlayingRecyclerAdapter = new NowPlayingRecyclerAdapter(songs);
        upNextRecyclerAdapter = new UpNextRecyclerAdapter(songs);

        recyclerViewJoiner.add(new JoinableAdapter(nowPlayingRecyclerAdapter, true));
        recyclerViewJoiner.add(new JoinableAdapter(upNextRecyclerAdapter, true));

        binding.tracklistRecyclerView.setAdapter(recyclerViewJoiner.getAdapter());

        return binding.getRoot();
    }

    public void addSong(Song song) {
        songs.add(song);
        upNextRecyclerAdapter.notifyDataSetChanged();
    }

}
