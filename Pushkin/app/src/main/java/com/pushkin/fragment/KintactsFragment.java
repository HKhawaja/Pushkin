package com.pushkin.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pushkin.Kintact;
import com.pushkin.PushkinDatabaseHelper;
import com.pushkin.R;
import com.pushkin.activity.MainActivity;
import com.pushkin.other.CircleTransform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KintactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KintactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KintactsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static Context context;
    private CustomAdapter<Kintact> mAdapter;
    private ArrayList<String> data=new ArrayList<String>();
    private ListView listView;
    private EditText editText;
    private View something;
    public static PushkinDatabaseHelper databaseHelper;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public KintactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KintactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KintactsFragment newInstance(String param1, String param2) {
        KintactsFragment fragment = new KintactsFragment();
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
        // Inflate the layout for this fragment
        something = inflater.inflate(R.layout.fragment_kintacts, container, false);
        if(context == null){
            context = getActivity();
        }
        databaseHelper = MainActivity.dbHelper;

        updateAdapter();

        editText=(EditText)something.findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                KintactsFragment.this.mAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
        return something;
    }

    public void updateAdapter(){
        //Read the file with our data
        ArrayList<Kintact> kintacts = databaseHelper.getKintacts();
        System.out.println(kintacts.size());

        Collections.sort(kintacts, new Comparator<Kintact>() {
            @Override
            public int compare(Kintact lhs, Kintact rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if (lhs.getfName().compareTo(rhs.getfName()) > 0)
                    return 1;
                else if (lhs.getfName().compareTo(rhs.getfName()) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        //Create the adapter with this new info
        mAdapter = new CustomAdapter<Kintact>(context, R.layout.activity_contacts_listview, R.id.contactsList, kintacts);
        listView = (ListView)something.findViewById(R.id.contactsList);
        listView.setAdapter(mAdapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setTextFilterEnabled(true);
        //listView.setFriction(ViewConfiguration.getScrollFriction() * (float)2);

        //Set the view of this adapter to the bottom
        listView.setSelection(listView.getCount() - 1);
    }

    private class CustomAdapter<E> extends ArrayAdapter<Kintact> {

        public CustomAdapter(Context context, int resource, int id, ArrayList<Kintact> data) {
            super(context, resource, id, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Kintact s = getItem(position);
            System.out.println("The thing is [" + s + "]");

            System.out.println("Inflating view to [to]");
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_contacts_listview, parent, false);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.profile_image);
            TextView nickname = (TextView) convertView.findViewById(R.id.nickname);
            TextView username = (TextView) convertView.findViewById(R.id.username);
            // Populate the data into the template view using the data object
            nickname.setText(s.getfName());
            nickname.setTextColor(Color.BLACK);

            username.setText(s.getUsername());

            Glide.with(getContext()).load(s.getImage())
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(getContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);

            // Return the completed view to render on screen
            return convertView;
        }
    }

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
