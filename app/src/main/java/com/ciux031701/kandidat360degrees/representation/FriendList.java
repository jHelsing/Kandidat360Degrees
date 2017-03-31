package com.ciux031701.kandidat360degrees.representation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;

/**
 * Created by Neso on 2017-03-29.
 */

public class FriendList {
    private Hashtable<String, ArrayList<FriendTuple>> sections;
    private ArrayList<FriendTuple> list;

    public FriendList(){
        sections = new Hashtable<String, ArrayList<FriendTuple>>();
        list = new ArrayList<FriendTuple>();
        for(int i = 65; i < 91; i++){
            String letter = Character.toString((char)i);
            sections.put(letter, new ArrayList<FriendTuple>());
        }
    }
    public void add(FriendTuple item){
        getSection(item).add(item);
        refresh();
    }

    public void remove(FriendTuple item){
        getSection(item).remove(item);
        refresh();
    }

    private ArrayList<FriendTuple> getSection(FriendTuple item){
        return sections.get(item.getUserName().substring(0,1).toUpperCase());
    }

    public ArrayList<FriendTuple> getList(){return list;}

    private void refresh(){
        list = new ArrayList<FriendTuple>();
        for(int i = 65; i < 91;i++){
            String letter = Character.toString((char)i);
            ArrayList<FriendTuple> section = sections.get(letter);
            if(!section.isEmpty()){
                list.add(new FriendTuple(letter, section.get(0).getContext()));
                Iterator it = section.iterator();
                while(it.hasNext())
                    list.add((FriendTuple)it.next());

            }
        }
    }

}
