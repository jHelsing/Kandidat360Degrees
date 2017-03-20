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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new LoginFragment();
        //fragmentManager.beginTransaction().add(R.id.fragment_container,fragment).addToBackStack(null).commit();
        fragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit();

    }

    @Override
    public void onBackPressed (){
        moveTaskToBack(true);
    }

}
