package se.zinokader.spotiq.ui.lobby;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.ActivityLobbyBinding;
import se.zinokader.spotiq.ui.base.BaseActivity;

@RequiresPresenter(LobbyPresenter.class)
public class LobbyActivity extends BaseActivity<LobbyPresenter> {

    private ActivityLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lobby);
        binding.setPresenter(getPresenter());
    }
}
