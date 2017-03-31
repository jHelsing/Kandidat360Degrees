package com.ciux031701.kandidat360degrees.adaptors;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentProvider;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.FriendsFragment;
import com.ciux031701.kandidat360degrees.MainActivity;
import com.ciux031701.kandidat360degrees.communication.JReqAcceptFriend;
import com.ciux031701.kandidat360degrees.communication.JReqDeclineFriend;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.representation.FriendList;
import com.ciux031701.kandidat360degrees.representation.FriendRequestList;
import com.ciux031701.kandidat360degrees.representation.FriendTuple;
import com.ciux031701.kandidat360degrees.ProfileFragment;
import com.ciux031701.kandidat360degrees.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-07. Modified by Amar 2017-03-09.
 */

public class FriendsAdapter extends FriendsListAdapter {
    private FriendRequestList friendRequests;

    public FriendsAdapter(Context context, FriendList friends, FriendRequestList friendRequests) {
        super(context, friends);
        this.friendRequests = friendRequests;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getInflater().inflate(R.layout.friends_list_item, parent, false);
        view.setTag(getHolder());
        setHolder(new ViewHolder(view));
        return getHolder();
    }

    @Override
    public int getItemCount() {
        return getDataSource().size() + friendRequests.size();
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Context context = getContext();
        TextView titleTextView = (TextView) holder.itemView.findViewById(R.id.friends_list_title);
        ImageView thumbnailImageView = (ImageView) holder.itemView.findViewById(R.id.friends_list_thumbnail);
        RelativeLayout friendlistDetails = (RelativeLayout) holder.itemView.findViewById(R.id.friendlist_details);
        LinearLayout friendlistSectionHeader = (LinearLayout) holder.itemView.findViewById(R.id.friendlist_section_header);
        Button acceptButton = (Button) holder.itemView.findViewById(R.id.buttonAcceptFriendRequest);
        Button cancelButton = (Button) holder.itemView.findViewById(R.id.buttonCancelFriendRequest);

        if (isFirstPosition(position) && friendRequests.size() > 1) { //show "friend requests"-header
            showFriendRequestHeader(position, friendlistDetails, friendlistSectionHeader, holder);
        } else if (isFriendRequest(position)) { //show friend request
            showFriendRequestItem(position, friendlistDetails, friendlistSectionHeader, titleTextView, thumbnailImageView, acceptButton, cancelButton);
        } else if (isFriend(position, getDataSource())) { //show friend
            showFriendItem(position,acceptButton,cancelButton,friendlistDetails,friendlistSectionHeader,titleTextView,thumbnailImageView,holder);
        } else { //show section header
            showFriendHeader(position,acceptButton,cancelButton,holder,friendlistDetails,friendlistSectionHeader);
        }
    }

    private boolean isFriendRequest(int position) {
        return position < friendRequests.size();
    }

    private boolean isFirstPosition(int position) {
        return position == 0;
    }

    private boolean isFriend(int position, ArrayList<FriendTuple> mDataSource) {
        return position > friendRequests.size() && (mDataSource.get(position-friendRequests.size()).getUserName().length() > 1);
    }

    private void showFriendRequestHeader(int position, RelativeLayout friendlistDetails, LinearLayout friendlistSectionHeader,
                                         RecyclerView.ViewHolder holder) {
        TextView friendlistSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.friends_list_letter);
        friendlistDetails.setVisibility(View.GONE);
        friendlistSectionHeader.setVisibility(View.VISIBLE);
        friendlistSectionHeaderText.setText(friendRequests.get(position).getUserName());
    }

    private void showFriendRequestItem(int position, RelativeLayout friendlistDetails, LinearLayout friendlistSectionHeader,
                                       TextView titleTextView, ImageView thumbnailImageView, Button acceptButton, Button cancelButton) {
        FriendTuple data = friendRequests.get(position);
        friendlistDetails.setVisibility(View.VISIBLE);
        friendlistSectionHeader.setVisibility(View.GONE);
        titleTextView.setText(data.getUserName());
        thumbnailImageView.setImageDrawable(data.getProfilePicture());
        acceptButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        addListenerToAcceptButton(acceptButton, data);
        addListenerToCancelButton(cancelButton, data);
    }

    private void addListenerToAcceptButton(Button acceptButton, final FriendTuple user){
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JReqAcceptFriend jReqAcceptFriend = new JReqAcceptFriend(user.getUserName());
                jReqAcceptFriend.setJResultListener(
                        new JRequest.JResultListener() {
                            @Override
                            public void onHasResult(JSONObject result) {
                                try {
                                    boolean error = result.getBoolean("error");
                                    if(!error){
                                        friendRequests.remove(user);
                                        friends.add(user);
                                        mDataSource = friends.getList();
                                        notifyDataSetChanged();
                                    }
                                }
                                catch(JSONException je){

                                }
                            }
                        }
                );
                jReqAcceptFriend.sendRequest();

            }
        });
    }

    private void addListenerToCancelButton(Button cancelButton, final FriendTuple user){
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JReqDeclineFriend jReqDeclineFriend = new JReqDeclineFriend(user.getUserName());
                jReqDeclineFriend.setJResultListener(
                        new JRequest.JResultListener() {
                            @Override
                            public void onHasResult(JSONObject result) {
                                try {
                                    boolean error = result.getBoolean("error");
                                    if(!error){
                                        friendRequests.remove(user);
                                        notifyDataSetChanged();
                                    }
                                }
                                catch(JSONException je){

                                }
                            }
                        }
                );
                jReqDeclineFriend.sendRequest();
            }
        });
    }

    private void showFriendItem(int position, Button acceptButton, Button cancelButton, RelativeLayout friendlistDetails, LinearLayout friendlistSectionHeader,
                                TextView titleTextView, ImageView thumbnailImageView, final RecyclerView.ViewHolder holder) {
        //List the user's friends
        ArrayList<FriendTuple> mDataSource = getDataSource();
        FriendTuple data = mDataSource.get(position - friendRequests.size());
        acceptButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        friendlistDetails.setVisibility(View.VISIBLE);
        friendlistSectionHeader.setVisibility(View.GONE);
        titleTextView.setText(data.getUserName());
        thumbnailImageView.setImageDrawable(data.getProfilePicture());

        addListenerToView(holder, getContext());
    }

    private void addListenerToView(final RecyclerView.ViewHolder holder, final Context context){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedUser = ((TextView) holder.itemView.findViewById(R.id.friends_list_title))
                        .getText().toString();
                ((MainActivity) context).showProfile(selectedUser);
            }
        });
    }

    private void showFriendHeader(int position, Button acceptButton, Button cancelButton, RecyclerView.ViewHolder holder, RelativeLayout friendlistDetails,
                                  LinearLayout friendlistSectionHeader) {
        acceptButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        TextView friendlistSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.friends_list_letter);
        friendlistDetails.setVisibility(View.GONE);
        friendlistSectionHeader.setVisibility(View.VISIBLE);
        friendlistSectionHeaderText.setText(getSectionName(position - friendRequests.size()));
    }

}
