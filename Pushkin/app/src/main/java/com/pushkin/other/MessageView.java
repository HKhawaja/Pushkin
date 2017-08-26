package com.pushkin.other;

/**
 * Created by Harith on 05/05/17.
 */

public class MessageView {

    private String sender;
    private String text;
    private String time;

    public MessageView(String sender, String text, String time){
        this.sender = sender;
        this.text = text;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

}
