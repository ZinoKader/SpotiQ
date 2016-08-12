package se.zinokader.spotiq.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.fabtransitionactivity.SheetLayout;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.SpotiqApplication;
import se.zinokader.spotiq.adapter.PlaylistRecyclerViewAdapter;
import se.zinokader.spotiq.adapter.PreCachingLayoutManager;
import se.zinokader.spotiq.constants.Constants;
import se.zinokader.spotiq.custom.RecyclerViewZ;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.Stickynotification;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.presenter.PartyPresenter;
import se.zinokader.spotiq.service.NotificationControlService;
import se.zinokader.spotiq.spotify.SpotifyWebAPIHelper;
import se.zinokader.spotiq.util.ImageUtils;
import se.zinokader.spotiq.view.PartyView;

public class PartyActivity extends BaseActivity implements PartyView, SheetLayout.OnFabAnimationEndListener {

    @Inject
    PartyPresenter partyPresenter;

    @BindView(R.id.activity_party_view)
    View view;
    @BindView(R.id.toolbar_party)
    Toolbar toolbar;
    @BindView(R.id.toolbar_party_title)
    TextView toolbartitle;
    @BindView(R.id.toolbar_user_profile_name)
    TextView profilename;
    @BindView(R.id.toolbar_user_profile_image)
    CircularImageView profileimage;
    @BindView(R.id.fab_play_pause)
    FloatingActionButton playpausefab;
    @BindView(R.id.songProgressBar)
    ProgressBar songprogressbar;
    @BindView(R.id.recyclerview_playlist)
    RecyclerViewZ recyclerview;
    @BindView(R.id.empty_recyclerview_playlist)
    View emptyview;
    @BindView(R.id.fab_add_song)
    FloatingActionButton addsongfab;
    @BindView(R.id.fab_add_song_sheet)
    SheetLayout sheetlayout;

    private BroadcastReceiver broadcastreceiver;
    IntentFilter broadcastfilter = new IntentFilter(Constants.NOTIFICATION_PLAYPAUSE_ACTION);

