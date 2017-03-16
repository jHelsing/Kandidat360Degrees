package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.R;

/**
 * Created by boking on 2017-02-21.
 */

public class ProfileFlowAdapter extends ArrayAdapter<FlowPicture> {
    public ProfileFlowAdapter(Context context, FlowPicture[] pictures) {
        super(context, R.layout.picture_profile_layout,pictures);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        FlowPicture singlePic = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.picture_profile_layout,parent,false);

        ImageView imageView = (ImageView) customView.findViewById(R.id.panoramaPreview);
        TextView locationText = (TextView) customView.findViewById(R.id.locationText);
        TextView dateText = (TextView) customView.findViewById(R.id.dateText);

        locationText.setText(singlePic.getLocation());
        dateText.setText(singlePic.getDate());
        //Set image
        return customView;
    }
}
