package se.zinokader.spotiq.presenter;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.UserPrivate;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import se.zinokader.spotiq.constants.Constants;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.Stickynotification;
import se.zinokader.spotiq.spotify.PreviewPlayer;
import se.zinokader.spotiq.spotify.SpotifyWebAPIHelper;
import se.zinokader.spotiq.view.PartyView;

import static se.zinokader.spotiq.constants.Constants.SNACKBAR_LENGTH_INDEFINITE;

public class PartyPresenterImpl implements PartyPresenter, PlayerNotificationCallback, ConnectionStateCallback {

    private PartyView view;
    private Party party;
    private Player player;
    private AuthenticationResponse response;

    private DatabaseReference songListReference;
    private ChildEventListener playlistchangelistener;

    private PlayerState playerstate = new PlayerState();
    private PreviewPlayer previewPlayer = new PreviewPlayer();
    private CountDownTimer progressUpdateTimer = null;

    private Boolean inbackground = false;
    private Boolean resumeafterpreview = false;
    private Boolean ishost = false;

    @Override
    public void setView(PartyView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        Spotify.destroyPlayer(this);
        previewPlayer.killPlayer();
        songListReference.removeEventListener(playlistchangelistener);
    }

    @Override
    public void inBackground(Boolean inbackground) {
        this.inbackground = inbackground;
    }

    @Override
    public void wentToBackground() {

        if(playerstate.playing) {
            SpotifyWebAPIHelper spotifyWebApiHelper = new SpotifyWebAPIHelper();
            spotifyWebApiHelper.getStickyNotification(playerstate.trackUri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Stickynotification>() {
                        @Override
                        public void onNext(Stickynotification stickynotification) {
                            view.updateNotificationService(stickynotification);
                        }

                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });
        }
    }

    @Override
    public Party getParty() {
        return this.party;
    }

    @Override
    public void setParty(Party party) {
        this.party = party;
    }

