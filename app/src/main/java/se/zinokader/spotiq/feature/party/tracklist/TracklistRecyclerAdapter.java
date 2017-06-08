package se.zinokader.spotiq.feature.party.tracklist;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.image.ImageHelper;

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

        List<String> artists = new ArrayList<>();
        for (ArtistSimple artist : song.getArtists()) {
            artists.add(artist.name);
        }

        String artistsName = TextUtils.join(",", artists);

        String runTimeText = String.format(Locale.getDefault(),
                "%d minutes, %d seconds",
                TimeUnit.MILLISECONDS.toMinutes(song.getDurationMs()),
                TimeUnit.MILLISECONDS.toSeconds(song.getDurationMs())
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDurationMs())));

        Glide.with(songHolder.itemView.getContext())
                .load(song.getAlbumArt().url)
                //.bitmapTransform(new BlurTransformation(songHolder.itemView.getContext(), 25, 4))
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable drawable, GlideAnimation<? super GlideDrawable> glideAnimation) {

                        Bitmap imageBitmap = ImageHelper.convertToBitmap(drawable,
                                drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight());

                        int startColor = ImageHelper.getDominantColor(imageBitmap);
                        int endColor = songHolder.cardViewRoot.getSolidColor();

                        int[] gradientColors = { startColor, endColor };
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);

                        songHolder.cardViewRoot.setBackground(gradientDrawable);
                    }
                });


        Glide.with(songHolder.itemView.getContext())
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
        private View cardViewRoot;
        private ImageView albumArt;
        private TextView songName;
        private TextView artistsName;
        private TextView albumName;
        private TextView runTime;

        SongHolder(View view) {
            super(view);

            cardViewRoot = view.findViewById(R.id.trackViewHolder);
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
