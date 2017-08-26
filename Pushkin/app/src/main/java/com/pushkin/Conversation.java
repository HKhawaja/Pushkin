package com.pushkin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Harith on 04/08/17.
 * Conversation class
 */

public class Conversation {

    //username is that of the recipient, not the starter!!
    private String username;
    private String firstName;
    private String lastName;
//    private String firstMessage;
//    private String timeOfFirstMessage;
    //private int numberOfMessages;

    public Conversation (String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
//        this.firstMessage = firstMessage;
//        this.timeOfFirstMessage = timeOfFirstMessage;
        //this.numberOfMessages = numberOfMessages;
    }

    //getter methods
    public String getUsername() {
        return username;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
//    public String getFirstMessage() {
//        return firstMessage;
//    }
//    public String getTimeOfFirstMessage() {
//        return timeOfFirstMessage;
//    }
//    public int getNumberOfMessages() {
//        return numberOfMessages;
//    }

    //here is a function that converts util.Date to a format that can be used to store in the database
    public static String getDateTime(Date timeOfFirstMessage) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(timeOfFirstMessage);
    }



}
