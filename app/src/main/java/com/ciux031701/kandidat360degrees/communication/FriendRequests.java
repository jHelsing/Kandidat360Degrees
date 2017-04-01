package com.ciux031701.kandidat360degrees.communication;

import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.ciux031701.kandidat360degrees.representation.FriendsAdapterItem;
import com.ciux031701.kandidat360degrees.representation.UserTuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Neso on 2017-03-29.
 */

public class FriendRequests {
    private static ArrayList<UserTuple> requests;
    private static ArrayList<FriendsAdapterItem> showing;

    public static void init(){

        requests = new ArrayList<>();
        fetch();
    }


    private static void fetch(){
        JReqFriendRequests jReqFriendRequests = new JReqFriendRequests();
        jReqFriendRequests.setJResultListener(
                new JRequest.JResultListener() {

                    @Override
                    public void onHasResult(JSONObject result) {
                        boolean error = false;
                        JSONArray friendrequests;
                        try {
                            error = result.getBoolean("error");

                            if (!error) {
                                friendrequests = result.getJSONArray("friendrequests");
                                for (int i = 0; i < friendrequests.length(); i++)
                                    add(new UserTuple(friendrequests.getJSONObject(i).getString("name"), ThreeSixtyWorld.getAppContext()));
                            }
                        } catch (JSONException je) {

                        }

                    }
                }
        );
        jReqFriendRequests.sendRequest();
    }

    /**
     * Gets the size of the total dataset.
     * @return Size of dataset.
     */
    public static int size(){
        if(requests.size() != 0)
            return requests.size() + 1;
        else
            return 0;
    }
    /**
     * Gets the number of the friendrequests.
     * @return Size of dataset.
     */
    public static int requestCount(){
        if(requests.size() != 0)
            return requests.size() + 1;
        else
            return 0;
    }

    public static void add(UserTuple sender){
        requests.add(sender);
    }
    public static void remove(UserTuple request) {requests.remove(request);}


    public static ArrayList<FriendsAdapterItem> getFriendsAdapterItems(){
        showing = new ArrayList<>();
        if(requests.size() != 0){
            showing.add(new FriendsAdapterItem(ThreeSixtyWorld.getAppContext(), "Friend Requests"));
            for(int i = 0; i < requests.size();i++){
                showing.add(new FriendsAdapterItem(FriendsAdapterItem.REQUEST, requests.get(i)));
            }
        }
       return showing;
    }
}
