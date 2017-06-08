package se.zinokader.spotiq.feature.party.partymember;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dilpreet2028.fragmenter_annotations.Fragmenter;
import com.dilpreet2028.fragmenter_annotations.annotations.FragModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.FragmentPartyMembersBinding;
import se.zinokader.spotiq.model.User;
import se.zinokader.spotiq.util.comparator.PartyMemberComparator;

@FragModule
public class PartyMemberFragment extends Fragment {

    FragmentPartyMembersBinding binding;
    private PartyMemberRecyclerAdapter partyMemberRecyclerAdapter;
    private List<User> partyMembers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_party_members, container, false);
        Fragmenter.inject(this);

        partyMemberRecyclerAdapter = new PartyMemberRecyclerAdapter(partyMembers);
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.membersRecyclerView.addItemDecoration(new DividerItemDecoration(getContext()
                .getDrawable(R.drawable.list_divider),false, false));
        binding.membersRecyclerView.setAdapter(partyMemberRecyclerAdapter);
        return binding.getRoot();
    }

    public void addMember(User partyMember) {
        partyMembers.add(partyMember);
        Collections.sort(partyMembers, PartyMemberComparator.getByJoinedTimeComparator());
        partyMemberRecyclerAdapter.notifyDataSetChanged();
    }

}
