package org.intelehealth.unicef.activities.activitySync;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;


public class ActivitySync extends LocalConfigActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

}
