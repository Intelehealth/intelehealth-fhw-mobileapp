package org.intelehealth.app.activities.homeActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Objects;

public class HomeFragment_New extends Fragment {
    private static final String TAG = "HomeFragment_New";
    View view;
    SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_ui2, container, false);
        return view;
    }

    private void initUI() {
        sessionManager = new SessionManager(getActivity());

        ImageView viewHamburger = Objects.requireNonNull(getActivity()).findViewById(R.id.iv_hamburger);
       viewHamburger.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_hamburger));
        TextView tvLocation = Objects.requireNonNull(getActivity()).findViewById(R.id.tv_user_location_home);
        tvLocation.setText(sessionManager.getLocationName());
        TextView tvLastSyncApp =  Objects.requireNonNull(getActivity()).findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification =  Objects.requireNonNull(getActivity()).findViewById(R.id.imageview_notifications_home);
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.VISIBLE);
        ivNotification.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: homefragen");
        initUI();

    }
}
