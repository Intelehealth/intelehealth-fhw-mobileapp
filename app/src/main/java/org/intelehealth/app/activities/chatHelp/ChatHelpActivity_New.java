package org.intelehealth.app.activities.chatHelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.ChatSupportAdapter_New;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;

public class ChatHelpActivity_New extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_help_new_ui2);
        ImageView ivIsInternet = findViewById(R.id.iv_is_internet);

        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        RecyclerView rvChatSupport = findViewById(R.id.rv_chatting);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChatSupport.setLayoutManager(layoutManager);
        ChatSupportAdapter_New chatSupportAdapter_new = new ChatSupportAdapter_New(this);
        rvChatSupport.setAdapter(chatSupportAdapter_new);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}