package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.opengl.Visibility;
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
import com.ciux031701.kandidat360degrees.R;
import com.ciux031701.kandidat360degrees.communication.JReqAcceptFriend;
import com.ciux031701.kandidat360degrees.communication.JReqCancelFriendrequest;
import com.ciux031701.kandidat360degrees.communication.JReqDeclineFriend;
import com.ciux031701.kandidat360degrees.communication.JReqRemoveFriend;
import com.ciux031701.kandidat360degrees.communication.JReqSendFriendrequest;
import com.ciux031701.kandidat360degrees.representation.FriendList;
import com.ciux031701.kandidat360degrees.representation.FriendRequestList;
import com.ciux031701.kandidat360degrees.representation.FriendTuple;
import com.ciux031701.kandidat360degrees.representation.UserRelationship;

import java.util.ArrayList;

/**
 * Created by Neso on 2017-03-31.
 */

public class FriendSearchAdapter extends RecyclerView.Adapter {
    private ArrayList<FriendTuple> result;
    private Context context;
    private RecyclerView.ViewHolder holder;
    private FriendList fList;
    private FriendRequestList fRequestList;
    private UserRelationship relationship;
    public FriendSearchAdapter(Context context, FriendRequestList fRequestList, FriendList fList, ArrayList<FriendTuple> result, UserRelationship relationship){
        this.context = context;
        this.result = result;
        this.fList = fList;
        this.fRequestList = fRequestList;
        this.relationship = relationship;
        this.result.add(0, new FriendTuple("Results", context));
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friends_list_item, parent, false);
        holder = new FriendsListAdapter.ViewHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView usernameTextView = (TextView) holder.itemView.findViewById(R.id.friends_list_title);
        usernameTextView.setText(result.get(position).getUserName());
        ImageView thumbnailImageView = (ImageView) holder.itemView.findViewById(R.id.friends_list_thumbnail);
        RelativeLayout friendlistDetails = (RelativeLayout) holder.itemView.findViewById(R.id.friendlist_details);
        LinearLayout friendlistSectionHeader = (LinearLayout) holder.itemView.findViewById(R.id.friendlist_section_header);
        Button rightButton = (Button) holder.itemView.findViewById(R.id.buttonAcceptFriendRequest);
        Button leftButton = (Button) holder.itemView.findViewById(R.id.buttonCancelFriendRequest);

        if(position == 0){
            friendlistDetails.setVisibility(View.GONE);
            friendlistSectionHeader.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.GONE);
            TextView friendlistSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.friends_list_letter);
            friendlistSectionHeaderText.setVisibility(View.VISIBLE);
            friendlistSectionHeaderText.setText(result.get(0).getUserName());
        }
        else{
            friendlistDetails.setVisibility(View.VISIBLE);
            friendlistSectionHeader.setVisibility(View.GONE);
            usernameTextView.setText(result.get(position).getUserName());
            thumbnailImageView.setImageDrawable(result.get(position).getProfilePicture());
            FriendsAdapter.addListenerToView(holder, context);
            setupButtons(result.get(position), rightButton, leftButton);
        }
    }

    private void setupButtons(final FriendTuple user, final Button rightButton, final Button leftButton){
        leftButton.setVisibility(View.INVISIBLE);
        rightButton.setVisibility(View.INVISIBLE);
        if(relationship.getValue() == UserRelationship.SELF){
            rightButton.setVisibility(View.INVISIBLE);
        }
        else if(relationship.getValue() == UserRelationship.NONE){
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setText("Add");
            rightButton.setOnClickListener(
                    new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            JReqSendFriendrequest jReqSendFriendrequest = new JReqSendFriendrequest(user.getUserName());
                            jReqSendFriendrequest.sendRequest();
                            refreshButtons(new UserRelationship(UserRelationship.PENDING_REQUESTER), user, rightButton, leftButton);
                        }
                    }
            );
        }
        else if(relationship.getValue() == UserRelationship.FRIEND){
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setText("Remove");
            rightButton.setOnClickListener(
                    new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            JReqRemoveFriend jReqRemoveFriend = new JReqRemoveFriend(user.getUserName());
                            jReqRemoveFriend.sendRequest();
                            refreshButtons(new UserRelationship(UserRelationship.NONE), user, rightButton, leftButton);
                            fList.remove(user);
                        }
                    }
            );
        }
        else if(relationship.getValue() == UserRelationship.PENDING_REQUESTER){
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setText("Withdraw");
            rightButton.setOnClickListener(
                    new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            JReqCancelFriendrequest jReqCancelFriendrequest = new JReqCancelFriendrequest(user.getUserName());
                            jReqCancelFriendrequest.sendRequest();
                            refreshButtons(new UserRelationship(UserRelationship.NONE), user, rightButton, leftButton);
                        }
                    }
            );
        }
        else if(relationship.getValue() == UserRelationship.PENDING_REQUESTEE){
            leftButton.setVisibility((View.VISIBLE));
            rightButton.setVisibility(View.VISIBLE);
            leftButton.setText("Decline");
            leftButton.setOnClickListener(
                    new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            JReqDeclineFriend jReqDeclineFriend = new JReqDeclineFriend(user.getUserName());
                            jReqDeclineFriend.sendRequest();
                            fRequestList.remove(user);
                            refreshButtons(new UserRelationship(UserRelationship.NONE), user, rightButton, leftButton);
                        }
                    }
            );
            rightButton.setText("Accept");
            rightButton.setOnClickListener(
                    new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            JReqAcceptFriend jReqAcceptFriend = new JReqAcceptFriend(user.getUserName());
                            jReqAcceptFriend.sendRequest();
                            fRequestList.remove(user);
                            fList.add(user);
                            refreshButtons(new UserRelationship(UserRelationship.FRIEND), user, rightButton, leftButton);
                        }
                    }
            );
        }
    }

    private void refreshButtons(UserRelationship newRelationship, FriendTuple user, Button rightButton, Button leftButton){
        relationship = newRelationship;
        setupButtons(user, rightButton, leftButton);
    }

    @Override
    public int getItemCount() {
        return result.size();
    }

}
