package org.intelehealth.app.activities.aboutus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.MostSearchedVideosAdapter_New;

/**
 * Created by: Prajwal Waingankar On: 25/Nov/2022
 * Github: prajwalmw
 */
public class AboutUsActivity extends AppCompatActivity {
    private RecyclerView images_recyclerview;
    private AboutUsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
    }

    private void initUI() {
        images_recyclerview = findViewById(R.id.images_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        images_recyclerview.setLayoutManager(layoutManager);
        images_recyclerview.addItemDecoration(new CirclePagerIndicatorDecoration(this));
        adapter = new AboutUsAdapter(this);
        images_recyclerview.setAdapter(adapter);

//        MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity());
//        rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);
    }
}