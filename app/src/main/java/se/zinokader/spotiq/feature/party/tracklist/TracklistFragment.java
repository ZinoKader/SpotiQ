package se.zinokader.spotiq.feature.party.tracklist;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
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

@FragModule
public class TracklistFragment extends Fragment {

    FragmentTracklistBinding binding;
    private TracklistRecyclerAdapter tracklistRecyclerAdapter;
    private List<Song> songs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracklist, container, false);
        Fragmenter.inject(this);

        tracklistRecyclerAdapter = new TracklistRecyclerAdapter(songs);
        binding.songsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.songsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.songsRecyclerView.setAdapter(tracklistRecyclerAdapter);

        return binding.getRoot();
    }

    public void addSong(Song song) {
        songs.add(song);
        tracklistRecyclerAdapter.notifyDataSetChanged();
    }

}
