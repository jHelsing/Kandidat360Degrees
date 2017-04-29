package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.adaptors.NotificationAdapter;

/**
 * Created by boking on 2017-02-17.
 */

public class NotificationFragment extends Fragment {
    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;
    private DrawerLayout mDrawerLayout;
    private TextView toolbarTitle;

    private ListView listView;
    private Button acceptButton;
    private Button cancelButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notification, container, false);

        //The toolbar:
        toolbar = (Toolbar) root.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = (TextView) root.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Notifications");
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toolbarMenuButton = (ImageButton) root.findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        //List of notifications:
        listView = (ListView) root.findViewById(R.id.notifications_list_view);

        final NotificationViewItem[] items = new NotificationViewItem[5];

        for (int i = 0; i < items.length; i++) {
            if (i % 2 != 0) {
                items[i] = new NotificationViewItem("Username1 added you!", NotificationAdapter.TYPE_FRIEND_REQUEST);
            } else {
                items[i] = new NotificationViewItem("Username3 uploaded a panorama!", NotificationAdapter.TYPE_IMAGE_UPLOAD);
            }
        }

        NotificationAdapter adapter = new NotificationAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        addListenerToNotificationItems(items);

        //Buttons:
        View inflatedView = getActivity().getLayoutInflater().inflate(R.layout.notification_friend_item, null);
        acceptButton = (Button)inflatedView.findViewById(R.id.buttonAcceptNotification);
        cancelButton = (Button)inflatedView.findViewById(R.id.buttonCancelNotification);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //TODO
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //TODO
            }
        });

        return root;
    }

    private void addListenerToNotificationItems(final NotificationViewItem[] items){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                if (items[i].getType() == NotificationAdapter.TYPE_IMAGE_UPLOAD){
                    //Go to the user's profile - map view and show the image
                    String selectedUser = "Username1";
                    Fragment fragment = new ProfileFragment();
                    Bundle setArgs = new Bundle();
                    setArgs.putString("username", selectedUser);
                    fragment.setArguments(setArgs);
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment, "PROFILE_FRAGMENT");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });
    }
}

