package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by boking on 2017-02-21.
 */

public class ProfileFragment extends Fragment {
    TextView textView;
    Bundle args;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        //Get for which username this profile is for
        args = getArguments();
        username = args.getString("username");

        //Get pictures, total likes nbr of friends or whatever we decide to display from db

        return root;
    }
}
