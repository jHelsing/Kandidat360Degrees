package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.adaptors.ShareAdapter;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.JReqShareImage;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.representation.UserTuple;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anna on 2017-03-06.
 * This fragment is started from an Upload-fragment or Camera-fragment.
 */

public class ShareFragment extends Fragment {
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private ImageView previewPic;
    private Button shareButton;
    private MenuItem earthButton;
    private final Friends friendList = new Friends();
    private RecyclerView mRecyclerView;
    private boolean firstView = false;
    private boolean makePublic = false;
    private Menu toolbarMenu;

    private Bundle args;
    private Bitmap pictureInBitmap;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_share, container, false);
        setHasOptionsMenu(true);
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

        shareButton = (Button)root.findViewById(R.id.shareButton);

        addListenerToShareButton(shareButton);

        //The picture to be shared:
        args = getArguments();
        if(args!=null){
            pictureInBitmap = args.getParcelable("picture");
            //previewPic.setImageBitmap(pictureInBitmap);
        }

        //The friends list:
        mRecyclerView = (RecyclerView) root.findViewById(R.id.share_friends_recycle_view);


        ShareAdapter adapter = new ShareAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolmenu_share, menu);
        this.toolbarMenu = menu;
        earthButton = menu.findItem(R.id.sharePublic);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sharePublic:
                if (makePublic) {
                    toolbarMenu.getItem(0).setIcon(R.drawable.temp_earthblack);
                    makePublic = false;

                } else {
                    toolbarMenu.getItem(0).setIcon(R.drawable.temp_earthwhite);
                    makePublic = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addListenerToShareButton(Button shareButton) {
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareAdapter adapter = (ShareAdapter)mRecyclerView.getAdapter();
                String selectedNames = adapter.getSelectedString();
                if(!selectedNames.isEmpty()) {
                    JReqShareImage jReqShareImage = new JReqShareImage("111", selectedNames);
                    jReqShareImage.setJResultListener(
                            new JRequest.JResultListener() {
                                @Override
                                public void onHasResult(JSONObject result) {
                                    try {
                                        boolean error = result.getBoolean("error");
                                        if(!error){
                                            //arguments so that the explore view can show some kind of loading Toast "Sharing..."
                                            Toast.makeText(getActivity(), "Sharing...",
                                                    Toast.LENGTH_SHORT).show();
                                            args = new Bundle();
                                            args.putString("shared", "somekindofID");
                                            Fragment fragment = new ExploreFragment();
                                            FragmentManager fragmentManager = getActivity().getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.content_frame, fragment);
                                            fragmentTransaction.addToBackStack(null);
                                            fragmentTransaction.commit();
                                        }
                                    }
                                    catch(JSONException je){

                                    }
                                }
                            }
                    );
                    jReqShareImage.sendRequest();
                }
            }
        });
    }

}
