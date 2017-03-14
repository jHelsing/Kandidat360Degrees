package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
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

import static com.ciux031701.kandidat360degrees.R.id.textView;

/**
 * Created by Anna on 2017-02-22.
 */

public class CreateAccountFragment extends Fragment {
    Button createAccountButton;
    EditText usernameText;
    EditText passwordText;
    EditText repeatPasswordText;
    EditText emailText;
    TextView errorText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_createaccount, container, false);

        errorText = (TextView)root.findViewById(R.id.accErrorTextView);
        errorText.setVisibility(View.INVISIBLE);
        usernameText = (EditText) root.findViewById(R.id.createAccUsernameField); //username input
        emailText = (EditText) root.findViewById(R.id.createAccEmailField);
        passwordText = (EditText) root.findViewById(R.id.createAccPassword1Field); //first password input
        repeatPasswordText = (EditText) root.findViewById(R.id.createAccPassword2Field); //repeat password

        //To change the color of the text field depending on number of characters:
        listenerEditText(root,usernameText);
        listenerEditText(root,passwordText);

        //To change the color of the text field depending on if the two password fields have equal text
        listenerRepeatPasswordText(root);


        //create account-button: should return to activity_login ( + show that the account was created?)
        createAccountButton = (Button)root.findViewById(R.id.createAccCreateAccButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String username = usernameText.getText().toString();
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                String repeatPassword = repeatPasswordText.getText().toString();
                if(!password.equals(repeatPassword)){
                    errorText.setText("Passwords must match.");
                    errorText.setVisibility(View.VISIBLE);
                    return;
                }
                else
                    errorText.setVisibility(View.INVISIBLE);

                JReqRegister registerReq = new JReqRegister(username, password, email);
                registerReq.setJResultListener(
                        new JResultListener(){
                            @Override
                            public void onHasResult(JSONObject result) {
                                try {
                                    boolean error = result.getBoolean("error");
                                    String message = result.getString("message");
                                    if(!error){
                                        Toast.makeText(getActivity(), "Account created",Toast.LENGTH_SHORT).show();
                                        getFragmentManager().popBackStack();
                                    }
                                    else if(message.equals("ERR_USER")) {
                                        errorText.setText("Invalid username.");
                                        errorText.setVisibility(View.VISIBLE);
                                    }
                                    else if(message.equals("ERR_PASSWORD")) {
                                        errorText.setText("Invalid password.");
                                        errorText.setVisibility(View.VISIBLE);
                                    }
                                    else if(message.equals("ERR_EMAIL")) {
                                        errorText.setText("Invalid e-mail.");
                                        errorText.setVisibility(View.VISIBLE);
                                    }
                                    else if(message.equals("ERR_USER_EXISTS")) {
                                        errorText.setText("Username taken.");
                                        errorText.setVisibility(View.VISIBLE);
                                    }
                                    else if(message.equals("ERR_EMAIL_EXISTS")) {
                                        errorText.setText("E-mail already registered.");
                                        errorText.setVisibility(View.VISIBLE);
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


    //Adds TextWatcher to field. Changes the color to red if length < 5 and > 0, otherwise it is black
    private void listenerEditText(View root,final EditText field){
        final TextWatcher txwatcher1 = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                field.removeTextChangedListener(this);
                //Not correct if input is shorter than 5 characters
                if(s.length() < 5 && s.length() > 0){
                    field.setTextColor(Color.RED);
                } else {
                    field.setTextColor(Color.BLACK);
                }
                field.addTextChangedListener(this);
            }
        };
        field.addTextChangedListener(txwatcher1);
    }
}
