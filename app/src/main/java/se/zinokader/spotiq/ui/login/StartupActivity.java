package se.zinokader.spotiq.ui.login;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.ActivityStartupBinding;
import se.zinokader.spotiq.ui.base.BaseActivity;

@RequiresPresenter(StartupPresenter.class)
public class StartupActivity extends BaseActivity<StartupPresenter> {

    private static final int LOGIN_REQUEST = 2157;
    private ActivityStartupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_startup);
        binding.setPresenter(getPresenter());
    }

    public void goToAuthentication() {
        startActivityForResult(new Intent(this, AuthenticationActivity.class), LOGIN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != LOGIN_REQUEST) return;

        if (resultCode == RESULT_OK) {
            binding.logoTextView.setText("y!");
        }
        else {
            binding.logoTextView.setText("n :(");
        }
    }
}
