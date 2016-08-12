package se.zinokader.spotiq.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;
import com.fuck_boilerplate.rx_paparazzo.entities.Options;
import com.fuck_boilerplate.rx_paparazzo.entities.Response;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.mukesh.tinydb.TinyDB;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.SpotiqApplication;
import se.zinokader.spotiq.constants.Constants;
import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.presenter.LobbyPresenter;
import se.zinokader.spotiq.view.LobbyView;
import uk.co.barbuzz.beerprogressview.BeerProgressView;

public class LobbyActivity extends BaseActivity implements LobbyView {

    @Inject
    LobbyPresenter lobbyPresenter;

    @BindView(R.id.activity_lobby_view)
    View view;
    @BindView(R.id.lobby_user_profile_image)
    ImageView profileimage;
    @BindView(R.id.lobby_user_profile_name)
    TextView displayname;
    @BindView(R.id.spotiq_logo)
    ImageView spotiqlogo;
    @BindView(R.id.create_party_layout)
    RelativeLayout createpartylayout;
    @BindView(R.id.join_party_layout)
    RelativeLayout joinpartylayout;
    @BindView(R.id.create_party_text)
    TextView createpartytext;
    @BindView(R.id.join_party_text)
    TextView joinpartytext;
    @BindView(R.id.beer_progress_view)
    BeerProgressView beerprogressview;

    View showpartydialogview;
    EditText partynameinput;
    EditText partypasswordinput;

    private MaterialDialog setinformationdialog;
    private EditText profilenameedittext;
    private CircularImageView fromgallerybutton;
    private Boolean shouldcloseinfodialog = false;

    private MaterialDialog createpartydialog;
    private MaterialDialog joinpartydialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ((SpotiqApplication)getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);

