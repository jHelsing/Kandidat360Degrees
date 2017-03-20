package com.ciux031701.kandidat360degrees;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ciux031701.kandidat360degrees.communication.JReqLogin;
import com.ciux031701.kandidat360degrees.communication.JRequest;
import com.ciux031701.kandidat360degrees.communication.JRequester;
import com.ciux031701.kandidat360degrees.communication.Session;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by AMAR on 2017-03-20.
 */

public class CreateAccountFeedbackFragment extends Fragment {
    ProgressBar createAccountProgressbar;
    Button createAccountCongratulationsBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_createaccount_feedback, container, false);
        TextView usernameText = (TextView)root.findViewById(R.id.createAccountCongratulationsTextview3);
        Bundle args = getArguments();
        final String username = args.getString("username");
        final String password = args.getString("password");
        usernameText.setText(getText(R.string.create_account_thanks_for_registering) + ", " + username + "!");

        createAccountProgressbar = (ProgressBar) root.findViewById(R.id.createAccountProgressbar);

        //GUI
        createAccountCongratulationsBtn = (Button)root.findViewById(R.id.createAccountCongratulationsBtn);
        createAccountCongratulationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccountProgressbar.setVisibility(View.VISIBLE);
                JReqLogin loginReq = new JReqLogin(username, password);
                loginReq.setJResultListener(
                        new JRequest.JResultListener(){
                            @Override
                            public void onHasResult(JSONObject result) {
                                boolean error;
                                String sessionId = null;
                                try{
                                    error = result.getBoolean("error");
                                    sessionId = result.getString("id");
                                }
                                catch(JSONException je){
                                    error = true;
                                }
                                if(!error){
                                    Session.setId(sessionId);
                                    Session.save();
                                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                                    myIntent.putExtra("username", username); //Optional parameters
                                    startActivity(myIntent);
                                }
                                else{
                                    createAccountProgressbar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getActivity(), "Something went wrong, please try again.",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );
                JRequester.setRequest(loginReq);
                JRequester.sendRequest();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(createAccountProgressbar.getVisibility() == View.VISIBLE)
            createAccountProgressbar.setVisibility(View.INVISIBLE);
    }
}
