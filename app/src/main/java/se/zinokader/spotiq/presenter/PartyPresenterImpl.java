package se.zinokader.spotiq.presenter;

import android.util.Log;

import se.zinokader.spotiq.model.Party;
import se.zinokader.spotiq.view.PartyView;

public class PartyPresenterImpl implements PartyPresenter {

    private PartyView view;
    private Party party;

    @Override
    public void setView(PartyView view) {
        this.view = view;
    }

    @Override
    public void detach() {

    }

    @Override
    public void setParty(Party party) {
        this.party = party;
    }
}
