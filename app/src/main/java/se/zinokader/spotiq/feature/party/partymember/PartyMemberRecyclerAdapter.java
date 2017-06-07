package se.zinokader.spotiq.feature.party.partymember;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.model.User;

public class PartyMemberRecyclerAdapter extends RecyclerView.Adapter<PartyMemberRecyclerAdapter.UserHolder> {

    private List<User> partyMembers;

    PartyMemberRecyclerAdapter(List<User> partyMembers) {
        this.partyMembers = partyMembers;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflatedView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_row_party_members, viewGroup, false);
        return new UserHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(UserHolder userHolder, int i) {
        User partyMember = partyMembers.get(i);
        Glide.with(userHolder.itemView)
                .load(partyMember.getUserImageUrl())
                .into(userHolder.userImage);
        userHolder.userName.setText(partyMember.getUserName());
    }

    @Override
    public int getItemCount() {
        return partyMembers.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView userImage;
        private TextView userName;

        UserHolder(View view) {
            super(view);

            userImage = view.findViewById(R.id.userImage);
            userName = view.findViewById(R.id.userName);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

}
