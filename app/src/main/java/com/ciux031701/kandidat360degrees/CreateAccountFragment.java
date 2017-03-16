package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
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
    TextView EmailInformationText;
    TextView passwordInformationText;
    TextView usernameInformationText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_createaccount, container, false);

        EmailInformationText = (TextView)root.findViewById(R.id.createEmailErrorText);
        passwordInformationText = (TextView)root.findViewById(R.id.createAccPasswordInfoView);
        usernameInformationText = (TextView)root.findViewById(R.id.createAccUsernameInfoView);

        usernameText = (EditText) root.findViewById(R.id.createAccUsernameField); //username input
        emailText = (EditText) root.findViewById(R.id.createAccEmailField);
        passwordText = (EditText) root.findViewById(R.id.createAccPassword1Field); //first password input
        repeatPasswordText = (EditText) root.findViewById(R.id.createAccPassword2Field); //repeat password

        //To change the color of the text field depending on if the two password fields have equal text
        listenerRepeatPasswordText(root);


        //create account-button: should return to activity_login ( + show that the account was created?)
        createAccountButton = (Button)root.findViewById(R.id.createAccCreateAccButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                final String username = usernameText.getText().toString();
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                String repeatPassword = repeatPasswordText.getText().toString();
                if(!password.equals(repeatPassword)){
                    passwordInformationText.setText("Passwords must match.");
                    passwordInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
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
                                        usernameInformationText.setText("The username cannot be shorter than 5 characters.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_TOO_LONG")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("The username cannot be longer than 30 characters.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_EXISTS")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("The username is already taken.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_INVALID_CHARACTER")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("The username can only contain letters a-z and numbers 0-9.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_INVALID_START")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("The username needs to start with at least 3 letters.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else if(message.contains("ERR_USER_INVALID_NUM_POS")) {
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setText("The username can only have numbers at the end.");
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    } else {
                                        usernameInformationText.setVisibility(View.INVISIBLE);
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if(message.contains("ERR_PASSWORD_TOO_LONG")) {
                                        EmailInformationText.setText("Invalid password.");
                                        EmailInformationText.setVisibility(View.VISIBLE);
                                    } else if(message.contains("ERR_PASSWORD_TOO_SHORT")) {
                                        EmailInformationText.setText("Invalid password.");
                                        EmailInformationText.setVisibility(View.VISIBLE);
                                    } else if(message.contains("ERR_PASSWORD_INVALID_CHARACTER")) {
                                        EmailInformationText.setText("Invalid password.");
                                        EmailInformationText.setVisibility(View.VISIBLE);
                                    } else
                                        EmailInformationText.setVisibility(View.INVISIBLE);
                                    if(message.contains("ERR_EMAIL")) {
                                        EmailInformationText.setText("Invalid e-mail.");
                                        EmailInformationText.setVisibility(View.VISIBLE);
                                    } else if(message.contains("ERR_EMAIL_EXISTS")) {
                                        EmailInformationText.setText("E-mail is already registered.");
                                        EmailInformationText.setVisibility(View.VISIBLE);
                                    } else{
                                        EmailInformationText.setVisibility(View.INVISIBLE);
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

    //Adds TextWatcher to repeatPasswordText. Turns the color red if the string does not equal the password
    //given in passwordText.
    private void listenerRepeatPasswordText(View root){
        final TextWatcher txwatcher2 = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                repeatPasswordText.removeTextChangedListener(this);
                //Not correct if not equals the first input
                if(!s.toString().equals(passwordText.getText().toString())){
                    repeatPasswordText.setTextColor(Color.RED);
                } else {
                    repeatPasswordText.setTextColor(Color.BLACK);
                }
                repeatPasswordText.addTextChangedListener(this);
            }
        };
        repeatPasswordText.addTextChangedListener(txwatcher2);
    }

}
