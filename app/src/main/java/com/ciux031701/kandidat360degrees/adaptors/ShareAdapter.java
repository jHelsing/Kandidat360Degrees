package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.MainActivity;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.representation.FriendsAdapterItem;
import com.ciux031701.kandidat360degrees.representation.UserTuple;
import com.ciux031701.kandidat360degrees.R;

import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-16.
 */

public class ShareAdapter extends RecyclerView.Adapter {
    private ArrayList<FriendsAdapterItem> dataSource;
    private Context context;
    private RecyclerView.ViewHolder holder;
    private LayoutInflater inflater;

    public ShareAdapter(Context context){
        this.context = context;
        dataSource = Friends.getFriendsAdapterItems();
        inflater = LayoutInflater.from(context);


    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.share_friends_list_item, parent, false);
        holder = new ViewHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView username = (TextView)holder.itemView.findViewById(R.id.share_friends_list_title);
        TextView section = (TextView)holder.itemView.findViewById(R.id.share_friends_list_letter);
        ImageView thumbnail = (ImageView)holder.itemView.findViewById(R.id.share_friends_list_thumbnail);
        CheckBox shareCheck = (CheckBox)holder.itemView.findViewById(R.id.share_box);
        RelativeLayout itemLayout = (RelativeLayout)holder.itemView.findViewById(R.id.share_list_details);
        LinearLayout headerLayout = (LinearLayout)holder.itemView.findViewById(R.id.share_list_section_header);
        username.setVisibility(View.VISIBLE);
        FriendsAdapterItem item = dataSource.get(position);

        switch(item.getType()){
            case FriendsAdapterItem.HEADER:
                section.setText(item.getDataText());
                thumbnail.setVisibility(View.GONE);
                shareCheck.setVisibility(View.GONE);
                itemLayout.setVisibility(View.GONE);
                headerLayout.setVisibility(View.VISIBLE);
                break;
            case FriendsAdapterItem.FRIEND:
                username.setText(item.getDataText());
                thumbnail.setVisibility(View.VISIBLE);
                shareCheck.setVisibility(View.VISIBLE);
                headerLayout.setVisibility(View.GONE);
                itemLayout.setVisibility(View.VISIBLE);
                setItemClickListener(holder, shareCheck, position);
                break;
        }



    }

    private void setItemClickListener(RecyclerView.ViewHolder holder, final CheckBox shareCheck, final int position){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shareCheck.isChecked())
                    shareCheck.setChecked(false);
                else
                    shareCheck.setChecked(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public CheckBox selected;
        public ImageView thumbnail;
        public ViewHolder(View itemView){
            super(itemView);
            username = (TextView)itemView.findViewById(R.id.share_friends_list_title);
            thumbnail = (ImageView)itemView.findViewById(R.id.share_friends_list_thumbnail);
            selected = (CheckBox)itemView.findViewById(R.id.share_box);

        }
    }
}