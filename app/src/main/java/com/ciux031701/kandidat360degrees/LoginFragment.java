package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ciux031701.kandidat360degrees.communication.FriendRequests;
import com.ciux031701.kandidat360degrees.communication.Friends;
import com.ciux031701.kandidat360degrees.communication.JReqCheckSession;
import com.ciux031701.kandidat360degrees.communication.JReqLogin;
import com.ciux031701.kandidat360degrees.communication.JRequest.JResultListener;
import com.ciux031701.kandidat360degrees.communication.JRequester;
import com.ciux031701.kandidat360degrees.communication.MD5;
import com.ciux031701.kandidat360degrees.communication.Session;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Anna on 2017-02-23. Modified by Amar on 2017-03-16.
 */

public class LoginFragment extends Fragment implements View.OnKeyListener {
    Button loginButton;
    Button createAccountButton;
    TextView title;
    EditText usernameField;
    EditText passwordField;
    TextView errorView;
    ProgressBar loginProgressbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //If there is a saved session, skip login.
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        usernameField = (EditText)root.findViewById(R.id.usernameFIeld);
        passwordField = (EditText)root.findViewById(R.id.passwordField);
        errorView = (TextView)root.findViewById(R.id.errorView);
        errorView.setVisibility(View.INVISIBLE);

        loginProgressbar = (ProgressBar) root.findViewById(R.id.loginProgressbar);
        passwordField.setOnKeyListener(this);

        //GUI
        loginButton = (Button)root.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeLogin();
            }
        });

        title = (TextView)root.findViewById(R.id.appnameView);
        title.setBackgroundResource(R.color.colorPrimary);

        LinearLayout mainLayout = (LinearLayout) root.findViewById(R.id.loginMainLayout);
        
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

    private void executeLogin(){
        Session.setUser(usernameField.getText().toString());
        String password = passwordField.getText().toString();
        JReqLogin loginReq = new JReqLogin(Session.getUser(), password);
        loginReq.setJResultListener(
                new JResultListener(){
                    @Override
                    public void onHasResult(JSONObject result) {
                        boolean error;
                        String sessionId = null;
                        String message = null;
                        try{
                            error = result.getBoolean("error");
                            message = result.getString("message");
                            sessionId = result.getString("id");
                        }
                        catch(JSONException je){
                            error = true;
                        }
                        if(!error){
                            Session.setId(sessionId);
                            Session.save();
                            Friends.init();
                            FriendRequests.init();
                            Intent myIntent = new Intent(getActivity(), MainActivity.class);
                            myIntent.putExtra("username", Session.getUser()); //Optional parameters
                            startActivity(myIntent);
                            getActivity().finish();

                        }
                        else if(message.equals("ERR_USER") | message.equals("ERR_PASS")){
                            errorView.setText("Please, make sure that your username or password is correct.");
                            errorView.setVisibility(View.VISIBLE);
                            usernameField.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                            passwordField.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                            loginProgressbar.setVisibility(View.INVISIBLE);
                        }
                    }
                }
        );
        loginReq.sendRequest();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(loginProgressbar.getVisibility() == View.VISIBLE)
            loginProgressbar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            executeLogin();
        }
        return false;
    }
}
