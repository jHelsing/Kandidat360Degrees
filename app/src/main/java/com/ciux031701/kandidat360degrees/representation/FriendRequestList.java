package com.ciux031701.kandidat360degrees.representation;

import com.ciux031701.kandidat360degrees.representation.FriendTuple;

import java.util.ArrayList;

/**
 * Created by Neso on 2017-03-29.
 */

public class FriendRequestList extends ArrayList<FriendTuple> {

    @Override
    public boolean add(FriendTuple item){
        if(size() == 0)
            super.add(0, new FriendTuple("Friend Requests", item.getContext()));
        return super.add(item);
    }
    public void add(int i, FriendTuple item){
        if(size() == 0)
            super.add(0, new FriendTuple("Friend Requests", item.getContext()));
        super.add(i, item);
    }
    @Override
    public boolean remove(Object item){
        //Remove header also if last friend request to be removed.
        if(size()==2) {
            clear();
            return true;
        }
        else
            return super.remove(item);
    }
}
