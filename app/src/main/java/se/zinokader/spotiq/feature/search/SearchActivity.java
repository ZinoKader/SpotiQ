package se.zinokader.spotiq.feature.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;

import org.cryse.widget.persistentsearch.PersistentSearchView;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.ActivitySearchBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.util.di.Injector;

public class SearchActivity extends BaseActivity implements SearchView {

    ActivitySearchBinding binding;
    private SearchPresenter presenter;

    public SearchActivity() {
        addPlugin(new TiActivityPlugin<>(SearchPresenter::new));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        binding.searchBar.openSearch();
        binding.searchBar.setHomeButtonListener(this::finish);
        binding.searchBar.setSearchListener(new PersistentSearchView.SearchListener() {
            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchTermChanged(String s) {

            }

            @Override
            public void onSearch(String s) {

            }

            @Override
            public void onSearchEditOpened() {

            }

            @Override
            public void onSearchEditClosed() {

            }

            @Override
            public boolean onSearchEditBackPressed() {
                return false;
            }

            @Override
            public void onSearchExit() {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startForegroundTokenRenewalService();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopForegroundTokenRenewalService();

    }

    @Override
    public void setPresenter(TiPresenter presenter) {
        this.presenter = (SearchPresenter) presenter;
        ((Injector) getApplication()).inject(presenter);
        this.presenter.init();
    }

    @Override
    public boolean isPresenterAttached() {
        return presenter != null;
    }
    @Override
    public View getRootView() {
        return binding.getRoot();
    }
}
