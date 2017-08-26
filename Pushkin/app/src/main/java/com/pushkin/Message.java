package com.pushkin;

/**
 * Created by Harith on 04/08/17.
 */

public class Message {

    //username of sender
    private String sender;
    private String timeOfMessage;
    private String text;

    public Message (String sender, String timeOfMessage, String text) {
        this.sender = sender;
        this.timeOfMessage = timeOfMessage;
        this.text = text;
    }

    public String getSender() {return sender;}
    public String getTimeOfMessage() {return timeOfMessage;}
    public String getText() {return text;}

}
