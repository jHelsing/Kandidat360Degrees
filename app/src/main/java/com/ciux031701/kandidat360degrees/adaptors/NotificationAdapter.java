package com.ciux031701.kandidat360degrees.adaptors;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.NotificationViewItem;
import com.ciux031701.kandidat360degrees.R;
import com.ciux031701.kandidat360degrees.communication.DownloadService;
import com.ciux031701.kandidat360degrees.communication.FTPInfo;
import com.ciux031701.kandidat360degrees.communication.ImageType;

import java.io.File;

/**
 * Created by Anna on 2017-03-09.
 */

public class NotificationAdapter extends BaseAdapter {

    public static final int TYPE_FRIEND_REQUEST = 0;
    public static final int TYPE_IMAGE_UPLOAD = 1;
    //public static final int TYPE_WHITE = 2;
    //public static final int TYPE_BLACK = 3;

    private NotificationViewItem[] objects;
    private Context mContext;
    private LayoutInflater mInflater;

    ImageView thumbnail;
    String username;
    public NotificationAdapter(Context context, NotificationViewItem[] objects) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        this.objects = objects;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return objects[position].getType();
    }

    //Not needed here
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return objects[position];
    }

    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        NotificationViewItem listViewItem = objects[position];
        int listViewItemType = getItemViewType(position);

        if (convertView == null) {
            if (listViewItemType == TYPE_FRIEND_REQUEST) {
                convertView = mInflater.inflate(R.layout.notification_friend_item, null);
                thumbnail = (ImageView)convertView.findViewById(R.id.notification_friend_image);
                thumbnail.setImageResource(R.drawable.friend_added_icon);
            } else { // if (listViewItemType == TYPE_IMAGE_UPLOAD) {
                convertView = mInflater.inflate(R.layout.notification_image_item, null);
                thumbnail = (ImageView)convertView.findViewById(R.id.notification_image_uploaded_image);
                thumbnail.setImageResource(R.drawable.upload_icon);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            viewHolder = new ViewHolder(textView);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.getText().setText(listViewItem.getText());

        return convertView;
    }


    private class ViewHolder {
        private TextView text;

        public ViewHolder(TextView text) {
            this.text = text;
        }

        public TextView getText() {
            return text;
        }

        public void setText(TextView text) {
            this.text = text;
        }

    }
}

