package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import static com.ciux031701.kandidat360degrees.R.id.textView;

/**
 * Created by Anna on 2017-02-22.
 */

public class CreateAccountFragment extends Fragment {
    EditText usernameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_createaccount, container, false);


        //To change the color of a text field (here: the usernameField) depending on number of characters
        //OBS! Is not working yet - does not change the text color
        usernameText = (EditText) root.findViewById(R.id.usernameField);
        final TextWatcher txwatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                usernameText.removeTextChangedListener(this);
                if(s.length() < 5 && s.length() > 0){
                    usernameText.setTextColor(Color.RED);
                } else {
                    usernameText.setTextColor(Color.BLACK);
                }
                usernameText.addTextChangedListener(this);
            }
        };
        usernameText.addTextChangedListener(txwatcher);



        return root;

    }
}
