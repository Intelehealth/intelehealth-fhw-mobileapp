package org.intelehealth.ezazi.ui.visit.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.databinding.ActivityVisitLabourEzaziBinding;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.ui.shared.BaseActivity;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;

/**
 * Created by Vaghela Mithun R. on 22-08-2023 - 10:23.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class VisitLabourActivity extends BaseActivity {
    private ActivityVisitLabourEzaziBinding binding;
    public static final String ARG_VISIT_ID = "visitId";
    public static final String ARG_HAS_MOTHER_DECEASED = "has_mother_deceased";

    public static void startLabourCompleteActivity(Context context, String visitId, boolean hasMotherDeceased) {
        Intent intent = new Intent(context, VisitLabourActivity.class);
        intent.putExtra(ARG_VISIT_ID, visitId);
        intent.putExtra(ARG_HAS_MOTHER_DECEASED, hasMotherDeceased);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisitLabourEzaziBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomSheetAppBar.toolbar.setTitle("Labour Complete");
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AppConstants.SYNC_INTENT_ACTION);
        registerReceiver(syncBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncBroadcastReceiver);
    }

    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD("syncBroadcastReceiver", "onReceive! " + intent);

            if (intent != null && intent.hasExtra(AppConstants.SYNC_INTENT_DATA_KEY)) {
//                int flagType = intent.getIntExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED);
                startHomeActivity();
            }
        }
    };

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void checkInternetAndUploadVisitEncounter() {
        if (NetworkConnection.isOnline(getApplication())) {
            SyncUtils syncUtils = new SyncUtils();
            syncUtils.syncForeground("timeline");
//            Toast.makeText(this, getResources().getString(R.string.syncInProgress), Toast.LENGTH_LONG).show();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //   Added the 4 sec delay and then push data.For some reason doing immediately does not work
//                    //Do something after 100ms
//                    SyncUtils syncUtils = new SyncUtils();
//                    syncUtils.syncForeground("timeline");
//                }
//            }, 4000);
        } else {
            Toast.makeText(this, getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
        }
    }
}
