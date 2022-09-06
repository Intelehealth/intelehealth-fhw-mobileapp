package org.intelehealth.app.ui2.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import org.intelehealth.app.R;
import org.intelehealth.app.ui2.fragments.HomeFragment;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import me.piruin.quickaction.QuickIntentAction;

public class HomeScreenActivityNew extends AppCompatActivity {
    private static final String TAG = "HomeScreenActivity";
    ImageView imageViewIsInternet;
    private boolean isConnected = false;
    private static final int ID_DOWN = 2;
    private QuickAction quickAction;
    private QuickAction quickIntent;
    private DrawerLayout mDrawer;
    NavigationView navView;
    SessionManager sessionManager;
    Dialog dialogLoginSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_ui2);

        sessionManager = new SessionManager(this);

        Log.d(TAG, "onCreate: selected chw : " + sessionManager.getChwname());

        mDrawer = findViewById(R.id.drawer_layout);
        TextView tvLocation = findViewById(R.id.tv_user_location_home);
        tvLocation.setText(sessionManager.getLocationName());
        //navView = findViewById(R.id.navigationview);

        imageViewIsInternet = findViewById(R.id.imageview_is_internet);
        ImageView ivHamburger = findViewById(R.id.iv_hamburger);
        ivHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navView.setFitsSystemWindows(false);
                //  mDrawer.setFitsSystemWindows(true);
                mDrawer.openDrawer(Gravity.LEFT);

            }
        });
        isNetworkAvailable(this);

        imageViewIsInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction.show(v);
            }
        });

    }

    private void checkForInternet() {
        boolean result = NetworkConnection.isOnline(this);
        Log.d(TAG, "checkForInternet: result : " + result);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void isNetworkAvailable(Context context) {
        int flag = 0;

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            if (imageViewIsInternet != null) {
                                imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
                                flag = 1;
                                setTooltipForInternet("Good internet.\nRefresh");

                            }
                        }
                    }
                }
            }
        }

        if (flag == 0) {
            if (imageViewIsInternet != null) {
                imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

                setTooltipForInternet("No internet");
            }

        }

    }

    private void setTooltipForInternet(String message) {
        QuickAction.setDefaultColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        QuickAction.setDefaultTextColor(Color.BLACK);

        ActionItem nextItem = new ActionItem(ID_DOWN, message);
        quickAction = new QuickAction(this, QuickAction.HORIZONTAL);
        quickAction.setColorRes(R.color.white);
        quickAction.setTextColorRes(R.color.textColorBlack);
        quickAction.addActionItem(nextItem);
        quickAction.setTextColor(Color.BLACK);


        //Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(ActionItem item) {
                //here we can filter which action item was clicked with pos or actionId parameter
                String title = item.getTitle();
                Toast.makeText(HomeScreenActivityNew.this, title + " selected", Toast.LENGTH_SHORT).show();
                if (!item.isSticky()) quickAction.remove(item);
            }
        });

        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Toast.makeText(HomeScreenActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        quickIntent = new QuickIntentAction(this)
                .setActivityIntent(sendIntent)
                .create();
        quickIntent.setAnimStyle(QuickAction.Animation.REFLECT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = new HomeFragment();

        loadFragment(fragment);

        showLoggingInDialog();
    }

    private void loadFragment(Fragment fragment) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showLoggingInDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(HomeScreenActivityNew.this);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(HomeScreenActivityNew.this);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_login_success, null);
        builder.setView(customLayout);

        dialogLoginSuccess = builder.create();
        dialogLoginSuccess.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialogLoginSuccess.show();
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        dialogLoginSuccess.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogLoginSuccess.dismiss();
            }
        }, 2000);
    }

}

