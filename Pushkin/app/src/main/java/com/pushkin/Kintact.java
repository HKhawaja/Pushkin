package com.pushkin;

/**
 * Created by Harith on 05/07/17.
 */

public class Kintact {
    private String username;
    private String fName;
    private String lName;
    private String image;
    private String publicKey;

    public Kintact (String username, String fName, String lName, String image, String publicKey) {
        this.username = username;
        this.fName = fName;
        this.lName = lName;
        this.image = image;
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public String getfName() {
        return fName;
    }

    public String getImage() {
        return image;
    }

    public String getlName() {
        return lName;
    }

    public String getPublicKey() {
        return publicKey;
    }
}