package se.zinokader.spotiq.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.zinokader.spotiq.MvpApplication;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.presenter.LobbyPresenter;
import se.zinokader.spotiq.view.LobbyView;

public class LobbyActivity extends BaseActivity implements LobbyView {

    @Inject
    LobbyPresenter lobbyPresenter;

    @BindView(R.id.activity_lobby_view)
    View view;
    @BindView(R.id.logoTextViewHome)
    TextView logoTextViewHome;
    @BindView(R.id.create_party_button)
    Button createpartybutton;
    @BindView(R.id.join_party_button)
    Button joinpartybutton;
    @BindView(R.id.play_button)
    Button playbutton;
    @BindView(R.id.pause_button)
    Button pausebutton;
    @BindView(R.id.next_button)
    Button nextbutton;

    View showpartydialogview;
    TextInputEditText partynameinput;
    TextInputEditText partypasswordinput;

    public static Intent intent;
    public static int resultCode;
    public static int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ((MvpApplication)getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);
        logoTextViewHome.setTypeface(BACKTOBLACK);
        createpartybutton.setTypeface(ROBOTOLIGHT);
        joinpartybutton.setTypeface(ROBOTOLIGHT);

        lobbyPresenter.setupPlayer(this, intent, resultCode, requestCode);

        createpartybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lobbyPresenter.showPartyDialog();
            }
        });

        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lobbyPresenter.playSong("spotify:track:2714ySK1pbOIGZwABmRyAz");
            }
        });

        pausebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lobbyPresenter.pauseSong();
            }
        });

        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lobbyPresenter.nextSong();
            }
        });

    }

    @Override
    public void showPartyDialog() {
        MaterialDialog showpartydialog = new MaterialDialog.Builder(this)
                .title(R.string.create_party)
                .customView(R.layout.dialog_create_party, true)
                .positiveText(R.string.create_party)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showpartydialogview = dialog.getCustomView();

                        Party party = new Party();
                        partynameinput = (TextInputEditText) showpartydialogview.findViewById(R.id.partyName);
                        partypasswordinput = (TextInputEditText) showpartydialogview.findViewById(R.id.partyPassword);

                        party.setPartyname(partynameinput.getText().toString());
                        party.setPartypassword(partypasswordinput.getText().toString());

                        lobbyPresenter.createParty(party);
                    }
                })
                .build();
        showpartydialog.show();
    }

    @Override
    public void goToParty(Party party) {
        Intent i = new Intent(LobbyActivity.this, PartyActivity.class);
        i.putExtra("party", party);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lobbyPresenter.setView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lobbyPresenter.detach();
    }

    @Override
    public void showSnackbar(String snacktext, int length) {
        Snackbar.make(view, snacktext, length)
        .show();
    }

}
