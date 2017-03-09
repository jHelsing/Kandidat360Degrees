package com.ciux031701.kandidat360degrees.adaptors;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ciux031701.kandidat360degrees.R;

/**
 * Created by Anna on 2017-03-09.
 * Used in UploadFragment's GridView.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mImageIds.length;
    }

    public Object getItem(int position) {
        return mImageIds[position];
    }

    //Not needed here
    public long getItemId(int position) {
        return 0;
    }

    //Create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            //Set the size/scale so all pictures are the same size:
            imageView.setLayoutParams(new GridView.LayoutParams(240,240));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }
        //Set the the position-th image:
        imageView.setImageResource(mImageIds[position]);
        return imageView;
    }

    //References to the images
    private Integer[] mImageIds = {
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama,
            R.drawable.example_panorama, R.drawable.example_panorama
    };
}
