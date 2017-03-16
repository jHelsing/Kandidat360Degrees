package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import com.ciux031701.kandidat360degrees.Communication.*;
import com.ciux031701.kandidat360degrees.Communication.JRequest.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anna on 2017-02-22. Modified by Amar on 2017-03-16.
 */

public class CreateAccountFragment extends Fragment {
    Button createAccountButton;
    EditText usernameText;
    EditText passwordText;
    EditText repeatPasswordText;
    EditText emailText;
    TextView emailInformationText;
    TextView passwordInformationText;
    TextView usernameInformationText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_createaccount, container, false);

        emailInformationText = (TextView)root.findViewById(R.id.createEmailErrorText);
        passwordInformationText = (TextView)root.findViewById(R.id.createAccPasswordInfoView);
        usernameInformationText = (TextView)root.findViewById(R.id.createAccUsernameInfoView);

        usernameText = (EditText) root.findViewById(R.id.createAccUsernameField); //username input
        emailText = (EditText) root.findViewById(R.id.createAccEmailField);
        passwordText = (EditText) root.findViewById(R.id.createAccPassword1Field); //first password input
        repeatPasswordText = (EditText) root.findViewById(R.id.createAccPassword2Field); //repeat password

        //create account-button: should return to activity_login ( + show that the account was created?)
        createAccountButton = (Button)root.findViewById(R.id.createAccCreateAccButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                final String username = usernameText.getText().toString();
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                final String repeatPassword = repeatPasswordText.getText().toString();
                if(!password.equals(repeatPassword)){
                    passwordInformationText.setVisibility(View.VISIBLE);
                    passwordInformationText.setText("Passwords must match.");
                    passwordInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                    repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                    return;
                } else {

                }
                    passwordInformationText.setVisibility(View.INVISIBLE);

                JReqRegister registerReq = new JReqRegister(username, password, email);
                registerReq.setJResultListener(
                        new JResultListener(){
                            @Override
                            public void onHasResult(JSONObject result) {
                                try {
                                    boolean error = result.getBoolean("error");
                                    String message = result.getString("message");
                                    Log.d("Databas", message);
                                    if(!error){
                                        Toast.makeText(getActivity(), "Account created",Toast.LENGTH_SHORT).show();
                                        getFragmentManager().popBackStack();
                                    }
                                    if(message.contains("ERR_USER_TOO_SHORT")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("Username cannot be shorter than 5 characters.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_TOO_LONG")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("Username cannot be longer than 30 characters.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_EXISTS")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("Username is already taken.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_INVALID_CHARACTER")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("Username can only contain letters a-z/A-Z and numbers 0-9.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_INVALID_START")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("Username needs to start with at least 3 letters.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_INVALID_NUM_POS")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("Username can only have numbers at the end.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else {
                                        usernameInformationText.setVisibility(View.INVISIBLE);
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }

                                    if(message.contains("ERR_PASSWORD_TOO_LONG")) {
                                        passwordInformationText.setText("Password cannot be longer than 30 characters.");
                                        passwordInformationText.setVisibility(View.VISIBLE);
                                        passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        passwordInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                    } else if(message.contains("ERR_PASSWORD_TOO_SHORT")) {
                                        passwordInformationText.setText("Password cannot be shorter than 5 characters.");
                                        passwordInformationText.setVisibility(View.VISIBLE);
                                        passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        passwordInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                    } else if(message.contains("ERR_PASSWORD_INVALID_CHARACTER")) {
                                        passwordInformationText.setText("Password can only contain letters a-z/A-Z and numbers 0-9.");
                                        passwordInformationText.setVisibility(View.VISIBLE);
                                        passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        passwordInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                    } else {
                                        passwordInformationText.setVisibility(View.INVISIBLE);
                                        passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                        repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }

                                    if(message.contains("ERR_EMAIL_EXISTS")) {
                                        emailInformationText.setText("E-mail address already exists.");
                                        emailText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        emailInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        emailInformationText.setVisibility(View.VISIBLE);
                                    } else if(message.contains("ERR_EMAIL")) {
                                        emailInformationText.setText("E-mail address is invalid.");
                                        emailText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        emailInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        emailInformationText.setVisibility(View.VISIBLE);
                                    } else{
                                        emailInformationText.setVisibility(View.INVISIBLE);
                                        emailText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }
                                }
                                catch(JSONException je){
                                    je.printStackTrace();
                                }

                            }
                        }
                );
                JRequester.setRequest(registerReq);
                JRequester.sendRequest();


            }
        });
        
        return root;

    }
}
