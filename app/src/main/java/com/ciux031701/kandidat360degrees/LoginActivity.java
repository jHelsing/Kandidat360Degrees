package com.ciux031701.kandidat360degrees;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ciux031701.kandidat360degrees.communication.FriendRequests;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.Session;
import com.ciux031701.kandidat360degrees.communication.SessionCheckService;

public class LoginActivity extends Activity{

    private BroadcastReceiver sessionCheckReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionCheckReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = intent.getIntExtra("RESULT", -1);
                if(result == Activity.RESULT_OK){
                    Friends.init();
                    FriendRequests.init();
                    Intent myIntent = new Intent(context, MainActivity.class);
                    myIntent.putExtra("username", Session.getUser()); //Optional parameters
                    startActivity(myIntent);
                    unregisterReceiver(sessionCheckReceiver);
                    ((LoginActivity)context).finish();
                }
                else if(result == Activity.RESULT_CANCELED ){
                    FragmentManager fragmentManager = getFragmentManager();
                    Fragment fragment = new LoginFragment();
                    //fragmentManager.beginTransaction().add(R.id.fragment_container,fragment).addToBackStack(null).commit();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container,fragment, "LOGIN_FRAGMENT").commit();
                    unregisterReceiver(sessionCheckReceiver);
                }
            }
        };

        registerReceiver(sessionCheckReceiver, new IntentFilter("com.ciux031701.kandidat360degrees.communication.SessionCheckService"));
        startService(new Intent(this, SessionCheckService.class));

    }

    @Override
    public void onBackPressed (){
        moveTaskToBack(true);
    }

}