    private Snackbar snackbar;
    private MaterialDialog sessionexpireddialog;
    private PlaylistRecyclerViewAdapter playlistadapter;
    private Animatable playpauseicon;
    private Boolean showplayicon = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party);
        ((SpotiqApplication) getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);

        partyPresenter.setView(this);
        final Bundle bundle = getIntent().getBundleExtra("lobbybundle");
        partyPresenter.setParty((Party) bundle.getParcelable("party"));
        partyPresenter.authenticate(this, (AuthenticationResponse) bundle.getParcelable("response"));
        partyPresenter.setUserType(datastore.getString(Constants.USER_ID));
        partyPresenter.startPlaylistListener();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //ta bort defualt title
        toolbartitle.setText(partyPresenter.getParty().getPartyName());
        toolbartitle.setTypeface(ROBOTOLIGHT);
        profilename.setTypeface(ROBOTOLIGHT);

        profilename.setText(datastore.getString(Constants.PROFILE_NAME));
        Glide.with(this).load(datastore.getString(Constants.PROFILE_IMAGE)).into(profileimage);

        playpauseicon = (Animatable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_to_pause, getResources().newTheme());
        playpausefab.setImageDrawable( (Drawable) playpauseicon);
        sheetlayout.setFab(addsongfab);
        sheetlayout.setFabAnimationEndListener(this);

        recyclerview.setLayoutManager(new PreCachingLayoutManager(this));
        LandingAnimator itemanimator = new LandingAnimator();
        itemanimator.setInterpolator(new FastOutSlowInInterpolator());
        recyclerview.setItemAnimator(itemanimator);
        recyclerview.getItemAnimator().setAddDuration(650);
        recyclerview.getItemAnimator().setRemoveDuration(400);
        recyclerview.getItemAnimator().setMoveDuration(200);
        recyclerview.getItemAnimator().setChangeDuration(200);

        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if(addsongfab.isShown()) {
                        addsongfab.hide();
                    }
                } else if(dy < 0) {
                    if(!addsongfab.isShown()) {
                        addsongfab.show();
                    }
                }
                //om recyclerview inte går att scrolla (för få låtar), visa addsongfab igen
                if(!(recyclerView.computeHorizontalScrollRange() > recyclerView.getWidth() || recyclerView.computeVerticalScrollRange() > recyclerView.getHeight())) {
                    addsongfab.show();
                }
            }
        });

        //stoppa songpreview vid fingerlyft utanför item
        recyclerview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        onItemLongClickEnded();
                }
                return false;
            }
        });

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbar("You can edit your profile in the lobby", Snackbar.LENGTH_LONG);
            }
        });

        sessionexpireddialog = new MaterialDialog.Builder(this)
                .title(R.string.spotify_session_expired)
                .titleColorRes(R.color.materialWhite)
                .content(R.string.spotify_session_expired_content)
                .contentColorRes(R.color.materialWhite)
                .backgroundColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.materialWhite)
                .positiveText(R.string.ok)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivity(new Intent(PartyActivity.this, LoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                })
                .build();

        SpotifyWebAPIHelper test = new SpotifyWebAPIHelper( ((AuthenticationResponse) bundle.getParcelable("response")).getAccessToken());
        test.getUserPlaylists()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pager<PlaylistSimple>>() {
                    @Override
                    public void call(Pager<PlaylistSimple> playlistSimplePager) {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        partyPresenter.setView(this);
        partyPresenter.inBackground(false);

        Intent notificationserviceintent = new Intent(PartyActivity.this, NotificationControlService.class);
        notificationserviceintent.setAction(Constants.NOTIFICATION_STOPFOREGROUND_ACTION);
        stopService(notificationserviceintent);

        if(broadcastreceiver != null) this.unregisterReceiver(broadcastreceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        partyPresenter.inBackground(true);
        partyPresenter.wentToBackground();

        broadcastreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onPlayPauseFabClick(); //simulera pause/play vid broadcast av click från notificationcontrolservice
            }
        };
        this.registerReceiver(broadcastreceiver, broadcastfilter);
    }

    @Override
    protected void onDestroy() {
        partyPresenter.detach();
        super.onDestroy();

        Intent notificationserviceintent = new Intent(PartyActivity.this, NotificationControlService.class);
        notificationserviceintent.setAction(Constants.NOTIFICATION_STOPFOREGROUND_ACTION);
        stopService(notificationserviceintent);

        if(broadcastreceiver != null) this.unregisterReceiver(broadcastreceiver);
    }

    @Override
    public void attachPlaylist(ArrayList<Song> songs) {
        playlistadapter = new PlaylistRecyclerViewAdapter(this, this, songs);
        recyclerview.setAdapter(playlistadapter);
        updateEmptyView();
    }

    @Override
    public void updateEmptyView() {
        if(playlistadapter != null && playlistadapter.getItemCount() == 0) {
            emptyview.setVisibility(View.VISIBLE);
        } else {
            emptyview.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateNotificationService(Stickynotification stickynotification) {
        Intent notificationserviceintent = new Intent(PartyActivity.this, NotificationControlService.class);
        notificationserviceintent.setAction(Constants.NOTIFICATION_STARTFOREGROUND_ACTION);
        notificationserviceintent.putExtra("songname", stickynotification.getSongName());
        notificationserviceintent.putExtra("artist", stickynotification.getArtist());
        notificationserviceintent.putExtra("albumcoverurl", stickynotification.getAlbumCoverUrl());
        startService(notificationserviceintent);
    }

    @Override
    public void addPlaylistItem(Song song) {
        playlistadapter.addSong(song);
        updateEmptyView();
    }

    @Override
    public void removePlaylistItem(Song song) {
        playlistadapter.removeSong(song);
        updateEmptyView();
    }

    @OnClick(R.id.fab_play_pause)
    public void onPlayPauseFabClick() {
        partyPresenter.playOrPauseEvent();
        //animera play->pause och vice versa
        if (playpauseicon != null) {
            if(showplayicon) { //play->pause
                showplayicon = false;
                playpauseicon = (Animatable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_to_pause, getResources().newTheme());
                playpausefab.setImageDrawable( (Drawable) playpauseicon);
                playpauseicon.start();
            } else { //pause->play
                showplayicon = true;
                playpauseicon = (Animatable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_to_play, getResources().newTheme());
                playpausefab.setImageDrawable( (Drawable) playpauseicon);
                playpauseicon.start();
            }
        }
    }

    @Override
    public void onItemLongClicked(Song song) {
        partyPresenter.previewSong(song);
    }

    @Override
    public void onItemLongClickEnded() {
        partyPresenter.pausePreview();
        if(snackbar.isShownOrQueued()) { snackbar.dismiss(); }
    }

    @Override
    public void onItemLikePressed(Song song, Boolean liked) {
        partyPresenter.setSongLiked(song, liked);
    }

    @Override
    public void updateSongProgress(int progress) {
        songprogressbar.setProgress(progress);
    }

    @OnClick(R.id.fab_add_song)
    public void onAddSongFabClick() {
        sheetlayout.expandFab();
    }

    @Override
    public void onFabAnimationEnd() {
        Intent i = new Intent(this, SearchActivity.class);
        i.putExtra(Constants.RESPONSE, getIntent().getBundleExtra("lobbybundle").getParcelable("response"));
        i.putExtra(Constants.USER, new User(this, profilename.getText().toString(), ImageUtils.drawableToBitmap(profileimage.getDrawable())));
        i.putExtra(Constants.PARTY, partyPresenter.getParty());
        startActivityForResult(i, 1337);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items_party, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sheetlayout.contractFab();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerview.smoothScrollToPosition(playlistadapter.getItemCount());
            }
        }, 900);
    }

    @Override
    public void showSnackbar(String snacktext, int length) {
        snackbar = Snackbar.make(view, snacktext, length);
        snackbar.show();
    }

    @Override
    public void showSessionExpired() {
        sessionexpireddialog.show();
    }

    @Override
    public void removePlayPauseButton() {
        playpausefab.setVisibility(View.INVISIBLE);
    }

}
