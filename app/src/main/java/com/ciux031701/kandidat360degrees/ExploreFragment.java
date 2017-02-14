package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by boking on 2017-02-14.
 */

public class ExploreFragment extends Fragment {
    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);

        TextView textView = (TextView) root.findViewById(R.id.textView1);
        textView.setText("explore");
        return root;
    }
}
