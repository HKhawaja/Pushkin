package com.pushkin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pushkin.activity.MainActivity;
import com.pushkin.other.CircleTransform;
import com.pushkin.other.ListViewClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harith on 04/28/17.
 */

public class KintactAdapter extends ArrayAdapter<Kintact> {

    private ArrayList<Kintact> previews;

    public KintactAdapter(Context context, int textViewResourceId, ArrayList<Kintact> previews) {
        super(context, textViewResourceId, previews);
        this.previews = previews;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

        PushkinDatabaseHelper dbHelper = MainActivity.dbHelper;

        Kintact item = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_kintact_adapter, parent, false);

        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.kin);
//        if (dbHelper.getChatID(item.getUsername()) == -1)
//            dbHelper.addConversationKintact(item.getUsername());

        layout.setOnClickListener(new ListViewClickListener(dbHelper.getChatID(item.getUsername())));

        ImageView pic = (ImageView) convertView.findViewById(R.id.pic);
        Glide.with(getContext()).load(item.getImage())
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(getContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(pic);

        //username
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText(item.getfName() + " " + item.getlName());


        return convertView;
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}