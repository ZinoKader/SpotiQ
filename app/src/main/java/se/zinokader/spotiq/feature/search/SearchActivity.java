package se.zinokader.spotiq.feature.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;

import org.cryse.widget.persistentsearch.SimpleSearchListener;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivitySearchBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.listener.Debouncer;

public class SearchActivity extends BaseActivity implements SearchView {

    ActivitySearchBinding binding;
    private SearchPresenter presenter;
    private Debouncer debouncer = new Debouncer();

    public SearchActivity() {
        addPlugin(new TiActivityPlugin<>(SearchPresenter::new));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        binding.searchBar.openSearch();
        binding.searchBar.setHomeButtonListener(this::finish);
        binding.searchBar.setSearchListener(new SimpleSearchListener() {
            @Override
            public void onSearchTermChanged(String query) {
                debouncer.debounce(() -> presenter.searchTracks(query), ApplicationConstants.DEFAULT_DEBOUNCE_MS);
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
