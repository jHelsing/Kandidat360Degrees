package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.adaptors.ImageAdapter;

import java.io.IOException;

/**
 * Created by boking on 2017-02-17.
 * This fragment is started from the Upload-button in the drawer.
 */

public class UploadFragment extends Fragment {

    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private Uri imageUri;
    private ImageView imagePreview;
    private Button okButton;
    private Bitmap imageBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);

        imageUri = getArguments().getParcelable("image");
        imagePreview = (ImageView) root.findViewById(R.id.uploadImageView);
        imagePreview.setImageURI(imageUri);

        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        okButton = (Button) root.findViewById(R.id.uploadOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable("picture", imageBitmap);
                Fragment fragment = new ShareFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        });

        imagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("origin", "camera"); //change origin
                args.putParcelable("image", imageBitmap);
                ImageViewFragment fragment = new ImageViewFragment();
                fragment.setArguments(args);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("upload").commit();
            }
        });

        //The toolbar:
        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer_layout);
        toolbarMenuButton = (ImageButton)root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
/*
        //The gridview:
        GridView gridview = (GridView) root.findViewById(R.id.gridview);
        final ImageAdapter adapter = new ImageAdapter(getActivity());
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Go to ShareFragment:
                Fragment fragment = new ShareFragment();
                Bitmap tempPicture = BitmapFactory.decodeResource(getResources(), (Integer)adapter.getItem(position));
                Bundle args = new Bundle();
                args.putParcelable("picture", tempPicture);
                fragment.setArguments(args);
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });*/


        return root;
    }
}
