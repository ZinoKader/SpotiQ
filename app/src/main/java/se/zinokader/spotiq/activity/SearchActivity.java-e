package se.zinokader.spotiq.activity;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.zinokader.spotiq.MvpApplication;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.presenter.SearchPresenter;
import se.zinokader.spotiq.view.SearchView;

public class SearchActivity extends BaseActivity implements SearchView {

    @Inject
    SearchPresenter searchPresenter;

    @BindView(R.id.toolbar_search)
    Toolbar toolbar;
    TextView toolbartitle;
    @BindView(R.id.songSearchView)
    MaterialSearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ((MvpApplication)getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);
        toolbartitle = (TextView) toolbar.findViewById(R.id.toolbar_search_title);
        toolbartitle.setTypeface(ROBOTOLIGHT);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); //visa inte vanlig titel, istället nestad, egen textview
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //visa backbutton
        }

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_song, menu);

        MenuItem item = menu.findItem(R.id.action_search_songs);
        searchView.setMenuItem(item);
        searchView.setVoiceSearch(true);

        return true;
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
