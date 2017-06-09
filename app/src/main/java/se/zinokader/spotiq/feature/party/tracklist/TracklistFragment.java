package se.zinokader.spotiq.feature.party.tracklist;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dilpreet2028.fragmenter_annotations.Fragmenter;
import com.dilpreet2028.fragmenter_annotations.annotations.FragModule;
import java.util.ArrayList;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.FragmentTracklistBinding;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.mapper.TrackMapper;

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
        binding.songsRecyclerView.setAdapter(tracklistRecyclerAdapter);

        //Quick, lazy test. TODO: Remove this
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken("BQCDpRDk4M4hSWVugFZJKDxcab6m4uxlH8YQGhSi2P-vvs0VF40H-TPIzneePI9aDW6XNKmidrMqEShzYhedJEW_hlpBjnqiD7w3fZvVuzr7pwY8s2z4A5f0LMAaeW8WquaQzvL90yTgjgJXzRhqg6EQLfgdMNhMoN-PsxqvH6NKJpJ_RxM");
        SpotifyService spotifyService = spotifyApi.getService();

        spotifyService.getTrack("3n3Ppam7vgaVa1iaRUc9Lp", new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                Song testSong = TrackMapper.trackToSong(track, "zinne97");
                addSong(testSong);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("ERRORGUY", error.getBody().toString());
            }
        });

        spotifyService.getTrack("12Chz98pHFMPJEknJQMWvI", new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                Song testSong = TrackMapper.trackToSong(track, "zinne97");
                addSong(testSong);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("ERRORGUY", error.getBody().toString());
            }
        });

        spotifyService.getTrack("1tjHKKI0r82IB5KL29whHs", new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                Song testSong = TrackMapper.trackToSong(track, "zinne97");
                addSong(testSong);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("ERRORGUY", error.getBody().toString());
            }
        });

        spotifyService.getTrack("6b2oQwSGFkzsMtQruIWm2p", new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                Song testSong = TrackMapper.trackToSong(track, "zinne97");
                addSong(testSong);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("ERRORGUY", error.getBody().toString());
            }
        });



        return binding.getRoot();
    }

    public void addSong(Song song) {
        songs.add(song);
        tracklistRecyclerAdapter.notifyDataSetChanged();
    }

}
