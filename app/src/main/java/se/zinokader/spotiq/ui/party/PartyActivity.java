package se.zinokader.spotiq.ui.party;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import nucleus5.factory.RequiresPresenter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.ActivityPartyBinding;
import se.zinokader.spotiq.ui.base.BaseActivity;

@RequiresPresenter(PartyPresenter.class)
public class PartyActivity extends BaseActivity<PartyPresenter> {

    private ActivityPartyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_party);
        binding.setPresenter(getPresenter());
    }



}
