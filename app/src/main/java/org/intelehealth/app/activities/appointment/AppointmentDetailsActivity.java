package org.intelehealth.app.activities.appointment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;

public class AppointmentDetailsActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentDetailsActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details_ui2);

        initUI();

    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText(getResources().getString(R.string.appointment_details));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }


    }

}