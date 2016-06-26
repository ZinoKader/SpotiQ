package se.zinokader.spotiq.spotify;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Single;
import se.zinokader.spotiq.model.Party;

import static android.media.CamcorderProfile.get;

public class TrackInfoParser {

    String spotifyURI;

    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();

    public void setSpotifyURI(String spotifyURI) {
        this.spotifyURI = spotifyURI.replace("spotify:track:", "");
    }

    public String getTrackName() { //network call, bara i BG thread
        return spotify.getTrack(spotifyURI).name;
    }

    public void getSearchedTracks(String searchquery, final Party party) {

        spotify.searchTracks(searchquery, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {

                List<Track> tracklist = tracksPager.tracks.items;

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dbref = database.getReference(party.getPartyname());

                ArrayList<String> trackarray = new ArrayList<String>();

                for(int i = 0; i <= 15; i++) {
                    trackarray.add(tracklist.get(i).name);
                    if(i == 15) {
                        party.setTracklist(trackarray);
                        dbref.setValue(party);
                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }


}
