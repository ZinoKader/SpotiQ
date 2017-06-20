package se.zinokader.spotiq.feature.lobby;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.bumptech.glide.Glide;
import com.rengwuxian.materialedittext.MaterialEditText;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.ActivityLobbyBinding;
import se.zinokader.spotiq.feature.base.BaseActivity;
import se.zinokader.spotiq.feature.party.PartyActivity;
import se.zinokader.spotiq.util.validator.PartyPasswordValidator;
import se.zinokader.spotiq.util.validator.PartyTitleValidator;

@RequiresPresenter(LobbyPresenter.class)
public class LobbyActivity extends BaseActivity<LobbyPresenter> implements LobbyView {

    ActivityLobbyBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lobby);
        binding.joinPartyButton.setOnClickListener(c -> showDialog(DialogType.JOIN_DIALOG));
        binding.createPartyButton.setOnClickListener(c -> showDialog(DialogType.CREATE_DIALOG));
    }

    @Override
    public void onResume() {
        super.onResume();
        super.startForegroundTokenRenewalService();
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopForegroundTokenRenewalService();
    }

    public void setUserDetails(String userName, String userImageUrl) {
        binding.userName.setText(userName);
        Glide.with(this)
            .load(userImageUrl)
            .placeholder(R.drawable.image_profile_placeholder)
            .dontAnimate()
            .dontTransform()
            .into(binding.userImage);
    }

    public void goToParty(String partyTitle) {
        ActivityOptions transitionOptions = ActivityOptions.makeSceneTransitionAnimation(this,
            new Pair<>(binding.userImage, getResources().getString(R.string.profile_image_transition)),
            new Pair<>(binding.userName, getResources().getString(R.string.user_name_transition)));
        Intent intent = new Intent(this, PartyActivity.class);
        intent.putExtra(ApplicationConstants.PARTY_NAME_EXTRA, partyTitle);
        startActivity(intent, transitionOptions.toBundle());
    }

    private static final int DIALOG_ANIMATION_DURATION = 500;

    private enum DialogType {
        JOIN_DIALOG, CREATE_DIALOG
    }

    private enum DialogAction {
        OPEN, CLOSE
    }

    private void showDialog(DialogType dialogType) {
        View dialogView;
        switch (dialogType) {
            case JOIN_DIALOG:
                dialogView = View.inflate(this, R.layout.dialog_join_party, null);
                break;
            case CREATE_DIALOG:
                dialogView = View.inflate(this, R.layout.dialog_create_party, null);
                break;
            default:
                return;
        }
        Dialog mockDialog = new Dialog(this, R.style.FullScreenDialogStyle);
        mockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mockDialog.setContentView(dialogView);

        MaterialEditText partyTitle = mockDialog.findViewById(R.id.partyTitle);
        MaterialEditText partyPassword = mockDialog.findViewById(R.id.partyPassword);
        partyTitle.addValidator(new PartyTitleValidator());
        partyPassword.addValidator(new PartyPasswordValidator());

        mockDialog.findViewById(R.id.submitButton).setOnClickListener(c -> {
            switch (dialogType) {
                case JOIN_DIALOG:
                    if(!(partyTitle.validate() & partyPassword.validate())) return;
                    animateDialog(DialogAction.CLOSE, dialogType, dialogView, mockDialog);
                    getPresenter().joinParty(partyTitle.getText().toString(), partyPassword.getText().toString());
                    break;
                case CREATE_DIALOG:
                    if(!(partyTitle.validate() & partyPassword.validate())) return;
                    animateDialog(DialogAction.CLOSE, dialogType, dialogView, mockDialog);
                    getPresenter().createParty(partyTitle.getText().toString(), partyPassword.getText().toString());
                    break;
            }
        });

        mockDialog.findViewById(R.id.closeDialogButton).setOnClickListener(c ->
            animateDialog(DialogAction.CLOSE, dialogType, dialogView, mockDialog));
        mockDialog.setOnShowListener(dialogInterface ->
            animateDialog(DialogAction.OPEN, dialogType, dialogView, null));
        mockDialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                animateDialog(DialogAction.CLOSE, dialogType, dialogView, mockDialog);
                return true;
            }
            return false;
        });

        mockDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mockDialog.show();
    }

    private void animateDialog(DialogAction dialogAction, DialogType dialogType, View dialogView, Dialog dialog) {
        View dialogRoot = dialogView.findViewById(R.id.root);
        int endRadius = (int) Math.hypot(dialogRoot.getWidth(), dialogRoot.getHeight());
        int x = dialogRoot.getWidth() / 2;
        int y;
        switch (dialogType) {
            case JOIN_DIALOG:
                y = (int) binding.joinPartyButton.getY() + (binding.joinPartyButton.getHeight() / 2);
                break;
            case CREATE_DIALOG:
                y = (int) binding.createPartyButton.getY() + (binding.createPartyButton.getHeight() / 2);
                break;
            default:
                return;
        }
        switch (dialogAction) {
            case OPEN:
                Animator circularDialogReveal = ViewAnimationUtils.createCircularReveal(dialogRoot,
                    x, y, binding.createPartyButton.getWidth() / 2, endRadius);
                circularDialogReveal.setInterpolator(new AccelerateDecelerateInterpolator());
                dialogRoot.setVisibility(View.VISIBLE);
                circularDialogReveal.setDuration(DIALOG_ANIMATION_DURATION);
                circularDialogReveal.start();
                break;
            case CLOSE:
                Animator circularDialogClose = ViewAnimationUtils.createCircularReveal(dialogRoot,
                    x, y, endRadius, 0);
                circularDialogClose.setInterpolator(new AccelerateDecelerateInterpolator());
                circularDialogClose.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dialog.dismiss();
                        dialogRoot.setVisibility(View.INVISIBLE);
                    }
                });
                circularDialogClose.setDuration(DIALOG_ANIMATION_DURATION);
                circularDialogClose.start();
                break;
        }
    }

    @Override
    public View getRootView() {
        return binding.getRoot();
    }
}
