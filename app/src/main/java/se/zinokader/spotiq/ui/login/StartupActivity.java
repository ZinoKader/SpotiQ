package se.zinokader.spotiq.ui.login;

import android.content.Intent;
import android.os.Bundle;

import nucleus5.view.NucleusAppCompatActivity;
import se.zinokader.spotiq.databinding.ActivityStartupBinding;
import se.zinokader.spotiq.util.Injector;

public class StartupActivity extends NucleusAppCompatActivity<StartupPresenter> {

    ActivityStartupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Injector) getApplication()).inject(this);
        startActivity(new Intent(this, AuthenticationActivity.class));
    }

}
