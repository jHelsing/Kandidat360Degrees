package com.ciux031701.kandidat360degrees.adaptors;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.MainActivity;
import com.ciux031701.kandidat360degrees.R;

/**
 * Created by boking on 2017-02-14.
 */

public class DrawerAdapter extends ArrayAdapter<String> {
    MainActivity mainActivity;

    public DrawerAdapter(MainActivity mainActivity, Context context, String[] options) {
        super(context, R.layout.drawer_list_item, options);
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.drawer_list_item, parent, false);

        String option = getItem(position);

        ImageView itemImageView = (ImageView) customView.findViewById(R.id.itemImageView);
        TextView itemTextView = (TextView) customView.findViewById(R.id.itemTextView);
        itemTextView.setGravity(Gravity.CENTER_VERTICAL);

                //The image will be different for each row
        if(option.equals("Camera")){
            itemImageView.setImageResource(R.drawable.camera_icon);
        }else if(option.equals("Explore")){
            itemImageView.setImageResource(R.drawable.temp_mapsicon);
        }else if (option.equals("Notifications")){
            itemImageView.setImageResource(R.drawable.temp_notifications);
        }else if(option.equals("Friends")){
            itemImageView.setImageResource(R.drawable.temp_friends);
        }else if(option.equals("Upload")){
            itemImageView.setImageResource(R.drawable.temp_upload);
        }else if(option.equals("Settings")){
            itemImageView.setImageResource(R.drawable.temp_settings);
        }
        itemTextView.setText(option);
        //same with image when we have those resources
        return customView;
    }
}
