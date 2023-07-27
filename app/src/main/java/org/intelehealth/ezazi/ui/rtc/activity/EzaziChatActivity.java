package org.intelehealth.ezazi.ui.rtc.activity;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.intelehealth.ezazi.R;
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity;

/**
 * Created by Vaghela Mithun R. on 24-05-2023 - 18:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class EzaziChatActivity extends ChatActivity {
    @Override
    protected int getContentResourceId() {
        return R.layout.activity_chat_ezazi;
    }

    @Override
    protected void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        super.setupActionBar();
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    protected Intent getVideoIntent() {
        return new Intent(this, EzaziVideoCallActivity.class);
    }

    @Override
    protected void initiateView() {
        mEmptyTextView = findViewById(R.id.empty_tv);
        mMessageEditText = findViewById(R.id.etMessageInput);
        mLoadingLinearLayout = findViewById(R.id.loading_layout);
        mEmptyLinearLayout = findViewById(R.id.empty_view);
        mRecyclerView = findViewById(R.id.rvConversation);
        mLayoutManager = new LinearLayoutManager(EzaziChatActivity.this, LinearLayoutManager.VERTICAL, true);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }
}
