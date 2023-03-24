package org.intelehealth.app.activities.aboutus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.MostSearchedVideosAdapter_New;
import org.intelehealth.app.syncModule.SyncUtils;

/**
 * Created by: Prajwal Waingankar On: 25/Nov/2022
 * Github: prajwalmw
 */
public class AboutUsActivity extends AppCompatActivity {
    private RecyclerView images_recyclerview;
    private AboutUsAdapter adapter;
    private TextView globe_link, info_link;
    private ImageView ivRefresh;
    private ObjectAnimator syncAnimator;
    private Button gotoButton;

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
        globe_link = findViewById(R.id.globe_link);
        globe_link.setAutoLinkMask(Linkify.ALL);    // When you want to show directly the Link to the user as text.

        info_link = findViewById(R.id.info_link);
        info_link.setMovementMethod(LinkMovementMethod.getInstance());  // When you need to show custom text rather than link to user.

        ivRefresh = findViewById(R.id.refresh);
        ivRefresh.setOnClickListener(v -> {
            SyncUtils.syncNow(AboutUsActivity.this, ivRefresh, syncAnimator);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        images_recyclerview.setLayoutManager(layoutManager);
        images_recyclerview.addItemDecoration(new CirclePagerIndicatorDecoration(this));
        adapter = new AboutUsAdapter(this);
        images_recyclerview.setAdapter(adapter);

//        MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity());
//        rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

        gotoButton = findViewById(R.id.goto_btn);
        gotoButton.setOnClickListener(v -> {
            String url = "https://intelehealth.org/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                intent.setPackage(null);
                startActivity(intent);
            }
        });
    }

    public void backPress(View view) {
        finish();
    }
}