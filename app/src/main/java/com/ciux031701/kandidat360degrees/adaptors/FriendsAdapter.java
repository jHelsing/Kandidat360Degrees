package com.ciux031701.kandidat360degrees.adaptors;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.representation.FriendTuple;
import com.ciux031701.kandidat360degrees.ProfileFragment;
import com.ciux031701.kandidat360degrees.R;

import java.util.ArrayList;
/**
 * Created by Anna on 2017-03-07. Modified by Amar 2017-03-09.
 */

public class FriendsAdapter extends FriendsListAdapter {

    public FriendsAdapter(Context context, ArrayList<FriendTuple> data) {
        super(context,data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getInflater().inflate(R.layout.friends_list_item, parent, false);
        view.setTag(getHolder());
        setHolder(new ViewHolder(view));
        return getHolder();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ArrayList<FriendTuple> mDataSource = getDataSource();
        final Context context = getContext();
        TextView titleTextView = (TextView) holder.itemView.findViewById(R.id.friends_list_title);
        ImageView thumbnailImageView = (ImageView) holder.itemView.findViewById(R.id.friends_list_thumbnail);
        RelativeLayout friendlistDetails = (RelativeLayout) holder.itemView.findViewById(R.id.friendlist_details);
        LinearLayout friendlistSectionHeader = (LinearLayout) holder.itemView.findViewById(R.id.friendlist_section_header);
        FriendTuple data = mDataSource.get(position);
        if(data.getUserName().length() > 1) {
            friendlistDetails.setVisibility(View.VISIBLE);
            friendlistSectionHeader.setVisibility(View.GONE);
            titleTextView.setText(data.getUserName());
            thumbnailImageView.setImageDrawable(data.getProfilePicture());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String selectedUser = ((TextView) holder.itemView.findViewById(R.id.friends_list_title)).getText().toString();
                    //TODO: Go to the selectedUser's profile instead of MrCool's
                    Fragment fragment = new ProfileFragment();
                    Bundle setArgs = new Bundle();
                    setArgs.putString("username", selectedUser);
                    fragment.setArguments(setArgs);
                    FragmentManager fragmentManager = ((FragmentActivity) context).getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        } else {
            TextView friendlistSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.friends_list_letter);
            friendlistDetails.setVisibility(View.GONE);
            friendlistSectionHeader.setVisibility(View.VISIBLE);
            friendlistSectionHeaderText.setText(getSectionName(position));

        }
    }

}
