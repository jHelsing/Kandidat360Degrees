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
                    passwordInformationText.setText(getResources().getText(R.string.password_match));
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

                                    boolean usernameError = false;
                                    if(message.contains("ERR_USER_TOO_SHORT")) {
                                        usernameInformationText.setText(getResources().getText(R.string.username_short));
                                        usernameError = true;
                                    } else if(message.contains("ERR_USER_TOO_LONG")) {
                                        usernameInformationText.setText(getResources().getText(R.string.username_long));
                                        usernameError = true;
                                    } else if(message.contains("ERR_USER_EXISTS")) {
                                        usernameInformationText.setText(getResources().getText(R.string.username_taken));
                                        usernameError = true;
                                    } else if(message.contains("ERR_USER_INVALID_CHARACTER")) {
                                        usernameInformationText.setText(getResources().getText(R.string.username_characters));;
                                        usernameError = true;
                                    } else if(message.contains("ERR_USER_INVALID_START")) {
                                        usernameInformationText.setText(getResources().getText(R.string.username_start));
                                        usernameError = true;
                                    } else if(message.contains("ERR_USER_INVALID_NUM_POS")) {
                                        usernameInformationText.setText(getResources().getText(R.string.username_end));
                                        usernameError = true;
                                    } else {
                                        usernameInformationText.setVisibility(View.INVISIBLE);
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if(usernameError){
                                        usernameInformationText.setVisibility(View.VISIBLE);
                                        usernameInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        usernameText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }

                                    boolean passwordError = false;
                                    if(message.contains("ERR_PASSWORD_TOO_LONG")) {
                                        passwordInformationText.setText(getResources().getText(R.string.password_long));
                                        passwordError = true;
                                    } else if(message.contains("ERR_PASSWORD_TOO_SHORT")) {
                                        passwordInformationText.setText(getResources().getText(R.string.password_short));
                                        passwordError = true;
                                    } else if(message.contains("ERR_PASSWORD_INVALID_CHARACTER")) {
                                        passwordInformationText.setText(getResources().getText(R.string.password_characters));
                                        passwordError = true;
                                    } else {
                                        passwordInformationText.setVisibility(View.INVISIBLE);
                                        passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                        repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if (passwordError){
                                        passwordInformationText.setVisibility(View.VISIBLE);
                                        passwordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        repeatPasswordText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        passwordInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                    }

                                    boolean emailError = false;
                                    if(message.contains("ERR_EMAIL_EXISTS")) {
                                        emailInformationText.setText(getResources().getText(R.string.email_taken));
                                        emailError = true;
                                    } else if(message.contains("ERR_EMAIL")) {
                                        emailInformationText.setText(getResources().getText(R.string.email_invalid));
                                        emailError = true;
                                    } else{
                                        emailInformationText.setVisibility(View.INVISIBLE);
                                        emailText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_dark) , PorterDuff.Mode.SRC_ATOP);
                                    }
                                    if(emailError){
                                        emailText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_dark) , PorterDuff.Mode.SRC_ATOP);
                                        emailInformationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        emailInformationText.setVisibility(View.VISIBLE);
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
