package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Anna on 2017-02-23.
 */

public class LoginFragment extends Fragment {
    Button loginButton;
    Button createAccountButton;
    TextView title;
    LinearLayout mainLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        //GUI
        loginButton = (Button)root.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                myIntent.putExtra("username", "usernamefromlogin"); //Optional parameters
                startActivity(myIntent);
            }
        });

        title = (TextView)root.findViewById(R.id.appnameView);
        title.setBackgroundResource(R.color.colorPrimary);

        mainLayout = (LinearLayout) root.findViewById(R.id.mainLayout);
        
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });

        createAccountButton = (Button)root.findViewById(R.id.createAccountButton);
        //Button createAccountButton = (Button)findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = new CreateAccountFragment();
                fragmentManager.beginTransaction().add(R.id.fragment_container,fragment).addToBackStack(null).commit();
            }
        });

        return root;
    }
}
