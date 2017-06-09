package se.zinokader.spotiq.feature.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        binding.searchView.setMenuItem(item);
        return true;
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }
}
