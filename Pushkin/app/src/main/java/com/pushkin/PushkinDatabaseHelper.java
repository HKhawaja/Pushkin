package com.pushkin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pushkin.other.MessageView;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Harith on 04/08/17.
 */

public class PushkinDatabaseHelper extends SQLiteOpenHelper{

    //username
    private static String username = "";

    //database version and name
    private static int database_version = 1;
    private static final String database_name = "Pushkin Database";

    //static variables for Conversations table
    private static final String CONVERSATIONS = "Conversations";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_Chat_ID = "CHAT_ID";
    private static final String KEY_FIRST_NAME = "FirstName";
    private static final String KEY_LAST_NAME = "LastName";
    private static final String KEY_LAST_ACTIVE = "LastActive";
    private static final String KEY_MESSAGES_SENT = "MessagesSent";
    private static final String KEY_MESSAGES_RECEIVED = "MessagesReceived";
    private static final String KEY_TOTAL_POINTS = "TotalPoints";
    private static final String KEY_IMAGE_URL = "ImageURL";
    private static final String KEY_LAST_MESSAGE = "LastMessage";
    private static final String KEY_CHATTER = "Chatter";

    //variables for Chat table. This is a table that contains all messages
    private static final String CHAT = "Chat";
    private static final String KEY_TIME = "Time";
    private static final String KEY_TEXT = "Text";
    private static final String KEY_SENDER = "Sender";
    private static final String KEY_MESSAGE_NUMBER = "Message_Number";
    private static final String KEY_POINTS_EARNED_BY_THIS_MESSAGE = "Points_earned";

    //variables for My table
    private static final String KEY_TOKEN = "Token";
    private static final String KEY_EMAIL = "Email";
    private static final String MY_TABLE = "My_Table";
    private static final String KEY_PRIVATE_KEY = "PrivateKey";
    private static final String KEY_BACKGROUND_IMAGE = "BackgroundImage";

    //variables for Kintacts table
    private static final String KEY_PUBLIC_KEY = "PublicKey";
    private static final String KEY_IMAGE = "Image";
    private static final String KINTACTS = "Kintacts";

    //Conversations table name
    private static final String CONVERSATIONS_NAME = "create table Conversations (" + KEY_USERNAME + " text not null, " +
            KEY_Chat_ID + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1, " + KEY_FIRST_NAME + " text not null, " +
            KEY_LAST_NAME + " text not null, " + KEY_LAST_ACTIVE + " text, " + KEY_MESSAGES_SENT + " int, " +
            KEY_MESSAGES_RECEIVED + " int, " + KEY_TOTAL_POINTS + " float DEFAULT 0, " + KEY_IMAGE_URL + " text not null, " +
            KEY_LAST_MESSAGE + " text, " + KEY_CHATTER + " text DEFAULT \"none\")";

    //Chat table name
    private static final String CHAT_NAME = "create table "+ CHAT + " (" + KEY_TIME + " text not null, " + KEY_TEXT + " text not null, " +
            KEY_SENDER + " text not null, " + KEY_MESSAGE_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1, " +
            KEY_POINTS_EARNED_BY_THIS_MESSAGE + " int DEFAULT 0, " + KEY_Chat_ID + " int)";

    //My table
    private static final String My_Table_Name = "create table " + MY_TABLE + " (" + KEY_USERNAME + " text not null, " +
            KEY_FIRST_NAME + " text, " + KEY_LAST_NAME + " text, " + KEY_IMAGE_URL + " text, " + KEY_TOKEN + " text, " +
            KEY_EMAIL + " text, " + KEY_BACKGROUND_IMAGE + " text, " + KEY_PRIVATE_KEY + " text)";

    //Kintacts table
    private static final String KINTACTS_TABLE = "create table " + KINTACTS + " (" + KEY_USERNAME + " text not null, " +
            KEY_FIRST_NAME + " text, " + KEY_LAST_NAME + " text, " + KEY_IMAGE + " text, " + KEY_PUBLIC_KEY + " text)";

