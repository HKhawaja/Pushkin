package com.pushkin.other;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.pushkin.Chat;
import com.pushkin.PushkinDatabaseHelper;
import com.pushkin.activity.MainActivity;
import com.pushkin.fragment.ConversationsFragment;

import java.util.ArrayList;

/**
 * Created by Harith on 05/05/17.
 */

public class ListViewClickListener implements View.OnClickListener {

    private int chatID;

    public ListViewClickListener (int id) {
        super();
        chatID = id;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), Chat.class);
        intent.putExtra("CHAT_ID", chatID);
        v.getContext().startActivity(intent);
    }
}
