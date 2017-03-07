package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.R;

import java.util.ArrayList;
/**
 * Created by Anna on 2017-03-07.
 */

public class FriendsAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<String> mDataSource;

    public FriendsAdapter(Context context,ArrayList<String> data) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDataSource = data;
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

        @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //ViewHolder pattern: (for faster scrolling - do not need to inflate if the view already exists)
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.friends_list_item, parent, false);
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.friends_list_thumbnail);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.friends_list_title);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        TextView titleTextView = holder.titleTextView;
        ImageView thumbnailImageView = holder.thumbnailImageView;

        String data = (String) getItem(position);
        titleTextView.setText(data);
        thumbnailImageView.setImageResource(R.drawable.anonymous_profile_image_circle_small);

        return convertView;
    }

    //Private class to implement ViewHolder pattern
    private static class ViewHolder {
        public TextView titleTextView;
        public ImageView thumbnailImageView;
    }
}
