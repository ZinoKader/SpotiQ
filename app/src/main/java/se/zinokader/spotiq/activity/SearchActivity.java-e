package se.zinokader.spotiq.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.SpotiqApplication;
import se.zinokader.spotiq.adapter.EmptySearchArrayAdapter;
import se.zinokader.spotiq.adapter.SearchArrayAdapter;
import se.zinokader.spotiq.constants.Constants;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.model.Playlist;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.presenter.SearchPresenter;
import se.zinokader.spotiq.view.SearchView;

public class SearchActivity extends BaseActivity implements SearchView {

    @Inject
    SearchPresenter searchPresenter;
    @BindView(R.id.activity_search_view)
    View view;
    @BindView(R.id.toolbar_search)
    Toolbar toolbar;
    TextView toolbartitle;
    @BindView(R.id.songSearchView)
    MaterialSearchView searchView;
    @BindView(R.id.searchProgressBar)
    ProgressBar searchProgressBar;
    @BindView(R.id.searchListView)
    ListView searchListView;
    ListView emptyListView;
    TextView emptyListViewText;
    TextView emptyListViewText2;

    private SearchArrayAdapter searchadapter;
    private EmptySearchArrayAdapter emptysearchadapter;
    private String handlerquerytext;
    private Handler searchHandler = new Handler();
    private Snackbar snackbar;

    private Boolean useraddedsong = false;
    private Boolean inplaylistsearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ((SpotiqApplication) getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);
        toolbartitle = (TextView) toolbar.findViewById(R.id.toolbar_search_title);
        toolbartitle.setTypeface(ROBOTOLIGHT);
        setSupportActionBar(toolbar);

        searchPresenter.setResponse((AuthenticationResponse) getIntent().getParcelableExtra("response"));
        searchPresenter.setParty((Party) getIntent().getParcelableExtra(Constants.PARTY));
        searchPresenter.setUser((User) getIntent().getParcelableExtra(Constants.USER));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); //visa inte vanlig titel, istället nestad, egen textview
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //visa backbutton
        }

        //Initiera emptylistview
        View emptyView = getLayoutInflater().inflate(R.layout.empty_listview_search, null);
        emptyListView = (ListView) emptyView.findViewById(R.id.empty_listview_search_playlists);
        emptyListViewText = (TextView) emptyView.findViewById(R.id.empty_listview_search_hint);
        emptyListViewText2 = (TextView) emptyView.findViewById(R.id.empty_listview_search_hint_below);
        emptyListViewText.setTypeface(ROBOTOLIGHT);
        emptyListViewText2.setTypeface(ROBOTOLIGHT);

        emptysearchadapter = new EmptySearchArrayAdapter(this, R.layout.items_listview_search_playlists, new ArrayList<Playlist>());
        emptyListView.setAdapter(emptysearchadapter);
        ((ViewGroup)searchListView.getParent()).addView(emptyView);
        searchListView.setEmptyView(emptyView);

        searchPresenter.populateUserPlaylists();

        //användare är dumma och ser inte searchikonen, hela toolbaren öppnar search också
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.showSearch();
            }
        });

        //uppdatera listan efter search
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String querytext) {
                if(querytext != null && querytext.length() > 0) {
                    searchPresenter.searchTracks(querytext);
                } else {
                    if(searchadapter != null) {
                        searchadapter.clear();
                        searchadapter.notifyDataSetChanged();
                    }
                }
                searchView.hideKeyboard(view);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String querytext) {
                //söker efter 400ms, om mindre än 400ms gått så avbryts tidigare sökning
                //det här förhindrar onödiga calls till spotify
                handlerquerytext = querytext;
                searchHandler.removeCallbacksAndMessages(null);

                searchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(handlerquerytext != null && handlerquerytext.length() > 0) {
                            searchPresenter.searchTracks(handlerquerytext);
                        } else {
                            if(searchadapter != null) {
                                searchadapter.clear();
                                searchadapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, 400);

                return true;
            }

        });

        //spellista vald
        emptyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Playlist playlist = (Playlist) adapterView.getItemAtPosition(position);
                searchPresenter.searchPlaylist(playlist);
                inplaylistsearch = true;
            }
        });

        //låt vald normal click
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View itemview, int position, long l) {
                final Song selectedsong = (Song) adapterView.getItemAtPosition(position);
                Snackbar.make(view, "Add \"" + selectedsong.getSongName() + "\" to the playlist?", Snackbar.LENGTH_LONG)
                        .setAction("Confirm", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                useraddedsong = true;
                                searchPresenter.songSelected(selectedsong);
                            }
                        })
                        .show();
            }
        });

        //låtpreview longclick
        searchListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                searchPresenter.previewSong((Song) adapterView.getItemAtPosition(position));

                searchListView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if(snackbar.isShownOrQueued()) { snackbar.dismiss(); }
                                searchPresenter.pausePreview();
                        }
                        return false;
                    }
                });
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        searchPresenter.setView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchPresenter.detach();
    }

    @Override
    public void finish() {
        //avsluta med data om användaren la till en låt eller inte
        Intent finishintent = new Intent();
        if(useraddedsong) finishintent.setData(Uri.parse(Constants.USER_ADDED_SONG));
        else finishintent.setData(Uri.parse(""));
        setResult(RESULT_OK, finishintent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if(inplaylistsearch) { //bakåtknapp går tillbaka till playlistvy (tom listview)
            searchadapter.clear(); //ger tom listview
            inplaylistsearch = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void updateSearchList(ArrayList<Song> songarray) {
        if (searchListView.getAdapter() == null) {
            searchadapter = new SearchArrayAdapter(this, R.layout.items_listview_search, songarray);
            searchListView.setAdapter(searchadapter);
        } else {
            searchadapter.clear();
            searchadapter.addAll(songarray);
            searchadapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updatePlaylists(ArrayList<Playlist> playlistarray) {
        emptysearchadapter.addAll(playlistarray);
        emptysearchadapter.notifyDataSetChanged();
    }

    @Override
    public void showProgressBar() {
        searchProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        searchProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showSnackbar(String snacktext, int length) {
        snackbar = Snackbar.make(view, snacktext, length);
        snackbar.show();
    }

    @Override
    public void retryWithSnackbar(String snacktext, String actiontext, final String querytext, int length) {
        snackbar = Snackbar.make(view, snacktext, length)
                .setAction(actiontext, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchPresenter.searchTracks(querytext);
                    }
                });
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_song, menu);

        MenuItem searchitem = menu.findItem(R.id.action_search_songs);
        searchView.setMenuItem(searchitem);
        searchView.setSubmitOnClick(false);
        searchView.setVoiceSearch(true);
        searchView.showVoice(true);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
