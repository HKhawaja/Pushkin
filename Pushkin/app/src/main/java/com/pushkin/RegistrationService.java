package com.pushkin;


import android.app.IntentService;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegistrationService extends IntentService {

    public static final String TAG = "Registration Service";

    public RegistrationService() {
        super("RegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID myID = InstanceID.getInstance(this);
        try {
            String registrationToken = myID.getToken(
                    getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );
            //System.out.println("Check out this sick token. " + registrationToken);
            //Send to server
            try {
                FileOutputStream fOut = openFileOutput("fcmToken.dat",MODE_PRIVATE);
                fOut.write(registrationToken.getBytes());
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            GcmPubSub subscription = GcmPubSub.getInstance(this);
//            subscription.subscribe(registrationToken, "/topics/my_little_topic", null);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }
}