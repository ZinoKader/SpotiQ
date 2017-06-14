package se.zinokader.spotiq.feature.search.searchlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.type.Ignore;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.SongHolder> {

    private final PublishSubject<Song> onClickSubject = PublishSubject.create();
    private final PublishSubject<Song> onLongClickSubject = PublishSubject.create();
    private final PublishSubject<Ignore> onLongClickEndSubject = PublishSubject.create();
    private boolean curentlyLongClicking = false;
    private List<Song> songs = new ArrayList<>();

    @Override
    public SongHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflatedView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_row_search_song, viewGroup, false);
        inflatedView.getLayoutParams().width = viewGroup.getWidth();
        return new SongHolder(inflatedView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(SongHolder songHolder, int i) {

        Song song = songs.get(i);
        Context context = songHolder.itemView.getContext();

        songHolder.itemView.setOnClickListener(view -> onClickSubject.onNext(song));
        songHolder.itemView.setOnLongClickListener(view -> {
            curentlyLongClicking = true;
            onLongClickSubject.onNext(song);
            return true;
        });
        songHolder.itemView.setOnTouchListener((view, motionEvent) -> {
            view.onTouchEvent(motionEvent);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && curentlyLongClicking) {
                onLongClickEndSubject.onNext(new Ignore());
                curentlyLongClicking = false;
            }
            return true;
        });

        List<String> artists = new ArrayList<>();
        for (ArtistSimple artist : song.getArtists()) {
            artists.add(artist.name);
        }

        String artistsName = TextUtils.join(", ", artists);

        Glide.with(context)
                .load(song.getAlbumArtUrl())
                .fitCenter()
                .into(songHolder.albumArt);
        songHolder.songName.setText(song.getName());
        songHolder.artistsName.setText(artistsName);
        songHolder.albumName.setText(song.getAlbum().name);
    }

    @Override
    public void onViewRecycled(SongHolder songHolder) {
        if(songHolder != null) {
            songHolder.albumArt.setImageDrawable(null);
            Glide.clear(songHolder.albumArt);
        }
        super.onViewRecycled(songHolder);
    }

    public void addSongs(List<Song> songs) {
        this.songs.addAll(songs);
    }

    public Song getSong(int itemId) {
        return songs.get(itemId);
    }

    public void clearSongs() {
        songs.clear();
    }

    public Observable<Song> observeClicks() {
        return onClickSubject;
    }

    public Observable<Song> observeLongClicks() {
        return onLongClickSubject;
    }

    public Observable<Ignore> observeLongClickEnd() {
        return onLongClickEndSubject;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongHolder extends RecyclerView.ViewHolder {

        private ImageView albumArt;
        private TextView songName;
        private TextView artistsName;
        private TextView albumName;

        SongHolder(View view) {
            super(view);

            albumArt = view.findViewById(R.id.albumArt);
            songName = view.findViewById(R.id.songName);
            artistsName = view.findViewById(R.id.artistsName);
            albumName = view.findViewById(R.id.albumName);
        }


    }

}
