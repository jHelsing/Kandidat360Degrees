package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.ciux031701.kandidat360degrees.R.id.textView;

/**
 * Created by Anna on 2017-02-22.
 */

public class CreateAccountFragment extends Fragment {
    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_createaccount, container, false);

        textView = (TextView) root.findViewById(R.id.textView2);
        textView.setText("Create account here!");

        return root;

    }
}
