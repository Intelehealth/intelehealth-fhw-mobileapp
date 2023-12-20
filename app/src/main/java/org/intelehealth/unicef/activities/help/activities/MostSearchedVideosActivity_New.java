package org.intelehealth.unicef.activities.help.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;
import org.intelehealth.unicef.activities.help.adapter.HelpVideosAdapterVerticle_New;
import org.intelehealth.unicef.ui2.utils.CheckInternetAvailability;

public class MostSearchedVideosActivity_New extends LocalConfigActivity {
    private static final String TAG = "MostSearchedVideosActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_most_searched_videos_ui2);

        initUI();
    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_videos);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText(getResources().getString(R.string.videos));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        RecyclerView rvSearchedVideos = findViewById(R.id.rv_most_searched_videos);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvSearchedVideos.setLayoutManager(layoutManager);
        HelpVideosAdapterVerticle_New helpVideosAdapterVerticle_new = new HelpVideosAdapterVerticle_New(this);
        rvSearchedVideos.setAdapter(helpVideosAdapterVerticle_new);

        FloatingActionButton fabHelp = findViewById(R.id.fab_help_videos);
        fabHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MostSearchedVideosActivity_New.this, ChatSupportHelpActivity_New.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}