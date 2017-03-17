package com.ciux031701.kandidat360degrees.representation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.widget.ImageView;
import com.ciux031701.kandidat360degrees.R;

/**
 * Created by AMAR on 2017-03-09.
 */

public class FriendTuple {
    private String userName;
    private Drawable profilePicture;

    public FriendTuple(String userName, Drawable profilePicture){
        this.userName = userName;
        this.profilePicture = profilePicture;
    }

    public FriendTuple(String userName, Context context){
        this.userName = userName;
        this.profilePicture = context.getResources().getDrawable(R.drawable.anonymous_profile_image_circle_small);
    }

    public String getUserName(){
        return userName;
    }

    public Drawable getProfilePicture(){
        return profilePicture;
    }
}
