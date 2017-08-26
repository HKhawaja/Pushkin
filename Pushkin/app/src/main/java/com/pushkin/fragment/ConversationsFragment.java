package com.pushkin.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pushkin.ConversationPreview;
import com.pushkin.Message;
import com.pushkin.PushkinAdapter;
import com.pushkin.PushkinDatabaseHelper;
import com.pushkin.R;
import com.pushkin.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConversationsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConversationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversationsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DB_HELPER = "dbHelper";
    private static final String RUN = "run";

    // TODO: Rename and change types of parameters
    public static PushkinDatabaseHelper dbHelper;
    private Boolean run;

    private OnFragmentInteractionListener mListener;
//    public static boolean isloaded = false;

    private View rootView;
    private ArrayList<Message> messageArrayList = new ArrayList<>();
    private static Context context;
    private FrameLayout frameLayout;
//    public static Handler handler;


    public ConversationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConversationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConversationsFragment newInstance(String param1, boolean param2) {
        ConversationsFragment fragment = new ConversationsFragment();
        Bundle args = new Bundle();
//        args.putSerializable(DB_HELPER, dbHelper);
        args.putBoolean(RUN, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Runnable runnable = new Runnable() {
//            @Override
//                public void run() {
//                    updateAdapter();
//                    System.out.println ("handler running");
//
//                }
//            };

//        linearLayout = (LinearLayout) rootView.findViewById(R.id.ll_conversations);
//        TextView tv = (TextView) linearLayout.findViewById(R.id.tv);
//        tv.setText("this is me");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_conversations, container, false);
//        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.frag_conversations);
//        System.out.println (linearLayout);
//        TextView tv = (TextView) linearLayout.findViewById(R.id.tv);
//        tv.setText("Hey");
//        System.out.println("Added textView");
        dbHelper = MainActivity.dbHelper;
        //System.out.println(dbHelper);
        //dbHelper.addToConversations();

        //start thread
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    if (run) {
                        System.out.println("Thread running");
                        sleep(5000);
                        new ConversationsAsyncTask().execute();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        if (getArguments() != null) {
            run = getArguments().getBoolean(RUN,false);
            System.out.println(run);
        }

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("handler running!");
                // upadte textView here
                new ConversationsAsyncTask().execute();
                updateAdapter();
                handler.postDelayed(this, 5000); // set time here to refresh textView
            }
        });

//        if (!run) {
//            System.out.println ("HANDLER STOPPED!");
//            handler.removeCallbacksAndMessages(null);
//        }

//        updateAdapter();
        return rootView;
    }

//    public static Handler getHandler() {
//        return handler;
//    }

    public void updateAdapter() {
        ListView listView = (ListView) rootView.findViewById(R.id.conversations_listView);
        ArrayList<ConversationPreview> previews = dbHelper.getConversationPreviews();
        System.out.println ("Size of previews " + previews.size());
//        System.out.println (previews);
        if (context == null)
            context = getActivity();
        PushkinAdapter adapter = new PushkinAdapter(context, R.layout.conversation_preview, previews);
        listView.setAdapter(adapter);

    }

    public void updateDatabase() {
        for (int i =0; i< messageArrayList.size(); i++) {
            Message m = messageArrayList.get(i);
            dbHelper.addReceivedMessage(m);
        }
    }
    //    public void onActivityCreated (Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public String readUserTokenFile(){
        String authorizationToken = "";
//        try {
//            FileInputStream fIn;
//            fIn = getContext().openFileInput("userToken.dat");
//            int n;
//            while((n = fIn.read()) != -1) {
//                authorizationToken = authorizationToken + Character.toString((char)n);
//
//            }
//            fIn.close();
//        }
//        catch (IOException e) {
//            authorizationToken = "0:0";
//            e.printStackTrace();
//        }
//        System.out.println("AUTHX" + authorizationToken);
        authorizationToken = dbHelper.getKeyUsername() + ":" + dbHelper.getKeyToken();
        return authorizationToken;
        //return "noah:WQ1I5TdkOTUyOGQyMWE1OTk1OTMzYTUwYTFjMGMxMzIxZjZhNTNkYzUyZjA=";
    }

    public String getLocalUser(){
        String token = readUserTokenFile();
        return token.split(":")[0];
    }

    public String getLocalAuthToken(){
        String token = readUserTokenFile();
        return token.split(":")[1];
    }


    public class ConversationsAsyncTask extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection harpoon;
            String url = "http://148.85.240.18:8080/getMsg";
            String result = null;
            String thing = "{\"authorization\":\"" + getLocalUser() + ":" + getLocalAuthToken() + "\"}";

            try {
                //Connect
                harpoon = (HttpURLConnection) ((new URL(url).openConnection()));
                harpoon.setDoOutput(true);
                harpoon.setRequestProperty("Content-Type", "application/json");
                harpoon.setRequestProperty("Accept", "application/json");
                harpoon.setRequestMethod("POST");
                try {
                    harpoon.connect();
                }
                catch (java.net.ConnectException e){
                    //Snackbar.make(getView(), "Can't connect to the server. Check network settings.", Snackbar.LENGTH_LONG)
                    //.setAction("Retry", whatIsThat)
                    //.setActionTextColor(Color.RED)
                    //.show();
                    //cant connect to server.
                    return false;
                }
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
//                String success = json.getString("Success");
//                if (success.equals("0")) {
//                    //we have failed to login, throw bad login.
//                    System.out.println(result);
//                    System.out.println(success);
//                    return false;
//                }
//                //else, we are good, get token and store it...

                JSONArray response = json.getJSONArray("Messages");
                //System.out.println ("response length!" + response.length());
                for (int i = 0; i<response.length(); i++ ) {
                    JSONObject each = response.getJSONObject(i);
                    String sender = each.getString("Sender");
                    String text = each.getString("Content");
                    String time = each.getString("Time");

                    dbHelper.addReceivedMessage(new Message(sender, time, text));
                }

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

        protected void onPostExecute (Boolean result) {
            super.onPostExecute(result);
            //System.out.println("MEssage array list!!!" + messageArrayList);
            if (result) {
//                updateDatabase();
            }
//            updateAdapter();
        }

    }
}


