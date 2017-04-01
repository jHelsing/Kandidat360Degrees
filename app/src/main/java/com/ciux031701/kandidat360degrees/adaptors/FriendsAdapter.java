package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.MainActivity;
import com.ciux031701.kandidat360degrees.communication.JReqAcceptFriend;
import com.ciux031701.kandidat360degrees.communication.JReqDeclineFriend;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.FriendRequests;
import com.ciux031701.kandidat360degrees.representation.FriendsAdapterItem;
import com.ciux031701.kandidat360degrees.representation.UserTuple;
import com.ciux031701.kandidat360degrees.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-07. Modified by Amar 2017-03-09.
 */

public class FriendsAdapter extends RecyclerView.Adapter {

    private LayoutInflater mInflater;
    protected ArrayList<FriendsAdapterItem> mDataSource;
    private ViewHolder holder;
    private Context context;

    public FriendsAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mDataSource = new ArrayList<>();
        mDataSource.addAll(FriendRequests.getFriendsAdapterItems());
        mDataSource.addAll(Friends.getFriendsAdapterItems());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.friends_list_item, parent, false);
        holder = new ViewHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public int getItemCount() {
        return Friends.size() + FriendRequests.size();
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        TextView titleTextView = (TextView) holder.itemView.findViewById(R.id.friends_list_title);
        ImageView thumbnailImageView = (ImageView) holder.itemView.findViewById(R.id.friends_list_thumbnail);
        RelativeLayout friendlistDetails = (RelativeLayout) holder.itemView.findViewById(R.id.friendlist_details);
        LinearLayout friendlistSectionHeader = (LinearLayout) holder.itemView.findViewById(R.id.friendlist_section_header);
        Button acceptButton = (Button) holder.itemView.findViewById(R.id.buttonAcceptFriendRequest);
        Button cancelButton = (Button) holder.itemView.findViewById(R.id.buttonCancelFriendRequest);

        switch (mDataSource.get(position).getType()) {
            case FriendsAdapterItem.HEADER:
                showHeader(position, acceptButton, cancelButton, holder, friendlistDetails, friendlistSectionHeader);
                break;
            case FriendsAdapterItem.REQUEST:
                showFriendRequestItem(position, friendlistDetails, friendlistSectionHeader, titleTextView, thumbnailImageView, acceptButton, cancelButton);
                break;
            case FriendsAdapterItem.FRIEND:
                showFriendItem(position, acceptButton, cancelButton, friendlistDetails, friendlistSectionHeader, titleTextView, thumbnailImageView, holder);
                break;
        }
    }


    private void showFriendRequestHeader(int position, RelativeLayout friendlistDetails, LinearLayout friendlistSectionHeader,
                                         RecyclerView.ViewHolder holder) {
        TextView friendlistSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.friends_list_letter);
        friendlistDetails.setVisibility(View.GONE);
        friendlistSectionHeader.setVisibility(View.VISIBLE);
        friendlistSectionHeaderText.setText(FriendRequests.getFriendsAdapterItems().get(position).getDataText());
    }

    private void showFriendRequestItem(int position, RelativeLayout friendlistDetails, LinearLayout friendlistSectionHeader,
                                       TextView titleTextView, ImageView thumbnailImageView, Button acceptButton, Button cancelButton) {
        UserTuple data = FriendRequests.getFriendsAdapterItems().get(position).getData();
        friendlistDetails.setVisibility(View.VISIBLE);
        friendlistSectionHeader.setVisibility(View.GONE);
        titleTextView.setText(data.getUserName());
        thumbnailImageView.setImageDrawable(data.getProfilePicture());
        acceptButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        addListenerToAcceptButton(acceptButton, data);
        addListenerToCancelButton(cancelButton, data);
    }

    private void addListenerToAcceptButton(Button acceptButton, final UserTuple user){
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
                                        FriendRequests.remove(user);
                                        Friends.add(user);
                                        mDataSource = Friends.getFriendsAdapterItems();
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

    private void addListenerToCancelButton(Button cancelButton, final UserTuple user){
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
                                        FriendRequests.remove(user);
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
        UserTuple data = mDataSource.get(position).getData();
        acceptButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        friendlistDetails.setVisibility(View.VISIBLE);
        friendlistSectionHeader.setVisibility(View.GONE);
        titleTextView.setText(data.getUserName());
        thumbnailImageView.setImageDrawable(data.getProfilePicture());

        addListenerToView(holder, context);
    }

    public static void addListenerToView(final RecyclerView.ViewHolder holder, final Context context){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedUser = ((TextView) holder.itemView.findViewById(R.id.friends_list_title))
                        .getText().toString();
                ((MainActivity) context).showProfile(selectedUser);
            }
        });
    }

    private void showHeader(int position, Button acceptButton, Button cancelButton, RecyclerView.ViewHolder holder, RelativeLayout friendlistDetails,
                            LinearLayout friendlistSectionHeader) {
        acceptButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        TextView friendlistSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.friends_list_letter);
        friendlistDetails.setVisibility(View.GONE);
        friendlistSectionHeader.setVisibility(View.VISIBLE);
        friendlistSectionHeaderText.setText(mDataSource.get(position).getDataText());
    }

    //Private class to implement ViewHolder pattern
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView titleTextView;
        private ImageView thumbnailImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.friends_list_title);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.friends_list_thumbnail);
        }
    }

}
