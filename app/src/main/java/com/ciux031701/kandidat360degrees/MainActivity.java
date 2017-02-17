package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.ciux031701.kandidat360degrees.adaptors.DrawerAdapter;

/**
 * Created by boking on 2017-02-14.
 */

public class MainActivity extends AppCompatActivity {
    private String[] mListOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private Toolbar toolbar;
    private ImageButton toolbarMenuButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListOptions = getResources().getStringArray(R.array.list_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarMenuButton = (ImageButton)findViewById(R.id.toolbarMenuButton);
        toolbarMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerAdapter(this,getApplicationContext(), mListOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tool_menu, menu);
        return true;
    }

    //Handles drawer item clicks
    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        //for args:
        // Bundle args = new Bundle();
        //args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        //fragment.setArguments(args);
        private void selectItem(int position) {

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if(position==0){
                System.out.println("clicked explore");
                ExploreFragment f = (ExploreFragment) fm.findFragmentByTag("fragment_explore");
                if (f == null) {  // not added
                    f = new ExploreFragment();
                    ft.add(R.id.content_frame, f, "fragment_explore");
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                } else {  // already added
                    ft.remove(f);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                }

                // Highlight the selected item, update the title, and close the drawer
                mDrawerList.setItemChecked(position, true);
                setTitle(mListOptions[position]);
            }else if(position==1) {
                System.out.println("clicked notifications");

            }else if (position==2){
                System.out.println("clicked camera");
            }else if (position==3){
                System.out.println("clicked friends");
            }else if (position==4){
                System.out.println("clicked upload");
            }else if (position==5){
                System.out.println("clicked settings");
            }else if (position==6){
                System.out.println("clicked logout");
            }
            ft.commit();
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

}
