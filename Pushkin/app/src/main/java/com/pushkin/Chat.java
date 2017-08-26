package com.pushkin;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pushkin.activity.MainActivity;
import com.pushkin.fragment.ConversationsFragment;
import com.pushkin.other.MessageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Chat extends AppCompatActivity {

    CustomAdapter<MessageView> mAdapter;
    ArrayList<String> data;
    EditText editText;
    Message m;
    private String[] activityTitles;
    public static int navItemIndex = 0;
    private boolean photoTaken;

    public static PushkinDatabaseHelper dbHelper;
    int chatID;
    private Toolbar toolbar;

    static String IV = "AAAAAAAAAAAAAAAA";
    static String plaintext = "test text 123\0\0\0"; /*Note null padding*/
    static String encryptionKey = "0123456789abcdef";

    private ListView listView;
    private Bitmap imageBitmap;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void updateAdapter() {
        //get Messages
        ArrayList<MessageView> messages = dbHelper.getMessages(chatID);
        //Create the adapter with this new info
        mAdapter = new CustomAdapter<MessageView>(this, R.layout.activity_listview, R.id.listview, messages);
        listView = (ListView) findViewById(R.id.listview);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setAdapter(mAdapter);
        //listView.smoothScrollToPosition(listView.getCount() - 1);

        //Set the view of this adapter to the bottom
        listView.setSelection(listView.getCount() - 1);
    }

    private void setToolbarTitle(String chatName) {
        getSupportActionBar().setTitle(chatName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            sendImage();
            //send the image to the server, needs a special handler
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        dbHelper = MainActivity.dbHelper;
        chatID = getIntent().getIntExtra("CHAT_ID", 0);
        String firstName = dbHelper.getFirstName(chatID);
        String lastName = dbHelper.getLastName(chatID);

        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarTitle(firstName + " " + lastName);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            //getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        }

        //System.out.println (chatID);
        //System.out.println("if chatID is 0, then error!!");
//        System.out.println("here");
//        Intent intent = new Intent(this, MainConversationView.class);
//        startActivity(intent);

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //update textView here
                updateAdapter(); //there should be no need for this update adpater right?
                handler.postDelayed(this, 5000); // set time here to refresh textView
            }
        });

        //
        editText = (EditText) findViewById(R.id.messagebox);
