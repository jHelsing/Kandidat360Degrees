package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.representation.FriendList;
import com.ciux031701.kandidat360degrees.representation.FriendTuple;
import com.ciux031701.kandidat360degrees.R;

import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-16.
 */

public class ShareAdapter extends FriendsListAdapter {

    public ShareAdapter(Context context, FriendList data) {
        super(context,data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getInflater().inflate(R.layout.share_friends_list_item, parent, false);
        view.setTag(getHolder());
        setHolder(new ViewHolder(view));
        return getHolder();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ArrayList<FriendTuple> mDataSource = getDataSource();
        FriendTuple data = mDataSource.get(position);

        TextView titleTextView = (TextView) holder.itemView.findViewById(R.id.share_friends_list_title);
        ImageView thumbnailImageView = (ImageView) holder.itemView.findViewById(R.id.share_friends_list_thumbnail);
        RelativeLayout shareListDetails = (RelativeLayout) holder.itemView.findViewById(R.id.share_list_details);
        LinearLayout shareListSectionHeader = (LinearLayout) holder.itemView.findViewById(R.id.share_list_section_header);
        final CheckBox mCheckBox = (CheckBox) holder.itemView.findViewById(R.id.share_box);

        if(data.getUserName().length() > 1) {
            shareListDetails.setVisibility(View.VISIBLE);
            shareListSectionHeader.setVisibility(View.GONE);
            titleTextView.setText(data.getUserName());
            thumbnailImageView.setImageDrawable(data.getProfilePicture());

           mCheckBox.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view){
                   if(mCheckBox.isChecked()){
                        //TODO. Not sure on how to save which friends that the image should be shared with
                   } else {
                        //TODO
                   }
               }
           });

        } else {
            TextView shareListSectionHeaderText = (TextView) holder.itemView.findViewById(R.id.share_friends_list_letter);
            shareListDetails.setVisibility(View.GONE);
            shareListSectionHeader.setVisibility(View.VISIBLE);
            shareListSectionHeaderText.setText(getSectionName(position));

        }
    }

}