    public PushkinDatabaseHelper(Context context) {
        super(context, database_name, null, database_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CONVERSATIONS_NAME);
        db.execSQL(CHAT_NAME);
        db.execSQL(My_Table_Name);
//        username = getKeyUsername();
        db.execSQL(KINTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(PushkinDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + CONVERSATIONS);
        db.execSQL(CONVERSATIONS_NAME);
        db.execSQL(CHAT_NAME);
        db.execSQL(My_Table_Name);
//        username = getKeyUsername();
        db.execSQL(KINTACTS_TABLE);
    }

    //this method checks whether a conversation with a given Username already exists
    //if so, then just call addReceivedMessage(Message m) with your message
    //if not, then call addConversation(Conversation c) with a new conversation
    public boolean conversationExists (String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        //query the database to check whether a conversation with the given username exists
        Cursor c = db.rawQuery("SELECT Username from " + CONVERSATIONS + " WHERE Username = ?", new String[] {username});
        try{
        if (c.moveToFirst()) {
            if (c.getString(c.getColumnIndex("Username")).equals(username)) {
                return true;
            }
        }}
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return false;
    }

    //add a conversation
    public long addConversation (String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USERNAME, username);
        long chatID = db.insert(CONVERSATIONS, null, values);
        return chatID;
    }


    //adds a message to an existing chat and updates the conversations database
    public void addReceivedMessage (Message message) {
        System.out.println ("adding received message");
        SQLiteDatabase db = this.getWritableDatabase();

        if (conversationExists(message.getSender())) {
            Cursor c = db.rawQuery("select " + KEY_Chat_ID + " from " + CONVERSATIONS + " where " +
                    KEY_USERNAME + " = ?", new String[]{message.getSender()});
            try {
                int chatID = -1;
                if (c.moveToFirst()) {
                    chatID = c.getInt(c.getColumnIndex(KEY_Chat_ID));
                }
                //update Chat table
                ContentValues values = new ContentValues();
                values.put(KEY_TIME, message.getTimeOfMessage());
                values.put(KEY_TEXT, message.getText());
                values.put(KEY_SENDER, message.getSender());
                values.put(KEY_Chat_ID, chatID);
                db.insert(CHAT, null, values);

                //update Conversations
                db.execSQL("UPDATE " + CONVERSATIONS + " SET " + KEY_LAST_ACTIVE + " = ?, " + KEY_LAST_MESSAGE + " = ? " +
                        " WHERE Username = ?", new String[]{message.getTimeOfMessage(), message.getText(), message.getSender()});
            }
            finally {
                // this gets called even if there is an exception somewhere above
                if(c != null)
                    c.close();
            }
        }

        else {
            //add a conversation

            ContentValues values = new ContentValues();
            values.put(KEY_USERNAME, message.getSender());
            values.put(KEY_LAST_ACTIVE, message.getTimeOfMessage());
            values.put(KEY_LAST_MESSAGE, message.getText());
            long toReturn = db.insert(CONVERSATIONS_NAME, null, values);

            //add a chat
            values.clear();
            values.put(KEY_TIME, message.getTimeOfMessage());
            values.put(KEY_TEXT, message.getText());
            values.put(KEY_SENDER, message.getSender());
            values.put(KEY_Chat_ID, toReturn);
            db.insert(CHAT, null, values);
        }
    }

    public void addSentMessage (Message message, int chatID) {
        System.out.println ("Adding message!");
        SQLiteDatabase db = this.getWritableDatabase();

        //update Chat table
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, message.getTimeOfMessage());
        values.put(KEY_TEXT, message.getText());
        values.put(KEY_SENDER, message.getSender());
        values.put(KEY_Chat_ID, chatID);
        values.put(KEY_POINTS_EARNED_BY_THIS_MESSAGE, 0.5);
        db.insert(CHAT, null, values);

        //Update Conversations
//        db.execSQL("UPDATE " + CONVERSATIONS + " SET " + KEY_LAST_ACTIVE + " = \"" + message.getTimeOfMessage() +
//                "\", " + KEY_LAST_MESSAGE + " =\"" + message.getText() + "\" WHERE CHAT_ID = " + chatID);

