package com.ciux031701.kandidat360degrees.representation;

import android.content.Context;

/**
 * Created by Neso on 2017-04-01.
 */

public class FriendsAdapterItem {
    public static final int HEADER = 0;
    public static final int REQUEST = 1;
    public static final int FRIEND = 2;
    private boolean selected = false;
    private int type;
    private UserTuple data;
    public FriendsAdapterItem(Context context, String header){
        data = new UserTuple(header, context);
        this.type = HEADER;
    }
    public FriendsAdapterItem(int type, UserTuple user){
        data = user;
        this.type = type;
    }
    public int getType(){return type;}
    public String getDataText(){return data.getUserName();}
    public UserTuple getData(){return data;}

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean sel){
        selected = sel;
    }
}
