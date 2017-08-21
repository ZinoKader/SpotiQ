package se.zinokader.spotiq.feature.party.tracklist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.mapper.ArtistMapper;

public class TracklistRecyclerAdapter extends RecyclerView.Adapter<TracklistRecyclerAdapter.SongHolder> {

    private List<Song> songs;
    private static final int POSITION_NOW_PLAYING = 0;
    private static final int POSITION_UP_NEXT_WITH_HEADER = 1;
    private static final int POSITION_UP_NEXT = 2;

    TracklistRecyclerAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView;
        switch (viewType) {
            case POSITION_NOW_PLAYING:
                inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_row_tracklist_now_playing, parent, false);
                break;
            case POSITION_UP_NEXT_WITH_HEADER:
                inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_row_tracklist_up_next_with_header, parent, false);
                break;
            default:
            case POSITION_UP_NEXT:
                inflatedView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_row_tracklist_up_next, parent, false);
        }
        return new SongHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(SongHolder songHolder, int position) {

        Song song = songs.get(position);
        Context context = songHolder.itemView.getContext();

        String artistsName = ArtistMapper.joinArtistNames(song.getArtists());

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

        Glide.with(songHolder.itemView.getContext())
            .load(song.getAlbumArtUrl())
            .fitCenter()
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

        songHolder.songName.setText(song.getName());
        songHolder.artistsName.setText(artistsName);
        songHolder.runTime.setText(runTimeText);
        songHolder.albumName.setText(song.getAlbum().name);
    }

    @Override
    public void onViewRecycled(SongHolder songHolder) {
        if(songHolder != null) {
            songHolder.cardViewRoot.setBackground(null);
            songHolder.albumArt.setImageDrawable(null);
            Glide.clear(songHolder.cardViewRoot);
            Glide.clear(songHolder.albumArt);
        }
        super.onViewRecycled(songHolder);
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return POSITION_NOW_PLAYING;
            case 1:
                return POSITION_UP_NEXT_WITH_HEADER;
            default:
                return POSITION_UP_NEXT;
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
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
