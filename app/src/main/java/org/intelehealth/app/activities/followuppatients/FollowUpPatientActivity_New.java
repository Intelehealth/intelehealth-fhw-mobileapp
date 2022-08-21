package org.intelehealth.app.activities.followuppatients;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import org.intelehealth.app.R;


/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class FollowUpPatientActivity_New extends AppCompatActivity {
    RecyclerView rv_today, rv_week, rv_month;
    FollowUpPatientAdapter_New adapter_new;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_up_visits);

        // changing status bar color
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        }

        rv_today = findViewById(R.id.recycler_today);
        rv_week = findViewById(R.id.rv_thisweek);
        rv_month = findViewById(R.id.rv_thismonth);

        adapter_new = new FollowUpPatientAdapter_New(this);
        rv_today.setAdapter(adapter_new);
        rv_week.setAdapter(adapter_new);
        rv_month.setAdapter(adapter_new);
    }
}