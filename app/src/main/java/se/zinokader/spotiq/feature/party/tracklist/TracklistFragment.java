package se.zinokader.spotiq.feature.party.tracklist;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import se.zinokader.spotiq.util.listener.FabListener;
import su.j2e.rvjoiner.JoinableAdapter;
import su.j2e.rvjoiner.JoinableLayout;
import su.j2e.rvjoiner.RvJoiner;

@FragModule
public class TracklistFragment extends Fragment {

    FragmentTracklistBinding binding;

    private FabListener fabListener;
    private RvJoiner recyclerViewJoiner = new RvJoiner(true);
    private NowPlayingRecyclerAdapter nowPlayingRecyclerAdapter;
    private UpNextRecyclerAdapter upNextRecyclerAdapter;
    private boolean upNextHeaderAttached = false;

    private List<Song> songs = new ArrayList<>();

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
        Fragmenter.inject(this);

        binding.tracklistRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fabListener.hideFab();
                }
                else if (dy < 0) {
                    fabListener.showFab();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        binding.tracklistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        nowPlayingRecyclerAdapter = new NowPlayingRecyclerAdapter(songs);
        upNextRecyclerAdapter = new UpNextRecyclerAdapter(songs);

        recyclerViewJoiner.add(new JoinableAdapter(nowPlayingRecyclerAdapter, true));
        recyclerViewJoiner.add(new JoinableAdapter(upNextRecyclerAdapter, true));

        binding.tracklistRecyclerView.setAdapter(recyclerViewJoiner.getAdapter());

        return binding.getRoot();
    }

    public void addSong(Song song) {
        songs.add(song);
        if (songs.size() > 1 && !upNextHeaderAttached) {
            recyclerViewJoiner.add(new JoinableLayout(R.layout.recyclerview_row_tracklist_upnext_header), 1);
            upNextHeaderAttached = true;
        }
        recyclerViewJoiner.getAdapter().notifyItemInserted(songs.size());
    }


    public void removeSong(int position) {
        songs.remove(position);
        if (songs.size() <= 1) {
            recyclerViewJoiner.remove(new JoinableLayout(R.layout.recyclerview_row_tracklist_upnext_header));
            upNextHeaderAttached = false;
        }
        recyclerViewJoiner.getAdapter().notifyDataSetChanged();
    }

}
