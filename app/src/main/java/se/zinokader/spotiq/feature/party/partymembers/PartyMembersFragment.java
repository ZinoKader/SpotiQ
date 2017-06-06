package se.zinokader.spotiq.feature.party.partymembers;

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
import java.util.List;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.databinding.FragmentPartyMembersBinding;
import se.zinokader.spotiq.model.User;

@FragModule
public class PartyMembersFragment extends Fragment {

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
        binding.membersRecyclerView.setAdapter(partyMemberRecyclerAdapter);
        return binding.getRoot();
    }

    public void addMember(User partyMember) {
        for(int i = 0; i < 10; i++) { //Test, TODO: Remove this later
            partyMembers.add(partyMember);
        }
        partyMemberRecyclerAdapter.notifyDataSetChanged();
    }

}