        db.execSQL("UPDATE " + CONVERSATIONS + " SET " + KEY_LAST_ACTIVE + " = ?, " + KEY_LAST_MESSAGE + " = ? " +
                "WHERE CHAT_ID = ?", new String[] {message.getTimeOfMessage(), message.getText(), chatID + ""});
    }

    //some method to compute the number of points a certain message gets you
    public static float computeNumberOfPoints () {
        return 0;
    }

    public void populate() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from Conversations");

        ContentValues values = new ContentValues();
        values.put(KEY_FIRST_NAME, "Sean");
        values.put(KEY_LAST_NAME, "Fitzgerald");
        values.put(KEY_LAST_ACTIVE, "5:01PM");
        values.put(KEY_IMAGE_URL, "http://cdnak1.psbin.com/img/mw=160/mh=210/cr=n/d=m3gxg/ogoeh4en6evdh5zp.jpg");
        values.put(KEY_MESSAGES_SENT, 5);
        values.put(KEY_MESSAGES_RECEIVED, 0);
        values.put(KEY_USERNAME, "sFitz");
        values.put(KEY_TOTAL_POINTS, 5.5);
        values.put(KEY_LAST_MESSAGE, "Hey, interested in checking out the circus later? \uD83C\uDFAA");

        long fitzID = db.insert(CONVERSATIONS, null, values);
        values.clear();

        values.put(KEY_TIME, "05:01 PM");
        values.put(KEY_TEXT, "Hey, interested in checking out the circus later? \uD83C\uDFAA");
        values.put(KEY_SENDER, "sFitz");
        values.put(KEY_Chat_ID, fitzID);

        db.insert(CHAT, null, values);

        values.clear();
        values.put(KEY_FIRST_NAME, "Francis");
        values.put(KEY_LAST_NAME, "Choi");
        values.put(KEY_LAST_ACTIVE, "4:42PM");
        values.put(KEY_IMAGE_URL, "https://scontent.fzty2-1.fna.fbcdn.net/v/t1.0-1/p100x100/15349620_1181045108656192_8197388348936221309_n.jpg?oh=0641d6ad32163ae353c0f233a5a5e679&oe=59BC2CAF");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "fChoi");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "Mr. Pushkin, so great to see you again yesternoon. Shall we chat again? Lorem Ipsum dipsum wholesome whomesome once upon a foresty inside the buttons.");

        long franID = db.insert(CONVERSATIONS, null, values);

        values.clear();
        values.put(KEY_FIRST_NAME, "Leo");
        values.put(KEY_LAST_NAME, "Tolstoy");
        values.put(KEY_LAST_ACTIVE, "12:20PM");
        values.put(KEY_IMAGE_URL, "https://upload.wikimedia.org/wikipedia/commons/c/c6/L.N.Tolstoy_Prokudin-Gorsky.jpg");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "lTolstoy");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "Peter Ivanovich, like everyone else on such occasions, entered feeling uncertain what he would have to do. All he knew was that at such times it is always safe to cross oneself.");

        db.insert(CONVERSATIONS, null, values);
        values.clear();
        values.put(KEY_FIRST_NAME, "Robert");
        values.put(KEY_LAST_NAME, "Frost");
        values.put(KEY_LAST_ACTIVE, "Yesterday");
        values.put(KEY_IMAGE_URL, "https://www.biography.com/.image/t_share/MTE5NDg0MDU1MjkxOTIxOTM1/robert-frost-9303322-1-402.jpg");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "rFrost");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "These woods are lovely, dark and deep, But I have promises to keep, And miles to go before I sleep, And miles to go before I sleep.");

        db.insert(CONVERSATIONS, null, values);
        values.clear();
        values.put(KEY_FIRST_NAME, "Tsar");
        values.put(KEY_LAST_NAME, "Nicholas I");
        values.put(KEY_LAST_ACTIVE, "Wednesday");
        values.put(KEY_IMAGE_URL, "https://upload.wikimedia.org/wikipedia/commons/c/cc/Franz_Krüger_-_Portrait_of_Emperor_Nicholas_I_-_WGA12289.jpg");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "tNick");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "Alex, we should meet up to discuss the recent developments in a certain context of sorts. Most issues that pertain to such a degree.");

        db.insert(CONVERSATIONS, null, values);

        values.clear();
        values.put(KEY_FIRST_NAME, "Natalia");
        values.put(KEY_LAST_NAME, "Pushkina");
        values.put(KEY_LAST_ACTIVE, "Tuesday");
        values.put(KEY_IMAGE_URL, "https://s-media-cache-ak0.pinimg.com/736x/ba/78/a5/ba78a583f0e5899c5524a3b549d34a24.jpg");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "fChoi");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "Okay honey, see you tonight :)");

        db.insert(CONVERSATIONS, null, values);

        values.clear();
        values.put(KEY_FIRST_NAME, "Emily");
        values.put(KEY_LAST_NAME, "Dickinson");
        values.put(KEY_LAST_ACTIVE, "Tuesday");
        values.put(KEY_IMAGE_URL, "http://frenchquest.files.wordpress.com/2016/04/emily-dickinson.jpg");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "fChoi");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "To make a prairie it takes a clover and one bee, One clover, and a bee, And revery.");

        db.insert(CONVERSATIONS, null, values);

        values.clear();
        values.put(KEY_FIRST_NAME, "James");
        values.put(KEY_LAST_NAME, "Monroe");
        values.put(KEY_LAST_ACTIVE, "Monday");
        values.put(KEY_IMAGE_URL, "http://cdn.history.com/sites/2/2013/11/James_Monroe-AB.jpeg");
        values.put(KEY_MESSAGES_SENT, 3);
        values.put(KEY_MESSAGES_RECEIVED, 2);
        values.put(KEY_USERNAME, "fChoi");
        values.put(KEY_TOTAL_POINTS, 3.0);
        values.put(KEY_LAST_MESSAGE, "I must protest such behavior, James. I can think of only one solution.");

        db.insert(CONVERSATIONS, null, values);

        values.clear();
        values.put(KEY_TIME, "7:59pm");
        values.put(KEY_TEXT, "Quite differentiable.");
        values.put(KEY_SENDER, "fChoi");
        values.put(KEY_Chat_ID, franID);

        db.insert(CHAT,null,values);

    }

    public ArrayList<ConversationPreview> getConversationPreviews() {
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("KEY USERNAME " + getKeyUsername());
        String username = "";
        if(getKeyUsername() != null)
            username = getKeyUsername();
        Cursor c = db.rawQuery("select FirstName, LastName, LastActive, ImageURL, CHAT_ID, LastMessage from Conversations where " +
                "Chatter =? order by LastActive DESC", new String [] {username});
//        System.out.println (c);
        ArrayList<ConversationPreview> list = new ArrayList<ConversationPreview>();
        try{
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                String fname = c.getString(c.getColumnIndex(KEY_FIRST_NAME));
                String lname = c.getString(c.getColumnIndex(KEY_LAST_NAME));
                String lastActive = c.getString(c.getColumnIndex(KEY_LAST_ACTIVE));
                String lastMessage = c.getString(c.getColumnIndex(KEY_LAST_MESSAGE));
                String imageURL = c.getString(c.getColumnIndex(KEY_IMAGE_URL));
                int chatID = c.getInt(c.getColumnIndex(KEY_Chat_ID));

                ConversationPreview cp = new ConversationPreview(fname, lname, lastActive, imageURL, lastMessage, chatID);
                list.add(cp);

                c.moveToNext();
            }
        }}
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        //System.out.println ("getPreviews Size is " + list.size());
        return list;
    }

    public ArrayList<MessageView> getMessages (int chatID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select Time, Sender, Text from Chat where CHAT_ID = ?", new String[] {chatID+""});
        ArrayList<MessageView> messages = new ArrayList<>();

//        System.out.println(c);
        try{
        if (c.moveToFirst()) {
            for (int i = 0; i<c.getCount(); i++) {
                String text = c.getString(c.getColumnIndex("Text"));
                String sender = c.getString(c.getColumnIndex("Sender"));
                String time = c.getString(c.getColumnIndex("Time"));
                MessageView v = new MessageView(sender,text,time);
                messages.add(v);
                c.moveToNext();
            }
        }}
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        //System.out.println(messages);
        return messages;
    }


    public String getFirstName (int chatID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select FirstName from Conversations where CHAT_ID =?", new String[] {chatID+""});
        try{
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex("FirstName"));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    public String getLastName (int chatID) {
        SQLiteDatabase db = this.getWritableDatabase();
           Cursor c = db.rawQuery("select LastName from Conversations where CHAT_ID =?", new String[]{chatID + ""});
    try{
           if (c.moveToFirst()) {
               return c.getString(c.getColumnIndex("LastName"));
           }
           return null;
       }
       finally {
           // this gets called even if there is an exception somewhere above
           if(c != null)
               c.close();
       }
    }

    //returns Username of Conversant
    public String getConversant (int chatID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select Username from Conversations where CHAT_ID =?", new String[] {chatID+""});
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex("Username"));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    //add details to My_Table
    public void addInfo (String username, String fName, String lName, String imageURL, String token, String email,
                         String backgroundImage, String pKey) {
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_FIRST_NAME, fName);
        values.put(KEY_LAST_NAME, lName);
        values.put(KEY_IMAGE_URL, imageURL);
        values.put(KEY_TOKEN, token);
        values.put(KEY_EMAIL, email);
        values.put(KEY_BACKGROUND_IMAGE, backgroundImage);
        values.put(KEY_PRIVATE_KEY, pKey);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(MY_TABLE, null, values);
    }

    public String getKeyBackgroundImage() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select " + KEY_BACKGROUND_IMAGE + " from " + MY_TABLE, null);
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(KEY_BACKGROUND_IMAGE));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }
    public String getKeyFirstName() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select " + KEY_FIRST_NAME + " from " + MY_TABLE, null);
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(KEY_FIRST_NAME));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    public String getKeyLastName() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select " + KEY_LAST_NAME + " from " + MY_TABLE, null);
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(KEY_LAST_NAME));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    public String getKeyImageURL() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select " + KEY_IMAGE_URL + " from " + MY_TABLE, null);
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(KEY_IMAGE_URL));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    //get user's token
    public String getKeyToken() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("select " + KEY_TOKEN + " from " + MY_TABLE, null);
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(KEY_TOKEN));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    public String getKeyUsername() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("select " + KEY_USERNAME + " from " + MY_TABLE, null);
        try {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(KEY_USERNAME));
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return null;
    }

    //removes whatever entries we need to logout
    public void logout() {
        SQLiteDatabase db = this.getWritableDatabase();

        //firstly, get username
        String username = getKeyUsername();

        //delete entry from MyTable and drop kintacts
        db.execSQL("delete from " + MY_TABLE);
        db.execSQL("delete from " + KINTACTS);

        //set all chatter columns in conversations to our username where we are the conversant
//        db.execSQL("update " + CONVERSATIONS + " set " + KEY_CHATTER + " =? where " + KEY_CHATTER + " =?",
//                new String [] {username, "none"});

    }

    //get Kintact list
    public ArrayList<Kintact> getKintacts() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("select * from " + KINTACTS, null);
        ArrayList<Kintact> list = new ArrayList<>();
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i<c.getCount(); i++) {
                    String username = c.getString(c.getColumnIndex(KEY_USERNAME));
                    String fName = c.getString(c.getColumnIndex(KEY_FIRST_NAME));
                    String lName = c.getString(c.getColumnIndex(KEY_LAST_NAME));
                    String image = c.getString(c.getColumnIndex(KEY_IMAGE));
                    String pKey = c.getString(c.getColumnIndex(KEY_PUBLIC_KEY));
                    Kintact myKintact = new Kintact(username,fName,lName,image,pKey);
                    list.add(myKintact);
                    c.moveToNext();
                }
            }
        }
        finally {
            // this gets called even if there is an exception somewhere above
            if(c != null)
                c.close();
        }
        return list;
    }

    //create conversation for kintact if not already here
    public void addConversationKintact (String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("select * from " + CONVERSATIONS + " where " + KEY_USERNAME + " =? and " + KEY_CHATTER +
                " =?", new String[] {username, getKeyUsername()});
        try {
            if (c.moveToFirst()) {
                //do nothing
                System.out.println ("HERE!!!");
            } else {
                //get Kintact
                Cursor d = db.rawQuery("select * from " + KINTACTS + " where " + KEY_USERNAME +
                        " =?", new String[]{username});
                try {
                    if (d.moveToFirst()) {
                        ContentValues values = new ContentValues();
                        values.put(KEY_USERNAME, d.getString(d.getColumnIndex(KEY_USERNAME)));
                        values.put(KEY_FIRST_NAME, d.getString(d.getColumnIndex(KEY_FIRST_NAME)));
                        values.put(KEY_LAST_NAME, d.getString(d.getColumnIndex(KEY_LAST_NAME)));
                        values.put(KEY_CHATTER, getKeyUsername());
                        values.put(KEY_IMAGE_URL, d.getString(d.getColumnIndex(KEY_IMAGE)));
                        db.insert(CONVERSATIONS, null, values);
                    }
                }
                finally {
                    d.close();
                }
            }
        }
        finally {
            if (c!= null) {
                c.close();
            }
        }
    }

    //get a certain Kintact's chatID
    public int getChatID (String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("select " + KEY_Chat_ID + " from " + CONVERSATIONS + " where " + KEY_USERNAME + " =?",
                new String[] {username});
        try {
            if (c.moveToFirst()){
                return c.getInt(c.getColumnIndex(KEY_Chat_ID));
            }
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
        return -1;
    }

    //add Kintacts
    public void addKintact(Kintact k) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, k.getUsername());
        values.put(KEY_IMAGE, k.getImage());
        values.put(KEY_FIRST_NAME, k.getfName());
        values.put(KEY_LAST_NAME,k.getlName());
        values.put(KEY_PUBLIC_KEY,k.getPublicKey());

        db.insert(KINTACTS, null, values);
        System.out.println("ADDED");
    }

    //convert from Kintacts to Conversations
    public void addToConversations() {
        ArrayList<Kintact> kintacts = getKintacts();
        for (int i= 0; i<kintacts.size(); i++) {
            System.out.println("ADDING TO CONVERSATIONS");
            addConversationKintact(kintacts.get(i).getUsername());
        }
    }
}
