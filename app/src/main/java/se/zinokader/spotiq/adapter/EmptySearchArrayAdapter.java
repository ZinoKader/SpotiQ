package se.zinokader.spotiq.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Playlist;

public class EmptySearchArrayAdapter extends ArrayAdapter<Playlist> {

    Typeface ROBOTO = Typeface.createFromAsset(getContext().getResources().getAssets(), "fonts/robotolight.ttf");
    CircularImageView albumart;
    TextView playlistname;
    TextView songcount;

    public EmptySearchArrayAdapter(Context context, int resource, ArrayList<Playlist> songs) {
        super(context, resource, songs);
    }

    @Override
    public void addAll(Playlist... items) {
        super.addAll(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Playlist playlist = getItem(position); //skapa ny song för varje view (varje view är ett listitem)

        View view = convertView;

        if(view == null) {
            LayoutInflater inflater;
            inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.items_listview_search_playlists, null);
        }

        albumart = (CircularImageView) view.findViewById(R.id.albumArt);
        playlistname = (TextView) view.findViewById(R.id.playlistName);
        songcount = (TextView) view.findViewById(R.id.playlistSongCount);

        playlistname.setTypeface(ROBOTO);
        songcount.setTypeface(ROBOTO);

        Glide.with(getContext()).load(playlist.getPlaylistArtUrl()).into(albumart);
        playlistname.setText(playlist.getPlaylistName());
        songcount.setText(Integer.toString(playlist.getSongCount()) + " songs");

        return view;
    }
}
