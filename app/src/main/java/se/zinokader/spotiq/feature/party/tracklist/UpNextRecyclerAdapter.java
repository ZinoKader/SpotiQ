package se.zinokader.spotiq.feature.party.tracklist;

import android.content.Context;
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

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.model.Song;

public class UpNextRecyclerAdapter extends RecyclerView.Adapter<UpNextRecyclerAdapter.SongHolder> {

    private List<Song> songs;

    UpNextRecyclerAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflatedView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_row_tracklist_upnext, viewGroup, false);
        inflatedView.getLayoutParams().width = viewGroup.getWidth();
        return new SongHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(SongHolder songHolder, int position) {

        if (position >= 0) {
            position += 1;
        }

        Song song = songs.get(position);
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

        songHolder.cropTransformation = new CropTransformation(context,
                ApplicationConstants.DEFAULT_TRACKLIST_CROP_WIDTH,
                ApplicationConstants.DEFAULT_TRACKLIST_CROP_HEIGHT,
                CropTransformation.CropType.CENTER);
        songHolder.blurTransformation = new BlurTransformation(context, ApplicationConstants.DEFAULT_TRACKLIST_BLUR_RADIUS);
        songHolder.colorFilterTransformation = new ColorFilterTransformation(context, R.color.colorPrimary);

        if (songs.get(position) != null) {
            Glide.with(songHolder.itemView.getContext())
                    .load(song.getAlbumArtUrl())
                    .placeholder(R.drawable.image_album_placeholder)
                    .bitmapTransform(songHolder.blurTransformation, songHolder.cropTransformation, songHolder.colorFilterTransformation)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            songHolder.cardViewRoot.setBackground(drawable);
                        }
                    });

            Glide.with(context)
                    .load(song.getAlbumArtUrl())
                    .placeholder(R.drawable.image_album_placeholder)
                    .fitCenter()
                    .into(songHolder.albumArt);
        }
        else {
            songHolder.cardViewRoot.setBackground(null);
            songHolder.albumArt.setImageDrawable(null);
            Glide.clear(songHolder.cardViewRoot);
            Glide.clear(songHolder.albumArt);
        }

        songHolder.songName.setText(song.getName());
        songHolder.artistsName.setText(artistsName);
        songHolder.runTime.setText(runTimeText);
        songHolder.albumName.setText(song.getAlbum().name);
    }

    @Override
    public int getItemCount() {
        return songs.size() > 1 ? songs.size() - 1 : 0; //handle all items except the first one
    }

    class SongHolder extends RecyclerView.ViewHolder {
        private CropTransformation cropTransformation;
        private BlurTransformation blurTransformation;
        private ColorFilterTransformation colorFilterTransformation;

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
        }

    }

}
