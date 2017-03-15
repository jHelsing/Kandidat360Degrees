package com.ciux031701.kandidat360degrees.adaptors;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.FriendTuple;
import com.ciux031701.kandidat360degrees.ProfileFragment;
import com.ciux031701.kandidat360degrees.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
/**
 * Created by Anna on 2017-03-07. Modified by Amar 2017-03-09.
 */

public class FriendsAdapter extends RecyclerView.Adapter implements FastScrollRecyclerView.SectionedAdapter {
    private LayoutInflater mInflater;
    private ArrayList<FriendTuple> mDataSource;
    private ViewHolder holder;
    private Context context;

    public FriendsAdapter(Context context, ArrayList<FriendTuple> data) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mDataSource = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.friends_list_item, parent, false);
        view.setTag(holder);
        holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return mDataSource.get(position).getUserName().charAt(0) + "";
    }

    //Private class to implement ViewHolder pattern
    private static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView titleTextView;
        private ImageView thumbnailImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.friends_list_title);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.friends_list_thumbnail);
        }
    }
}
