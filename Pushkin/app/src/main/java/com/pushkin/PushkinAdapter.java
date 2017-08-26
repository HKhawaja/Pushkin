package com.pushkin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pushkin.other.CircleTransform;
import com.pushkin.other.ListViewClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Harith on 04/28/17.
 */

public class PushkinAdapter extends ArrayAdapter<ConversationPreview> {

    private ArrayList<ConversationPreview> previews;

    public PushkinAdapter(Context context, int textViewResourceId, ArrayList<ConversationPreview> previews) {
        super(context, textViewResourceId, previews);
        this.previews = previews;

    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

        ConversationPreview item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.conversation_preview, parent, false);
        }

        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.layout);
        layout.setOnClickListener(new ListViewClickListener(item.getChatID()));

        ImageView pic = (ImageView) convertView.findViewById(R.id.pic);

        String imageBytes = item.getImageURL();
        //            byte[] imageByteArray = Base64.decode(imageBytes, Base64.DEFAULT);

        //            Glide.with(getContext()).load(imageByteArray).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).override(300, 200)
        //                    .into(pic);
        //                .crossFade()
        //                .thumbnail(0.5f)
        //                .bitmapTransform(new CircleTransform(getContext()))

        Glide.with(getContext()).load(imageBytes).crossFade().thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(getContext()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(pic);


        TextView message = (TextView) convertView.findViewById(R.id.message);
        String msg = item.getName();
        if(msg.startsWith("FFD8"))
        {
            message.setText("Attachment: 1 Image");
        }
        else{
            message.setText(item.getName());
        }

        TextView lastActive = (TextView) convertView.findViewById(R.id.lastActive);

        //Date date = new Date(Long.parseLong(item.getLastActive())*1000L); // *1000 is to convert seconds to milliseconds
        //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a"); // the format of your date
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
        //String formattedDate = sdf.format(date);
        lastActive.setText(item.getLastActive());

        TextView tpoints = (TextView) convertView.findViewById(R.id.tpoints);
        tpoints.setText(item.getLastMessage());

        return convertView;
    }
}
