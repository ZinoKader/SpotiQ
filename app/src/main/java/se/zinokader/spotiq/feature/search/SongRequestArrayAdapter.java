package se.zinokader.spotiq.feature.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.model.Song;

public class SongRequestArrayAdapter extends ArrayAdapter<Song> {

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

        List<String> artists = new ArrayList<>();
        for (ArtistSimple artist : song.getArtists()) {
            artists.add(artist.name);
        }
        String artistsJoined = TextUtils.join(", ", artists);

        ImageView albumArt = convertView.findViewById(R.id.albumArt);
        TextView songName = convertView.findViewById(R.id.songName);
        TextView artistsName = convertView.findViewById(R.id.artistsName);
        TextView albumName = convertView.findViewById(R.id.albumName);

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

        return convertView;
    }
}
