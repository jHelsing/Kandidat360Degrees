package com.ciux031701.kandidat360degrees.representation;

import android.content.Context;

import java.util.ArrayList;


/**
 * Created by Neso on 2017-03-29.
 */

public class FriendListSection extends ArrayList<FriendTuple> {
    private String letter;
    public FriendListSection(String c){
        this.letter = c;
    }
    public String getLetter(){return letter;}

}