    @Override
    public void authenticate(Context context, AuthenticationResponse response) {

        this.response = response;

        if (player == null) {
            Config playerConfig = new Config(context, response.getAccessToken(), Constants.SPOTIFY_CLIENT_ID);
            player = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {
                    player.addConnectionStateCallback(PartyPresenterImpl.this);
                    player.addPlayerNotificationCallback(PartyPresenterImpl.this);
                    player.setPlaybackBitrate(PlaybackBitrate.BITRATE_HIGH);
                }

                @Override
                public void onError(Throwable error) {
                    Log.d("Error in initialization", error.getMessage());
                }
            });
        } else {
            player.login(response.getAccessToken());
        }

    }

    @Override
    public void setUserType(final String userid) {
        DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(party.getPartyName());
        partyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Party dbparty = dataSnapshot.getValue(Party.class);

                if( ! dbparty.getPartyHost().contentEquals(userid)) {
                    ishost = false;
                    view.removePlayPauseButton();
                    view.showSnackbar("Connected as a guest", Constants.SNACKBAR_LENGTH_LONG);
                } else {
                    ishost = true;
                    view.showSnackbar("Connected as a host", Constants.SNACKBAR_LENGTH_LONG);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void playOrPauseEvent() {

        DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(party.getPartyName());
        DatabaseReference songListReference = partyReference.child("tracklist");

        if (playerstate.playing) {
            player.pause();
        } else {
            songListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildren().iterator().hasNext()) {
                        Song firstinlist = dataSnapshot.getChildren().iterator().next().getValue(Song.class);
                        if (playerstate.trackUri != null && playerstate.trackUri.equals(firstinlist.getUri())) {
                            player.resume();
                        } else {
                            player.play(firstinlist.getUri());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void previewSong(Song song) {
        previewPlayer.playPreview(song.getPreviewUrl());
        view.showSnackbar("Previewing " + song.getSongName(), SNACKBAR_LENGTH_INDEFINITE);

        if(playerstate.playing) { //pausa låt om något spelas
            view.onPlayPauseFabClick();
            resumeafterpreview = true;
        }
    }

    @Override
    public void pausePreview() {
        previewPlayer.resetPlayer();

        if(resumeafterpreview) { //återuppta låt om något spelades
            view.onPlayPauseFabClick();
            resumeafterpreview = false; //återställ bool till nästa event
        }
    }

    @Override
    public void setSongLiked(final Song song, final Boolean liked) {

        final DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(party.getPartyName());
        final DatabaseReference songListReference = partyReference.child("tracklist");

        songListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot tracklistsnapshot) {

                //hämta username
                SpotifyWebAPIHelper spotifyWebAPIHelper = new SpotifyWebAPIHelper(response.getAccessToken());
                spotifyWebAPIHelper.getUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<UserPrivate>() {
                            @Override
                            public void call(UserPrivate userPrivate) {

                                //iterata genom låtar
                                for (DataSnapshot songsnapshot : tracklistsnapshot.getChildren()) {
                                    Song dbsong = songsnapshot.getValue(Song.class);
                                    if(dbsong.sameSongAs(song)) {
                                        if (liked) {
                                            ArrayList<String> dbvotedupby = dbsong.getVotedUpBy();
                                            dbvotedupby.add(userPrivate.id);
                                            songListReference.child(songsnapshot.getKey()).child("votedUpBy")
                                                    .setValue(dbvotedupby);
                                            return;
                                        } else {
                                            ArrayList<String> dbvotedupby = dbsong.getVotedUpBy();
                                            dbvotedupby.remove(userPrivate.id);
                                            songListReference.child(songsnapshot.getKey()).child("votedUpBy")
                                                    .setValue(dbvotedupby);
                                            return;
                                        }
                                    }
                                }

                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void startPlaylistListener() {

        DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(party.getPartyName());
        songListReference = partyReference.child("tracklist");

        ArrayList<Song> songlist = new ArrayList<Song>();
        view.attachPlaylist(songlist);

        playlistchangelistener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot childDataSnapshot, String s) {
                Song song = childDataSnapshot.getValue(Song.class);
                view.addPlaylistItem(song);
                Log.d("SONG ADDED DB", song.getSongName());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Song song = dataSnapshot.getValue(Song.class);
                view.removePlaylistItem(song);
                Log.d("SONG REMOVED DB", song.getSongName());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        songListReference.addChildEventListener(playlistchangelistener);

    }

    @Override
    public void onPlaybackEvent(EventType eventType, final PlayerState playerstatefinal) {
        this.playerstate = playerstatefinal;

        if (eventType.equals(EventType.PLAY)) {

            SpotifyWebAPIHelper spotifyWebApiHelper = new SpotifyWebAPIHelper();
            spotifyWebApiHelper.getStickyNotification(playerstate.trackUri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Stickynotification>() {
                        @Override
                        public void onNext(Stickynotification stickynotification) {

                            if(inbackground) {
                                view.updateNotificationService(stickynotification);
                            } else {
                                view.showSnackbar("Now playing " + stickynotification.getSongName(), Constants.SNACKBAR_LENGTH_LONG);
                            }

                        }

                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }
                    });

            if (playerstate.playing) {
                progressUpdateTimer = new CountDownTimer(playerstate.durationInMs, 200) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (!player.isShutdown()) {
                            player.getPlayerState(new PlayerStateCallback() {
                                @Override
                                public void onPlayerState(PlayerState playerStateUpdate) {
                                    float floatprogress = ((float) playerStateUpdate.positionInMs / (float) playerStateUpdate.durationInMs) * 10000;
                                    int progress = (int) floatprogress;
                                    view.updateSongProgress(progress);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFinish() {
                        view.updateSongProgress(0);
                    }
                }.start();
            }
        }

        if (eventType.equals(EventType.PAUSE)) {
            progressUpdateTimer.cancel();
        }

        if (eventType.equals(EventType.TRACK_END)) {

            final DatabaseReference partyReference = FirebaseDatabase.getInstance().getReference(party.getPartyName());
            final DatabaseReference songListReference = partyReference.child("tracklist");

            songListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //hämta key för första låten i listan
                    String firstinlist = dataSnapshot.getChildren().iterator().next().getKey();

                    //ta bort första låten i listan
                    songListReference.child(firstinlist).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            //nu har första låten tagits bort, spela nästa låt i listan
                            songListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildren().iterator().hasNext()) {
                                        Song nextsong = dataSnapshot.getChildren().iterator().next().getValue(Song.class);
                                        player.play(nextsong.getUri());
                                    } else { //inga fler låtar i listan
                                        player.clearQueue();
                                        view.updateSongProgress(0);
                                        view.onPlayPauseFabClick(); //resetta fab till att visa playikon
                                        view.showSnackbar("Looks like we're outta' songs", Constants.SNACKBAR_LENGTH_LONG);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errormsg) {
        Log.d("PLAYBACK ERROR", errormsg);
    }

    @Override
    public void onLoggedIn() {
    }

    @Override
    public void onLoggedOut() {
        view.showSessionExpired();
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        view.showSessionExpired();
        view.showSnackbar(throwable.getMessage(), Constants.SNACKBAR_LENGTH_LONG);
        Log.d("Spotify login failed", throwable.getMessage());
    }

    @Override
    public void onTemporaryError() {
        if(playerstate.playing) {
            view.onPlayPauseFabClick();
            view.showSnackbar("Connection error, pausing song", Constants.SNACKBAR_LENGTH_LONG);
        } else {
            view.showSnackbar("Connection error, please check your internet connection", Constants.SNACKBAR_LENGTH_LONG);
        }
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("Spotify Connection Msg", s);
    }
}
