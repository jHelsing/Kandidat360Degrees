package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity {

    Button loginButton;
    Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //GUI
        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                myIntent.putExtra("username", "usernamefromlogin"); //Optional parameters
                LoginActivity.this.startActivity(myIntent);
            }
        });

        createAccountButton = (Button)findViewById(R.id.createAccountButton);
        //Button createAccountButton = (Button)findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = new CreateAccountFragment();
                fragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit();
            }
        });

    }

}
