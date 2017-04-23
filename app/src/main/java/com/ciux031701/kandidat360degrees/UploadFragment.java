package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.adaptors.ImageAdapter;
import com.ciux031701.kandidat360degrees.communication.ImageType;

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
    ProgressBar previewProgressBar;
    private TextView changeImageText;
    private static final int PICK_IMAGE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);

        imageUri = getArguments().getParcelable("image");
        imagePreview = (ImageView) root.findViewById(R.id.uploadImageView);
        imagePreview.setImageURI(imageUri);
        changeImageText = (TextView)root.findViewById(R.id.changeImageText);

        changeImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery,PICK_IMAGE);
            }
        });

        previewProgressBar = (ProgressBar)root.findViewById(R.id.previewProgressBar);

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
                previewProgressBar.setVisibility(View.VISIBLE);
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showPanoramaBitmap("upload",imageBitmap);
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

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent content){
        super.onActivityResult(requestCode,resultCode,content);

        if(resultCode== Activity.RESULT_OK && requestCode== PICK_IMAGE){
            Uri imageUri = content.getData();
            Class fragmentClass = UploadFragment.class;
            try {
                Fragment fragment = (Fragment) fragmentClass.newInstance();
                Bundle bundle = new Bundle();
                bundle.putParcelable("image", imageUri);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
