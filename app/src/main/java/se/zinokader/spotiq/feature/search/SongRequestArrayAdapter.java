package se.zinokader.spotiq.feature.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import io.reactivex.subjects.PublishSubject;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.mapper.ArtistMapper;

public class SongRequestArrayAdapter extends ArrayAdapter<Song> {

    private PublishSubject<Song> removalPublisher;

    SongRequestArrayAdapter(@NonNull Context context, @NonNull List<Song> songs) {
        super(context, 0, songs);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Song song = getItem(position);

        //inflate the view if convertView is null (as in convertView is not being reused)
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_row_requested_song, parent, false);
        }

        String artistsJoined = ArtistMapper.joinArtistNames(song.getArtists());

        ImageView albumArt = convertView.findViewById(R.id.albumArt);
        TextView songName = convertView.findViewById(R.id.songName);
        TextView artistsName = convertView.findViewById(R.id.artistsName);
        TextView albumName = convertView.findViewById(R.id.albumName);
        ImageButton closeButton = convertView.findViewById(R.id.closeButton);

        Glide.with(convertView.getContext())
            .load(song.getAlbumArtUrl())
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .placeholder(R.drawable.image_album_placeholder)
            .fitCenter()
            .override(ApplicationConstants.LOW_QUALITY_ALBUM_ART_DIMENSION, ApplicationConstants.LOW_QUALITY_ALBUM_ART_DIMENSION)
            .into(albumArt);

        songName.setText(song.getName());
        artistsName.setText(artistsJoined);
        albumName.setText(song.getAlbum().name);

        closeButton.setOnClickListener(view -> removalPublisher.onNext(song));

        return convertView;
    }

    public void setRemovalPublisher(PublishSubject<Song> removalPublisher) {
        this.removalPublisher = removalPublisher;
    }
}