//
        final Button sendButton = (Button) findViewById(R.id.sendbutton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide the keyboard
//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(sendButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        final Button cameraButton = (Button) findViewById(R.id.camerabutton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Chat Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class CustomAdapter<E> extends ArrayAdapter<MessageView> {

        public CustomAdapter(Context context, int resource, int id, ArrayList<MessageView> data) {
            super(context, resource, id, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            MessageView s = getItem(position);

            String sender = s.getSender();
            String text = s.getText();
            String time = s.getTime();
            String firstName = dbHelper.getFirstName(chatID);
            String lastName = dbHelper.getFirstName(chatID);

            //Date date = new Date(Long.parseLong(time)*1000L); // *1000 is to convert seconds to milliseconds
            //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a"); // the format of your date
            //sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
            //time = sdf.format(date);

            if (!sender.equals(getLocalUser())) {
                //System.out.println("Inflating view to [to]");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_listview, parent, false);

                // Lookup view for data population
                TextView username = (TextView) convertView.findViewById(R.id.username);

                if (text.startsWith("FFD8")) {
                    //textMessage.setVisibility(View.INVISIBLE);
                    ImageView photoView = (ImageView) convertView.findViewById(R.id.photoview);
                    ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(photoView.getLayoutParams());
                    marginParams.setMargins(10, 10, 10, 10);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
                    photoView.setLayoutParams(layoutParams);
                    //Noah: ASSIGN PHOTO TO mImageView HERE

                    //setting image position
                    //photoView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    //        RelativeLayout.LayoutParams.WRAP_CONTENT));

                    photoView.setVisibility(View.VISIBLE);

                    byte[] decodedString = Base64.decode(text.substring(4), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    photoView.setImageBitmap(decodedByte);
                    System.out.println("PICTURE");
                } else {
                    TextView textMessage = (TextView) convertView.findViewById(R.id.textmessage);
                    textMessage.setText(text);
                    textMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                }

                TextView timeStamp = (TextView) convertView.findViewById(R.id.timestamp);
                ImageView photoView = (ImageView) convertView.findViewById(R.id.photoview);
                // Populate the data into the template view using the data object
                username.setText(firstName);
                timeStamp.setText(time);

                username.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                username.setTypeface(null, Typeface.BOLD);

                timeStamp.setTextSize(11);
            } else {
                //from
                //System.out.println("Inflating view to [from]");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_listview_from, parent, false);
                convertView.setBackgroundColor(Color.parseColor("#B3E5FC"));
                convertView.getBackground().setAlpha(116);
                // Lookup view for data population
                if (text.startsWith("FFD8")) {
                    //textMessage.setVisibility(View.INVISIBLE);
                    ImageView photoView = (ImageView) convertView.findViewById(R.id.photoview);

                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) photoView.getLayoutParams();
                    marginParams.setMargins(0, 30, 0, 16);
                    //Noah: ASSIGN PHOTO TO mImageView HERE

                    //setting image position
                    //photoView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    //        RelativeLayout.LayoutParams.WRAP_CONTENT));

                    photoView.setVisibility(View.VISIBLE);


                    byte[] decodedString = Base64.decode(text.substring(4), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    photoView.setImageBitmap(decodedByte);
                } else {
                    TextView textMessage = (TextView) convertView.findViewById(R.id.textmessage);
                    textMessage.setText(text);
                    textMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                }

                TextView username = (TextView) convertView.findViewById(R.id.username);
                TextView timeStamp = (TextView) convertView.findViewById(R.id.timestamp);
                // Populate the data into the template view using the data object
                username.setText("Me");
                timeStamp.setText(time);

                username.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                username.setTypeface(null, Typeface.BOLD);
                timeStamp.setTextSize(11);

            }
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public String getTime() {
        Calendar cal = Calendar.getInstance();
        //long unixTime = System.currentTimeMillis() / 1000;
        Date currentDate = new Date();
        cal.setTime(currentDate);
        //cal.add(Calendar.SECOND, -30080);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(cal.getTime());
    }

    public PublicKey readPublicKey(String pKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.decode(pKey.getBytes(), Base64.NO_WRAP);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(spec);
        return key;
    }

    public PrivateKey readPrivateKey(String sKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.decode(sKey.getBytes(), Base64.NO_WRAP);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        return priv;
    }

    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        encrypt.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedMessage = encrypt.doFinal(message.getBytes());
        //System.out.println("Some encrypted junk " + new String(encryptedMessage, "UTF-8"));
        return encryptedMessage;
    }

    public static String decrypt(PrivateKey sKey, String b64encodedJunk) throws Exception {
        byte[] blob = Base64.decode(b64encodedJunk, Base64.NO_WRAP);
        Cipher decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decrypt.init(Cipher.DECRYPT_MODE, sKey);

        String decryptedMessage = new String(decrypt.doFinal(blob));
        //System.out.println("Some decrypted junk " + decryptedMessage);
        return decryptedMessage;
    }

    public static byte[] encryptAES(String plainText, byte[] encryptionKey) throws Exception {
        //encryptionKey is random generated
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    public static String decryptAES(byte[] cipherText, byte[] encryptionKey) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return new String(cipher.doFinal(cipherText),"UTF-8");
    }

    public void sendMessage() throws Exception {

        String message = editText.getText().toString();
        
        String paddedMSG = String.format("%0"+(32-message.length())+"d%s", 0, message);
        //get public key protected DEC/ENC key for AES
        byte[] encryptKey = encrypt(readPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDVoFdTqoDyYUxA6uWhrEdtQaUJpb4eUb37qo2VEzM9uts9uGJ/+IPZtnZNraLPjCo10866hu/jfV9JxM2VcxmOxXoWOtC46E660HViBrQYTDOKSzMf1sNSup7WQFkg3+YQuAcscZRaEQA2tXlbsxt107/ryiHcfypHPxFTlOcTVQIDAQAB"), encryptionKey);
        System.out.println("Encryption Key Length: " + encryptKey.length);
        System.out.println("Message length: " + message.length());
        System.out.println("Padded MSG: " + paddedMSG);
        System.out.println("Padded MSG length: " + paddedMSG.length());
        //do AES encryption with the encrypted random key (right now it is just 01234...)
         byte[] encryptMsg = encryptAES(message, encryptKey);
//       System.out.println("Encrypted Junk::: " + new String(encryptMsg));

        //So encryptMSG is the encrypted message that we want to send.
        //encryptKey is the key that needs to be decrypted with an RSA private key
        //once decrypted with RSA, it can be used to decrypt the message.
        //so we want to send the encryptKey along with the encryptMSG.

        String b64eMsg = Base64.encodeToString(encryptMsg, Base64.NO_WRAP);
        String b64eKey = Base64.encodeToString(encryptKey, Base64.NO_WRAP);

        String decryptKey = decrypt(readPrivateKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANWgV1OqgPJhTEDq5aGsR21BpQmlvh5RvfuqjZUTMz262z24Yn/4g9m2dk2tos+MKjXTzrqG7+N9X0nEzZVzGY7FehY60LjoTrrQdWIGtBhMM4pLMx/Ww1K6ntZAWSDf5hC4ByxxlFoRADa1eVuzG3XTv+vKIdx/Kkc/EVOU5xNVAgMBAAECgYEAgmDvqzJ+rG9RmLVEHN4GYdoUncS4OcztxHEdJASp21z1fb/Q7gWAgxlnLpVwrnb/NAbnPtQoCJF13JHuXu32hBzBYTzE1p/U839t3jIGf8MJjg/exqVYNb+vn50kGq/7jouk7/2WUM/hMx+33yIwyfd9vfxbrVretBv3cSahAukCQQDyigUWqd91t9PBkngjxAiVp9xfxcIxgPAyBzWaXQo3BPsAPKpVzavhMgxjyR6tmrROVDqNG+H+YJ0WEEs4jg9jAkEA4XuHrJfiCFWFbNwk/NrGeIShyc6vjlp+isJ2hD55Rndi9kzFF0Sv9vu0HLNA/UKEqkaMWKY4AbPfKdA3d3Vb5wJAAKIBj2SUE6+OGuQx8g3x48oTViCi+BQZDFheeG+jti8KJJ8D5sNpnmXOCHie3t8Xd1ja6kFXXp2L62HRgG1GdwJBAKKz+pCmACo3W9HTgLUcQH6SZhQzGibEUe9apx6B3gzk9Pn5J3bEz5yOv8a96jVVnFkqEvec6WkBHBUV9BA1FR0CQFaXl55KX4+YD+O402OIQPF4Cx9I/ICWDC1qId83yxK/vF5NeXG3uj0ihl50mj+TjoV3IxLVHNOqmT132uSWkZY="), b64eKey);
        //this decryptKey can be used to decrypt the AES data...

        System.out.println("ENCRYPTED DATA::: " + encryptMsg);
        System.out.println("DECRTYPED DATA::: " + decryptAES(encryptMsg, decryptKey));
        //VERIFY PADDED BYTES BEFORE SENDING, OTHERWISE BLOCK CIPHER WILL NOT WORK - BAD BAD BAD
        //message = Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
        String sender = getLocalUser();
        String time = getTime();
        m = new Message(sender, time, message);

        editText.setText("");

        sendtoServer(m);

    }

    public void sendImage() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 1, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        String sender = getLocalUser();
        String time = getTime();
        m = new Message(sender, time, "FFD8" + encoded);

        sendtoServer(m);
    }

    public void sendtoServer(Message message) {
        new PushkinAsyncTask().execute(message);
    }

    public String readUserTokenFile() {
        String authorizationToken = "";
        authorizationToken = dbHelper.getKeyUsername() + ":" + dbHelper.getKeyToken();
        return authorizationToken;
    }

    public String getLocalUser() {
        String token = readUserTokenFile();
        return token.split(":")[0];
    }

    public String getLocalAuthToken() {
        String token = readUserTokenFile();
        return token.split(":")[1];
    }

    public class PushkinAsyncTask extends AsyncTask<Message, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Message... params) {
            String localUser = getLocalUser();
            String token = getLocalAuthToken();
            Message m = params[0];
            String message = params[0].getText();

            String recipient = dbHelper.getConversant(chatID);


            //encode the Base64.encodeBase64 expects byte[]. eMsg is a byte[] that contains the encrypted message

            byte[] b64Msg = Base64.encode(message.getBytes(), Base64.NO_WRAP);
            HttpURLConnection harpoon;
            String url = "http://148.85.240.18:8080/sendMsg";
            String result = null;
            String thing = "{\"recipient\":\"" + recipient + "\",\"message\":\"" + message + "\",\"authorization\":\"" + getLocalUser() + ":" + getLocalAuthToken() + "\"}";

            try {
                //Connect
                harpoon = (HttpURLConnection) ((new URL(url).openConnection()));
                harpoon.setDoOutput(true);
                harpoon.setRequestProperty("Content-Type", "application/json");
                harpoon.setRequestProperty("Accept", "application/json");
                harpoon.setRequestMethod("POST");
                harpoon.connect();

                //Write
                OutputStream os = harpoon.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(thing);
                writer.close();
                os.close();

                //Read
                BufferedReader br = new BufferedReader(new InputStreamReader(harpoon.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                result = sb.toString();
                JSONObject json = new JSONObject(result);
                String success = json.getString("Success");
                if (success.equals("0")) {
                    //we have failed to login, throw bad login.
                    //System.out.println(result);
                    //System.out.println(success);
                    return false;
                }
                //else, we are good, get token and store it...
                String response = json.getString("Message");
                //System.out.println(response);
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        private View.OnClickListener whatIsThat = new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("buttonWasClicked");
            }
        };

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                dbHelper.addSentMessage(m, chatID);
                updateAdapter();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Sorry, we couldn't send that message.", Snackbar.LENGTH_LONG)
                        //.setAction("Retry", whatIsThat)
                        //.setActionTextColor(Color.RED)
                        .show();
            }
        }
    }
}
