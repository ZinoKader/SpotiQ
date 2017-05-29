package se.zinokader.spotiq.ui.lobby;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.ActivityLobbyBinding;
import se.zinokader.spotiq.ui.base.BaseActivity;

@RequiresPresenter(LobbyPresenter.class)
public class LobbyActivity extends BaseActivity<LobbyPresenter> {

    private enum DialogType {
        JOIN_DIALOG, CREATE_DIALOG
    }

    private enum DialogAction {
        OPEN, CLOSE
    }

    private static final int DIALOG_ANIMATION_DURATION = 500;
    private ActivityLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lobby);
        binding.setPresenter(getPresenter());
        binding.joinPartyButton.setOnClickListener( c -> showDialog(DialogType.JOIN_DIALOG));
        binding.createPartyButton.setOnClickListener( c -> showDialog(DialogType.CREATE_DIALOG));
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
}
