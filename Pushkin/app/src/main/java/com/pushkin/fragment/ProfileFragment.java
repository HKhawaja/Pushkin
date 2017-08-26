package com.pushkin.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.pushkin.Login;
import com.pushkin.R;
import com.pushkin.activity.MainActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static Context context;
    private String[] times={"12am-2am","2am-4am","4am-6am","6am-8am","8am-10am","10am-12pm","12pm-2pm","2pm-4pm","4pm-6pm","6pm-8pm","8pm-10pm","10pm-12am"};
    private int[] messagesbyTime={10,6,5,4,10,12,24,30,23,10,10,19};
    PieChart pieChart;
    final String TAG="UserProfile";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View something = inflater.inflate(R.layout.fragment_profile, container, false);
        if(context == null){
            context = getActivity();
        }
        // Inflate the layout for this fragment
        pieChart=(PieChart)something.findViewById(R.id.idPieChart);
        ImageView i = (ImageView) something.findViewById(R.id.imageView);
        String navHeaderPic = Login.dbHelper.getKeyBackgroundImage();
        new ImageLoadTask(navHeaderPic, i).execute();

        TextView messagesSent = (TextView) something.findViewById(R.id.messagesSent);
        TextView messagesReceived = (TextView) something.findViewById(R.id.messagesReceived);
        TextView kintactsNumber = (TextView) something.findViewById(R.id.kintactsNumber);
        TextView nickname = (TextView) something.findViewById(R.id.displayNickname);

        int sentNum = 2958;
        int receivedNum = 3845;
        int kintactsNum = 143;
        String s = Login.dbHelper.getKeyFirstName();;

        messagesSent.setText("Messages Sent: " + sentNum);
        messagesReceived.setText("Messages Received: " + receivedNum);
        kintactsNumber.setText("Number of Kintacts: " + kintactsNum);
        nickname.setText(s);

        pieChart.setCenterText("Activity by time");
        pieChart.setCenterTextSize(18);
        pieChart.setRotationEnabled(true);
        pieChart.setTransparentCircleAlpha(0);
        addDataSet(pieChart);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: Value selected from chart.");
                Log.d(TAG, "onValueSelected: "+ e.toString());
                Log.d(TAG, "onValueSelected: "+ h.toString());

                String string1 = h.toString();
                string1= string1.substring(string1.indexOf("x: ")+3);
                string1= string1.substring(0, string1.indexOf(".0, y"));
                Log.d(TAG, "string1: "+string1);
                int pos=Integer.parseInt(string1);

                String strings=times[pos];

                int num=messagesbyTime[pos];
            }

            @Override
            public void onNothingSelected() {

            }
        });

        return something;
    }

    private void addDataSet(PieChart pieChart) {
        ArrayList<PieEntry> yEntrys=new ArrayList<>();
        ArrayList<String> xEntrys=new ArrayList<>();

        for(int i=0;i< messagesbyTime.length;i++){
            yEntrys.add(new PieEntry(messagesbyTime[i],i));
        }
        for(int i=0;i<times.length;i++){
            xEntrys.add(times[i]);
        }

        PieDataSet pieDataSet= new PieDataSet(yEntrys, "Times");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        ArrayList<Integer> colors= new ArrayList<>();
        colors.add(Color.parseColor("#003399"));
        colors.add(Color.parseColor("#6600CC"));
        colors.add(Color.parseColor("#cc00cc"));
        colors.add(Color.parseColor("#cc0066"));
        colors.add(Color.parseColor("#cc0000"));
        colors.add(Color.parseColor("#cc6600"));
        colors.add(Color.parseColor("#ffcc66"));
        colors.add(Color.parseColor("#cccc00"));
        colors.add(Color.parseColor("#66cc00"));
        colors.add(Color.parseColor("#00cc66"));
        colors.add(Color.parseColor("#00cccc"));
        colors.add(Color.parseColor("#0066cc"));
        pieDataSet.setColors(colors);

        PieData pieData=new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
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
//
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
}
