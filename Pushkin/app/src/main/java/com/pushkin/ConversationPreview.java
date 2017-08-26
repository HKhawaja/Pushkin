package com.pushkin;

/**
 * Created by Harith on 04/28/17.
 * This is a file that creates ConversationPreviews for the conversation listView
 */

public class ConversationPreview {

    private String name;
    private String lastActive;
    private String imageURL;
    private String lastMessage;
    private int chatID;


    public ConversationPreview (String fname, String lname, String lastActive, String imageURL, String lastMessage, int chatID) {
        name = fname + " " + lname;
        this.lastActive = lastActive;
        this.imageURL = imageURL;
        this.lastMessage = lastMessage;
        this.chatID = chatID;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getLastActive() {
        return lastActive;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public int getChatID() {
        return chatID;
    }

    public String toString() {
        return name + " " + lastActive + " " + imageURL + " " + lastMessage;
    }
}