        //låt nya bounds för spotiqloggan beräknas och starta sedan tranisition
        postponeEnterTransition();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startPostponedEnterTransition();
            }
        }, 100);

        final Bundle responsebundle = getIntent().getBundleExtra("responsebundle");
        lobbyPresenter.setUserId(datastore, (AuthenticationResponse) responsebundle.getParcelable("response"));

        createpartytext.setTypeface(ROBOTOLIGHT);
        joinpartytext.setTypeface(ROBOTOLIGHT);


        setinformationdialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_setinformation_title)
                .titleColorRes(R.color.materialWhite)
                .backgroundColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.materialWhite)
                .negativeColorRes(R.color.materialWhite)
                .customView(R.layout.dialog_setinformation_lobby, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(shouldcloseinfodialog) dialog.dismiss();
                        else showSnackbar("Set a profile name and image to continue", Snackbar.LENGTH_LONG);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(shouldcloseinfodialog) {
                            dialog.dismiss();
                            if(profilenameedittext.getText().toString().length() >= 5) {
                                datastore.putString(Constants.PROFILE_NAME, profilenameedittext.getText().toString());
                                displayname.setText(datastore.getString(Constants.PROFILE_NAME));
                            } else showSnackbar("Your profile name should be 5 characters or longer", Snackbar.LENGTH_LONG);
                        } else showSnackbar("Set a profile name and image to continue", Snackbar.LENGTH_LONG);

                    }
                })
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .build();

        if(setinformationdialog.getCustomView() != null) {
            profilenameedittext = (EditText) setinformationdialog.getCustomView().findViewById(R.id.profileName);
            profilenameedittext.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    //tillåt endast val av bild om namnet är långt nog
                    if (editable.toString().length() >= 5) {
                        fromgallerybutton.setEnabled(true);
                        fromgallerybutton.setAlpha(1f);
                    } else {
                        fromgallerybutton.setEnabled(false);
                        fromgallerybutton.setAlpha(0.5f);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
            });
        }

        //användaren väljer bild
        final Options cropoptions = new Options();
        cropoptions.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        cropoptions.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        cropoptions.setCropFrameColor(ContextCompat.getColor(this, R.color.colorPrimaryTransparent));
        cropoptions.setLogoColor(ContextCompat.getColor(this,R.color.colorPrimary));
        cropoptions.setAspectRatio(1, 1);

        if(setinformationdialog.getCustomView() != null) {
            fromgallerybutton = (CircularImageView) setinformationdialog.getCustomView().findViewById(R.id.button_from_gallery);
            fromgallerybutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RxPaparazzo.takeImage(LobbyActivity.this)
                            .crop(cropoptions)
                            .usingGallery()
                            .subscribe(new Subscriber<Response<LobbyActivity, String>>() {
                                @Override
                                public void onNext(Response<LobbyActivity, String> response) {

                                    //ny tinyDB som kan nås av rxPaparazzo
                                    TinyDB datastore = new TinyDB(response.targetUI());

                                    if (response.resultCode() != RESULT_OK) {
                                        if (response.data().isEmpty()) {
                                            showSnackbar("A girl is no one", Snackbar.LENGTH_LONG);
                                        }
                                        return;
                                    }

                                    //sätt button till den valda bilden
                                    fromgallerybutton.setImageDrawable(Drawable.createFromPath(response.data()));

                                    if((profilenameedittext.getText().toString().length() >= 5) && (!response.data().isEmpty())) {
                                        //om användaren aldrig satt uppgifter förut
                                        response.targetUI().shouldcloseinfodialog = true;
                                        datastore.putString(Constants.PROFILE_NAME, profilenameedittext.getText().toString());
                                        datastore.putString(Constants.PROFILE_IMAGE, response.data());
                                        response.targetUI().displayname.setText(datastore.getString(Constants.PROFILE_NAME));
                                        Glide.with(response.targetUI()).load(datastore.getString(Constants.PROFILE_IMAGE)).into(profileimage);
                                    }

                                }
                                @Override
                                public void onCompleted() {
                                }
                                @Override
                                public void onError(Throwable e) {
                                }
                            });
                }
            });
        }

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setinformationdialog.setCancelable(true);
                setinformationdialog.show();
            }
        });

        createpartydialog = new MaterialDialog.Builder(this)
                .title(R.string.create_party_lowercase)
                .titleColorRes(R.color.materialWhite)
                .backgroundColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.materialWhite)
                .negativeColorRes(R.color.materialWhite)
                .customView(R.layout.dialog_createorjoin_lobby, true)
                .positiveText(R.string.create_party)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showpartydialogview = dialog.getCustomView();
                        if (showpartydialogview != null) {
                            partynameinput = (EditText) showpartydialogview.findViewById(R.id.partyName);
                            partypasswordinput = (EditText) showpartydialogview.findViewById(R.id.partyPassword);
                            lobbyPresenter.createParty(partynameinput.getText().toString(), partypasswordinput.getText().toString(),
                                    datastore.getString(Constants.USER_ID), (AuthenticationResponse) responsebundle.getParcelable("response"));
                        }
                    }
                })
                .build();

        if(createpartydialog.getCustomView() != null) {
            TextView createpartydialogcontent = (TextView) createpartydialog.getCustomView().findViewById(R.id.dialogContent);
            createpartydialogcontent.setText(R.string.create_party_dialog_content);
            EditText createpartypassword = (EditText) createpartydialog.getCustomView().findViewById(R.id.partyPassword);
            createpartypassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        joinpartydialog = new MaterialDialog.Builder(this)
                .title(R.string.join_party_lowercase)
                .titleColorRes(R.color.materialWhite)
                .backgroundColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.materialWhite)
                .negativeColorRes(R.color.materialWhite)
                .customView(R.layout.dialog_createorjoin_lobby, true)
                .positiveText(R.string.join_party)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showpartydialogview = dialog.getCustomView();
                        if (showpartydialogview != null) {
                            partynameinput = (EditText) showpartydialogview.findViewById(R.id.partyName);
                            partypasswordinput = (EditText) showpartydialogview.findViewById(R.id.partyPassword);
                            lobbyPresenter.joinParty(partynameinput.getText().toString(), partypasswordinput.getText().toString(),
                                    (AuthenticationResponse) responsebundle.getParcelable("response"));
                        }
                    }
                })
                .build();

        if(joinpartydialog.getCustomView() != null) {
            TextView joinpartydialogcontent = (TextView) joinpartydialog.getCustomView().findViewById(R.id.dialogContent);
            joinpartydialogcontent.setText(R.string.join_party_dialog_content);
            EditText joinpartypassword = (EditText) joinpartydialog.getCustomView().findViewById(R.id.partyPassword);
            joinpartypassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(datastore.getString(Constants.PROFILE_NAME).isEmpty()) {
            showSetUserInformationPrompt();
            return;
        } else {
            shouldcloseinfodialog = true;
        }

        displayname.setText(datastore.getString(Constants.PROFILE_NAME));
        Glide.with(this).load(datastore.getString(Constants.PROFILE_IMAGE)).into(profileimage);
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
    public void showSetUserInformationPrompt() {
        setinformationdialog.show();
    }

    @OnClick(R.id.create_party_layout)
    @Override
    public void showCreatePartyDialog() {
        createpartydialog.show();
    }

    @OnClick(R.id.join_party_layout)
    @Override
    public void showJoinPartyDialog() {
        joinpartydialog.show();
    }

    @Override
    public void goToParty(Party party, AuthenticationResponse response) {
        Intent i = new Intent(LobbyActivity.this, PartyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("response", response);
        bundle.putParcelable("party", party);
        i.putExtra("lobbybundle", bundle);
        startActivity(i);
    }

    @Override
    public void setBeerProgress(int progress) {
        beerprogressview.setBeerProgress(progress);
    }

    @Override
    public void showSnackbar(String snacktext, int length) {
        Snackbar.make(view, snacktext, length)
        .show();
    }

}
