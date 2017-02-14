package com.ciux031701.kandidat360degrees;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ciux031701.kandidat360degrees.adaptors.DrawerAdapter;

/**
 * Created by boking on 2017-02-14.
 */

public class MainActivity extends AppCompatActivity {
    private String[] mListOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListOptions = getResources().getStringArray(R.array.list_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerAdapter(this,getApplicationContext(), mListOptions));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
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
                System.out.println("clicked drawer 0");
            }else if(position==1) {
                System.out.println("clicked drawer 1");
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
            }else if (position==2){

            }
            ft.commit();
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

}
