package se.zinokader.spotiq.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Song;

public class SearchArrayAdapter extends ArrayAdapter<Song> {

    Typeface ROBOTO = Typeface.createFromAsset(getContext().getResources().getAssets(), "fonts/robotolight.ttf");
    TextView artistname;
    TextView songname;
    TextView runtime;

    public SearchArrayAdapter(Context context, int resource, ArrayList<Song> songs) {
        super(context, resource, songs);
    }

    @Override
    public void addAll(Song... items) {
        super.addAll(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Song song = getItem(position); //skapa ny song för varje view (varje view är ett listitem)

        View view = convertView;

        if(view == null) {
            LayoutInflater inflater;
            inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.items_listview_search, null);
        }

        artistname = (TextView) view.findViewById(R.id.artistName);
        songname = (TextView) view.findViewById(R.id.songName);
        runtime = (TextView) view.findViewById(R.id.runTime);
        artistname.setTypeface(ROBOTO);
        songname.setTypeface(ROBOTO);
        runtime.setTypeface(ROBOTO);

        artistname.setText(song.getArtist());
        songname.setText(song.getSongName());
        runtime.setText(song.getRuntime());

        return view;
    }
}
