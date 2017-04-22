package com.ciux031701.kandidat360degrees.communication;

import android.widget.Adapter;

import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.ciux031701.kandidat360degrees.adaptors.FriendsAdapter;
import com.ciux031701.kandidat360degrees.representation.FriendsAdapterItem;
import com.ciux031701.kandidat360degrees.representation.UserTuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;

/**
 * Created by Neso on 2017-03-29.
 */

public class Friends {
    private static Hashtable<String, ArrayList<UserTuple>> sections;
    private static ArrayList<FriendsAdapterItem> showing;
    private static FriendsAdapter friendsAdapter;

    public static void init(){
        sections = new Hashtable<String, ArrayList<UserTuple>>();
        showing = new ArrayList<FriendsAdapterItem>();
        for(int i = 65; i < 91; i++){
            String letter = Character.toString((char)i);
            sections.put(letter, new ArrayList<UserTuple>());
        }
        fetch();
    }

    public static void setFriendsAdapter(FriendsAdapter adapter) {
        friendsAdapter = adapter;
    }
    public static void fetch(){
        JReqFriends jReqFriends = new JReqFriends();
        jReqFriends.setJResultListener(
                new JRequest.JResultListener() {

                    @Override
                    public void onHasResult(JSONObject result) {
                        boolean error = false;
                        JSONArray friends;
                        try {
                            error = result.getBoolean("error");

                            if (!error) {
                                friends = result.getJSONArray("friends");
                                for (int i = 0; i < friends.length(); i++)
                                    if(get(friends.getJSONObject(i).getString("name")) == null)
                                        add(new UserTuple(friends.getJSONObject(i).getString("name"), ThreeSixtyWorld.getAppContext()));
                                if(friendsAdapter != null) {
                                    friendsAdapter.clearDataSet();
                                    friendsAdapter.addDataSet();
                                    friendsAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException je) {

                        }

                    }
                }
        );
        jReqFriends.sendRequest();
    }

    /**
     * Returns the total number of elements including section letters.
     * @return Data set count, including section headers.
     */
    public static int size(){
        return showing.size();
    }

    /**
     * Returns the number of friends.
     * @return Friend count.
     */
    public static int friendCount(){
        return showing.size() - sections.size();
    }



    public static void add(UserTuple item){
        if(get(item.getUserName()) == null){
            getSection(item).add(item);
            refresh();
        }
    }

    public static void remove(UserTuple item){
        getSection(item).remove(item);
        refresh();
    }

    private static ArrayList<UserTuple> getSection(UserTuple item){
        return getSection(item.getUserName());
    }

    private static ArrayList<UserTuple> getSection(String username){
        return sections.get(username.substring(0,1).toUpperCase());
    }

    public static UserTuple get(String username){
        ArrayList<UserTuple> section = getSection(username.substring(0,1).toUpperCase());
        for(int i = 0; i < section.size(); i++){
            UserTuple user = section.get(i);
            if(user.getUserName().equals(username))
                return user;
        }
        return null;
    }

    public static ArrayList<FriendsAdapterItem> getFriendsAdapterItems(){return showing;}

    private static void refresh(){
        showing = new ArrayList<>();
        for(int i = 65; i < 91;i++){
            String letter = Character.toString((char)i);
            ArrayList<UserTuple> section = sections.get(letter);
            if(!section.isEmpty()){
                showing.add(new FriendsAdapterItem(section.get(0).getContext(), letter));
                Iterator it = section.iterator();
                while(it.hasNext()){
                    UserTuple data = (UserTuple)it.next();
                    showing.add(new FriendsAdapterItem(FriendsAdapterItem.FRIEND, data));
                }

            }
        }
    }

}
