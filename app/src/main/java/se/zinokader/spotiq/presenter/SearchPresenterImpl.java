package se.zinokader.spotiq.presenter;

import android.os.Handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Playlist;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.spotify.PreviewPlayer;
import se.zinokader.spotiq.spotify.SpotifyWebAPIHelper;
import se.zinokader.spotiq.util.ImageUtils;
import se.zinokader.spotiq.view.SearchView;

import static se.zinokader.spotiq.constants.Constants.SNACKBAR_LENGTH_INDEFINITE;
import static se.zinokader.spotiq.constants.Constants.SNACKBAR_LENGTH_LONG;
import static se.zinokader.spotiq.constants.Constants.SNACKBAR_LENGTH_SHORT;

 
public class SearchPresenterImpl implements SearchPresenter {

    private SearchView view;
    private AuthenticationResponse response;
    private Party party;
    private User user;
    private PreviewPlayer previewPlayer = new PreviewPlayer();

    @Override
    public void setView(SearchView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        previewPlayer.killPlayer();
    }

    @Override
    public void setResponse(AuthenticationResponse response) {
        this.response = response;
    }

    @Override
    public void setParty(Party party) {
        this.party = party;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public Party getParty() {
        return this.party;
    }

    @Override
    public void searchTracks(final String querytext) {

        final ArrayList<Song> songarray = new ArrayList<>();
        SpotifyWebAPIHelper spotifyWebApiHelper = new SpotifyWebAPIHelper(response.getAccessToken());
        spotifyWebApiHelper.getSongList(querytext, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Track>>() {
                    @Override
                    public void onNext(List<Track> songlist) {
                        view.showProgressBar();
                        for (int next = 0; next < songlist.size(); next++) {
                            try { //låtar utan en parameter catchas (t.ex. ingen albumbild)
                                songarray.add(new Song(
                                        songlist.get(next).artists.get(0).name,
                                        songlist.get(next).name,
                                        songlist.get(next).duration_ms,
                                        songlist.get(next).uri,
                                        songlist.get(next).preview_url,
                                        songlist.get(next).album.images.get(0).url));
                            } catch(IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {
                        view.showSnackbar("Found " + songarray.size() + " songs matching your criterion", SNACKBAR_LENGTH_SHORT);
                        view.updateSearchList(songarray);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                view.hideProgressBar();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //searchtracks triggas av att öppna searchview (querytext.length() == 0), vi vill bara ha fails på riktiga sökningar
                        e.printStackTrace();
                        if (querytext.length() > 0) {
                            view.retryWithSnackbar("Error fetching songs", "Try again", querytext, SNACKBAR_LENGTH_SHORT);
                        }
                    }
                });

    }

    @Override
    public void songSelected(final Song song) {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getParty().getPartyName());

        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Iterable<DataSnapshot> dbsongentries = dataSnapshot.child("tracklist").getChildren();

                        //kolla om låten redan är queuead
                        for(DataSnapshot dbentry : dbsongentries) {
                            if(dbentry.getValue(Song.class).sameSongAs(song)) {
                                view.showSnackbar("This song is already in the queue", SNACKBAR_LENGTH_LONG);
                                return; //om den redan är queuead, returnera utan att fortsätta med att lägga till låten
                            }
                        }

                        //generera unik nyckel (baserat på timestamp: https://firebase.google.com/docs/database/android/save-data#append_to_a_list_of_data)
                        String songkey = databaseReference.child("tracklist").push().getKey();

                        //sätt info om användaren som lägger till låten, komprimera profilbild
                        song.setAddedByProfileName(user.getProfileName());
                        song.setAddedByProfilePicture(ImageUtils.bitmapToByteArrayCompressed(user.getProfilePicture(), 60));

                        //sätt songobjekt som value på den unika nyckeln
                        databaseReference.child("tracklist").child(songkey).setValue(song);

                        view.finish();
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        view.showSnackbar("Error adding song to playlist", SNACKBAR_LENGTH_LONG);
                    }
                });

    }

    @Override
    public void searchPlaylist(final Playlist playlist) {

        final ArrayList<Song> songarray = new ArrayList<>();

        SpotifyWebAPIHelper spotifyWebAPIHelper = new SpotifyWebAPIHelper(response.getAccessToken());
        spotifyWebAPIHelper.getSongsFromPlaylist(playlist.getOwnerId(), playlist.getId(), 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(new Func1<List<PlaylistTrack>, Observable<?>>() {
                    @Override
                    public Observable<?> call(List<PlaylistTrack> playlist) {
                        //skapa observables från alla playlisttrackobjekt
                        return Observable.from(playlist);
                    }
                })
                .toList() //skapa en lista av dessa observables
                .subscribe(new Subscriber<List<Object>>() {
                    @Override
                    public void onCompleted() {
                        view.showSnackbar("Found " + songarray.size() + " playable songs in the playlist", SNACKBAR_LENGTH_SHORT);
                        view.updateSearchList(songarray);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Object> objects) {
                        List<? extends Object> objectlist = objects;
                        List<PlaylistTrack> playlist = (List<PlaylistTrack>) objectlist;

                        for (int i = 0; i < playlist.size(); i++) {
                            if (!playlist.get(i).is_local) {
                                Song song = new Song(
                                        playlist.get(i).track.artists.get(0).name,
                                        playlist.get(i).track.name,
                                        playlist.get(i).track.duration_ms,
                                        playlist.get(i).track.uri,
                                        playlist.get(i).track.preview_url,
                                        playlist.get(i).track.album.images.get(0).url);
                                songarray.add(song);
                            }
                        }

                    }
                });
    }

    @Override
    public void populateUserPlaylists() {
        SpotifyWebAPIHelper spotifyWebAPIHelper = new SpotifyWebAPIHelper(response.getAccessToken());
        spotifyWebAPIHelper.getUserPlaylists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pager<PlaylistSimple>>() {
                    @Override
                    public void onNext(Pager<PlaylistSimple> playlistSimplePager) {
                        ArrayList<Playlist> playlistarray = new ArrayList<>();

                        for(int i = 0; i < playlistSimplePager.items.size(); i++) {
                            Playlist playlist = new Playlist();
                            try {
                                playlist.setPlaylistName(playlistSimplePager.items.get(i).name);
                                playlist.setSongCount(playlistSimplePager.items.get(i).tracks.total);
                                playlist.setPlaylistArtUrl(playlistSimplePager.items.get(i).images.get(0).url);
                                playlist.setId(playlistSimplePager.items.get(i).id);
                                playlist.setOwnerId(playlistSimplePager.items.get(i).owner.id);
                            } catch(IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                            playlistarray.add(playlist);
                        }

                        view.updatePlaylists(playlistarray);
                    }
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void previewSong(Song song) {
        previewPlayer.playPreview(song.getPreviewUrl());
        view.showSnackbar("Previewing " + song.getSongName(), SNACKBAR_LENGTH_INDEFINITE);
    }

    @Override
    public void pausePreview() {
        previewPlayer.resetPlayer();
    }


}
