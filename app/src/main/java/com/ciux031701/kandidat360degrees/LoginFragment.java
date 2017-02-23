package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Anna on 2017-02-23.
 */

public class LoginFragment extends Fragment {
    Button loginButton;
    Button createAccountButton;

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
