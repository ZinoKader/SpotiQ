package se.zinokader.spotiq.feature.party.tracklist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
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
        inflatedView.getLayoutParams().width = viewGroup.getWidth();
        return new SongHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(SongHolder songHolder, int i) {

        Song song = songs.get(i);
        Context context = songHolder.itemView.getContext();

        List<String> artists = new ArrayList<>();
        for (ArtistSimple artist : song.getArtists()) {
            artists.add(artist.name);
        }

        String artistsName = TextUtils.join(", ", artists);

        String runTimeText = String.format(Locale.getDefault(),
                "%d minutes, %d seconds",
                TimeUnit.MILLISECONDS.toMinutes(song.getDurationMs()),
                TimeUnit.MILLISECONDS.toSeconds(song.getDurationMs())
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDurationMs())));

        songHolder.cropTransformation = new CropTransformation(context, 600, 300, CropTransformation.CropType.CENTER);
        songHolder.blurTransformation = new BlurTransformation(context, 15, 1);
        songHolder.colorFilterTransformation = new ColorFilterTransformation(context, R.color.colorPrimary);

        Glide.with(songHolder.itemView.getContext())
                .load(song.getAlbumArtUrl())
                .bitmapTransform(songHolder.blurTransformation, songHolder.cropTransformation, songHolder.colorFilterTransformation)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable drawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        songHolder.cardViewRoot.setBackground(drawable);
                    }
                });

        songHolder.songName.setText(song.getName());
        songHolder.artistsName.setText(artistsName);
        songHolder.runTime.setText(runTimeText);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CropTransformation cropTransformation;
        private BlurTransformation blurTransformation;
        private ColorFilterTransformation colorFilterTransformation;

        private View cardViewRoot;
        private TextView songName;
        private TextView artistsName;
        private TextView runTime;

        SongHolder(View view) {
            super(view);

            cardViewRoot = view.findViewById(R.id.trackViewHolder);
            songName = view.findViewById(R.id.songName);
            artistsName = view.findViewById(R.id.artistsName);
            runTime = view.findViewById(R.id.runTime);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

}
