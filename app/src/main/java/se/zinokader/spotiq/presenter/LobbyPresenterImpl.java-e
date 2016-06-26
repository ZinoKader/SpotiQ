package se.zinokader.spotiq.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import se.zinokader.spotiq.activity.LobbyActivity;
import se.zinokader.spotiq.activity.PartyActivity;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.spotify.TrackInfoParser;
import se.zinokader.spotiq.view.LobbyView;

import static android.os.Build.VERSION_CODES.M;


public class LobbyPresenterImpl implements LobbyPresenter, ConnectionStateCallback, PlayerNotificationCallback {

    private LobbyView view;
    private Player mPlayer;
    private Boolean mPlayerIsReady = false;
    private TrackInfoParser trackInfo = new TrackInfoParser();

    List<String> songList = new ArrayList<String>();

    static final String CLIENT_ID = "5646444c2abc4d8299ee3f2cb274f0b6";
    static final int REQUEST_CODE = 1337;

    @Override
    public void setView(LobbyView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        Spotify.destroyPlayer(this);
    }

    @Override
    public void setupPlayer(Context context, Intent intent, int resultCode, int requestCode) {
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(context, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(LobbyPresenterImpl.this);
                        mPlayer.addPlayerNotificationCallback(LobbyPresenterImpl.this);
                        mPlayerIsReady = true;

                        songList.add("spotify:track:2714ySK1pbOIGZwABmRyAz");
                        songList.add("spotify:track:6Hr77eZSmaVGgH0vVulMUH");
                        songList.add("spotify:track:5UQ1cDe7Bb9OB9ZQcUvoS1");
                        songList.add("spotify:track:6rqj2zeKhLy3exkuFi6mSz");
                        songList.add("spotify:track:6oSnzvS5OxzwxSpkJrak3Z");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("Play error", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void showPartyDialog() {
        view.showPartyDialog();
    }

    @Override
    public void createParty(final Party party) {

        Boolean inputsanitized = party.getPartyname().length() >= 3 && party.getPartypassword().length() >= 3;

        if(inputsanitized) {
            view.showSnackbar("New party \"" + party.getPartyname() + "\" created", 0);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbref = database.getReference(party.getPartyname());

            dbref.setValue(party, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    view.goToParty(party);

                    //TODO: Move this test into real function that updates the tracklist
                    trackInfo.getSearchedTracks("hey", party);
                }
            });
        }
        else {
            view.showSnackbar("Failed to create party: name or password too short", 0);
        }

    }

    @Override
    public void playSong(final String songURI) {
        if (mPlayerIsReady) {
            mPlayer.play(songURI);
            mPlayer.play(songList);
        }
    }

    @Override
    public void pauseSong() {
        if (mPlayerIsReady) {
            mPlayer.pause();
        }
    }

    @Override
    public void nextSong() {
        if (mPlayerIsReady) {
            mPlayer.skipToNext();
        }
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

        trackInfo.setSpotifyURI(playerState.trackUri);

        if (eventType == EventType.PLAY || eventType == EventType.TRACK_CHANGED) {

            Single<String> trackNameSingle = Single.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return trackInfo.getTrackName();
                }
            });

            trackNameSingle
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<String>() {
                        @Override
                        public void onSuccess(String trackname) {
                            view.showSnackbar("Now playing " + trackname, 0);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });

        }

    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        view.showSnackbar("Playback error: " + s, 0);
        Log.d("PlaybackError", "Error msg: " + s);
    }
}
