package se.zinokader.spotiq.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.fabtransitionactivity.SheetLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import se.zinokader.spotiq.MvpApplication;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.presenter.PartyPresenter;
import se.zinokader.spotiq.view.PartyView;

public class PartyActivity extends BaseActivity implements PartyView, SheetLayout.OnFabAnimationEndListener {

    @Inject
    PartyPresenter partyPresenter;

    @BindView(R.id.activity_party_view)
    View view;
    @BindView(R.id.fab_add_song)
    FloatingActionButton fab;
    @BindView(R.id.fab_add_song_sheet)
    SheetLayout sheetlayout;

    private static final int REQUEST_CODE = 1337;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party);
        ((MvpApplication)getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);

        partyPresenter.setParty( (Party) getIntent().getExtras().getSerializable("party") );

        sheetlayout.setFab(fab);
        sheetlayout.setFabAnimationEndListener(this);
    }

    @OnClick(R.id.fab_add_song)
    public void onFabClick() {
        sheetlayout.expandFab();
    }

    @Override
    public void onFabAnimationEnd() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            sheetlayout.contractFab();
        }
    }

    @Override
    public void showSnackbar(String snacktext, int length) {
        Snackbar.make(view, snacktext, length)
                .show();
    }

}
