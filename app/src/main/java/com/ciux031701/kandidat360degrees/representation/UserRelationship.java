package com.ciux031701.kandidat360degrees.representation;

import java.util.InvalidPropertiesFormatException;

/**
 * Created by Neso on 2017-03-31.
 */

public class UserRelationship {
    private int relationship;
    public final static int SELF = -1;
    public final static int NONE = 0;
    public final static int FRIEND = 1;
    public final static int PENDING_REQUESTER = 2;
    public final static int PENDING_REQUESTEE = 3;
    public UserRelationship(int relationship){
        if(relationship >= -1 && relationship <= 3 )
            this.relationship = relationship;
        else
            try {
                throw new Exception("Relationship type does not exist.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        //TODO throw
    }
    public int getValue() {return relationship;}
}
