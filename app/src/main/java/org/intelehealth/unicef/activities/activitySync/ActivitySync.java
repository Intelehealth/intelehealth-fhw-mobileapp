package org.intelehealth.unicef.activities.activitySync;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.BaseActivity;


public class ActivitySync extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

}
