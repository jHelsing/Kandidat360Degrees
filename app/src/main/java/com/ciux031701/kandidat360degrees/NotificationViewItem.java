package com.ciux031701.kandidat360degrees;

/**
 * Created by Anna on 2017-03-09.
 * Used in the list of notifications in NotificationFragment.
 */

public class NotificationViewItem {
    private String text;
    private int type;

    public NotificationViewItem(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
