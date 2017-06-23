package se.zinokader.spotiq.feature.party.partymember;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.databinding.FragmentPartyMembersBinding;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.util.comparator.PartyMemberComparator;
import se.zinokader.spotiq.util.di.Injector;
import se.zinokader.spotiq.util.view.DividerItemDecoration;

public class PartyMemberFragment extends Fragment {

    FragmentPartyMembersBinding binding;

    @Inject
    PartiesRepository partiesRepository;

    private CompositeDisposable disposableActions = new CompositeDisposable();

    private PartyMemberRecyclerAdapter partyMemberRecyclerAdapter;

    private String partyTitle;
    private List<User> partyMembers = new ArrayList<>();

    public static PartyMemberFragment newInstance(String partyTitle) {
        PartyMemberFragment partyMemberFragment = new PartyMemberFragment();
        Bundle newInstanceArguments = new Bundle();
        newInstanceArguments.putString("partyTitle", partyTitle);
        partyMemberFragment.setArguments(newInstanceArguments);
        return partyMemberFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((Injector) getContext().getApplicationContext()).inject(this);
        super.onCreate(savedInstanceState);
        this.partyTitle = getArguments().getString("partyTitle");

        disposableActions.add(partiesRepository.listenToPartyMemberChanges(partyTitle)
            .delay(ApplicationConstants.DEFAULT_DELAY_MS, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(childEvent -> {
                User partyMember = childEvent.getDataSnapshot().getValue(User.class);
                switch (childEvent.getChangeType()) {
                    case ADDED:
                        addMember(partyMember);
                        break;
                    case CHANGED:
                        changePartyMember(partyMember);
                        break;
                }
            }));
    }

    @Override
    public void onDestroy() {
        disposableActions.clear();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("partyTitle", getArguments().getString("partyTitle"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_party_members, container, false);

        partyMemberRecyclerAdapter = new PartyMemberRecyclerAdapter(partyMembers);
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.membersRecyclerView.addItemDecoration(new DividerItemDecoration(getContext()
            .getDrawable(R.drawable.search_list_divider),false, false));

        AlphaInAnimationAdapter animatedAdapter =
            new AlphaInAnimationAdapter(partyMemberRecyclerAdapter);
        animatedAdapter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatedAdapter.setHasStableIds(true);
        animatedAdapter.setStartPosition(ApplicationConstants.DEFAULT_LIST_ANIMATION_ITEM_POSITION_START);
        animatedAdapter.setDuration(ApplicationConstants.DEFAULT_LIST_ANIMATION_DURATION_MS);

        binding.membersRecyclerView.setAdapter(animatedAdapter);
        return binding.getRoot();
    }

    public void scrollToTop() {
        binding.membersRecyclerView.smoothScrollToPosition(0);
    }

    private void addMember(User partyMember) {
        partyMembers.add(partyMember);
        Collections.sort(partyMembers, PartyMemberComparator.getByJoinedTimeComparator());
        partyMemberRecyclerAdapter.notifyDataSetChanged();
    }

    private void changePartyMember(User changedPartyMember) {
        List<User> toRemove = new ArrayList<>();
        for (User existingPartyMember : partyMembers) {
            if (existingPartyMember.getUserId().equals(changedPartyMember.getUserId())) {
                toRemove.add(existingPartyMember);
            }
        }
        partyMembers.removeAll(toRemove);
        addMember(changedPartyMember);
    }

}
