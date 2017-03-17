package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.representation.FriendTuple;
import com.ciux031701.kandidat360degrees.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

/**
 * Created by Anna on 2017-03-16.
 */

public abstract class FriendsListAdapter extends RecyclerView.Adapter implements FastScrollRecyclerView.SectionedAdapter {
    private LayoutInflater mInflater;
    private ArrayList<FriendTuple> mDataSource;
    private ViewHolder holder;
    private Context context;

    public FriendsListAdapter(Context context, ArrayList<FriendTuple> data) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mDataSource = data;
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(final RecyclerView.ViewHolder holder, int position);

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

    public ArrayList<FriendTuple> getDataSource(){
        return mDataSource;
    }

    public Context getContext(){
        return context;
    }

    public LayoutInflater getInflater(){
        return mInflater;
    }

    public ViewHolder getHolder() {
        return holder;
    }

    public void setHolder(ViewHolder holder){
        this.holder = holder;
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
