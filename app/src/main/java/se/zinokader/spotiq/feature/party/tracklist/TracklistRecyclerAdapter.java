package se.zinokader.spotiq.feature.party.tracklist;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Song;

public class TracklistRecyclerAdapter extends RecyclerView.Adapter<TracklistRecyclerAdapter.SongHolder> {

    private List<Song> songs;

    TracklistRecyclerAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflatedView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_row_tracklist_song, viewGroup, false);
        return new SongHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(SongHolder songHolder, int i) {
        Song song = songs.get(i);

        List<String> artists = new ArrayList<>();
        for (ArtistSimple artist : song.getArtists()) {
            artists.add(artist.name);
        }

        String artistsName = "Artists: " + TextUtils.join(",", artists);

        String runTimeText = String.format(Locale.getDefault(), "%02d minutes, %02d seconds",
                TimeUnit.MILLISECONDS.toMinutes(song.getDurationMs()),
                TimeUnit.MILLISECONDS.toSeconds(song.getDurationMs()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDurationMs()))
        );

        Glide.with(songHolder.itemView)
                .load(song.getAlbumArt().url)
                .into(songHolder.albumArt);

        songHolder.songName.setText(song.getName());
        songHolder.artistsName.setText(artistsName);
        songHolder.albumName.setText(song.getAlbum().name);
        songHolder.runTime.setText(runTimeText);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView albumArt;
        private TextView songName;
        private TextView artistsName;
        private TextView albumName;
        private TextView runTime;

        SongHolder(View view) {
            super(view);

            albumArt = view.findViewById(R.id.albumArt);
            songName = view.findViewById(R.id.songName);
            artistsName = view.findViewById(R.id.artistsName);
            albumName = view.findViewById(R.id.albumName);
            runTime = view.findViewById(R.id.runTime);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

}
