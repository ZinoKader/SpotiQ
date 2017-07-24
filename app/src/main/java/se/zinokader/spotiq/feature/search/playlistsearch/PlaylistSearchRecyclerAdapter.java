package se.zinokader.spotiq.feature.search.playlistsearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.User;

public class PlaylistSearchRecyclerAdapter extends RecyclerView.Adapter<PlaylistSearchRecyclerAdapter.PlaylistHolder> {

    private final PublishSubject<PlaylistSimple> onClickSubject = PublishSubject.create();
    private List<PlaylistSimple> playlists = new ArrayList<>();

    @Override
    public PlaylistHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflatedView = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.recyclerview_row_search_playlist, viewGroup, false);
        inflatedView.getLayoutParams().width = viewGroup.getWidth();
        return new PlaylistHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(PlaylistHolder playlistHolder, int i) {

        PlaylistSimple playlist = playlists.get(i);
        User playlistCreator = new User(playlist.owner.id, playlist.owner.display_name, playlist.owner.images);
        Context context = playlistHolder.itemView.getContext();

        playlistHolder.itemView.setOnClickListener(view -> onClickSubject.onNext(playlist));

        Glide.with(context)
            .load(playlist.images.get(0).url)
            .fitCenter()
            .into(playlistHolder.playlistArt);
        playlistHolder.playlistName.setText(playlist.name);
        playlistHolder.playlistCreatorName.setText(playlistCreator.getUserName());
        playlistHolder.songCount.setText(playlist.tracks.total + " tracks");
    }

    @Override
    public void onViewRecycled(PlaylistHolder playlistHolder) {
        if(playlistHolder != null) {
            playlistHolder.playlistArt.setImageDrawable(null);
            Glide.clear(playlistHolder.playlistArt);
        }
        super.onViewRecycled(playlistHolder);
    }

    void updatePlaylists(List<PlaylistSimple> playlists) {
        this.playlists = playlists;
    }

    public PlaylistSimple getPlaylist(int itemId) {
        return playlists.get(itemId);
    }

    Observable<PlaylistSimple> observeClicks() {
        return onClickSubject;
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class PlaylistHolder extends RecyclerView.ViewHolder {

        private ImageView playlistArt;
        private TextView playlistName;
        private TextView playlistCreatorName;
        private TextView songCount;

        PlaylistHolder(View view) {
            super(view);

            playlistArt = view.findViewById(R.id.playlistArt);
            playlistName = view.findViewById(R.id.playlistName);
            playlistCreatorName = view.findViewById(R.id.playlistCreatorName);
            songCount = view.findViewById(R.id.songCount);
        }


    }

}
