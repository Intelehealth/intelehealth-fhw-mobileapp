package org.intelehealth.ezazi.ui.shared;

import static org.intelehealth.ezazi.app.AppConstants.SHIFTED_DATA;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.ShiftChangeData;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.databinding.DialogShiftedPatientsBinding;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.dialog.adapter.ShiftedPatientAdapter;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziChatActivity;
import org.intelehealth.ezazi.utilities.AppNotification;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

import java.util.Objects;

/**
 * Created by Vaghela Mithun R. on 03-06-2023 - 19:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class BaseActivity extends AppCompatActivity implements SocketManager.NotificationListener {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketManager.getInstance().setNotificationListener(this);
        showShiftedPatientDialog(getIntent());
    }

    @Override
    public void showNotification(@NonNull ChatMessage chatMessage) {
        RtcArgs args = new RtcArgs();
        args.setPatientName(chatMessage.getPatientName());
        args.setPatientId(chatMessage.getPatientId());
        args.setVisitId(chatMessage.getVisitId());
        args.setNurseId(chatMessage.getToUser());
        args.setDoctorUuid(chatMessage.getFromUser());
        Log.e(TAG, "showNotification: " + args.toJson());
        try {
            String title = new ProviderDAO().getProviderName(args.getDoctorUuid());
            new AppNotification.Builder(this)
                    .title(title)
                    .body(chatMessage.getMessage())
                    .pendingIntent(EzaziChatActivity.getPendingIntent(this, args))
                    .send();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showShiftedPatientDialog(intent);
    }

    private void showShiftedPatientDialog(Intent intent) {
        if (intent == null || intent.getExtras() == null) return;
        if (intent.hasExtra(SHIFTED_DATA)) {
            ShiftChangeData data = getShiftedData(intent);
            if (data == null) return;

            DialogShiftedPatientsBinding binding = DialogShiftedPatientsBinding.inflate(getLayoutInflater(), null, false);
            binding.setNurse(data.getAssignorNurse());
            binding.rvShiftedPatientList.setLayoutManager(new LinearLayoutManager(this));
            binding.rvShiftedPatientList.setAdapter(new ShiftedPatientAdapter(BaseActivity.this, data.buildPatients()));
            CustomViewDialogFragment dialogFragment = new CustomViewDialogFragment.Builder(this)
                    .view(binding.getRoot())
                    .positiveButtonLabel(R.string.okay)
                    .hideNegativeButton(true)
                    .build();
            dialogFragment.setWrapContentDialog(true);
            dialogFragment.setListener(() -> new SyncDAO().pullData_Background(BaseActivity.this));
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getName());
        }
    }

    private ShiftChangeData getShiftedData(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra(SHIFTED_DATA, ShiftChangeData.class);
        } else {
            return (ShiftChangeData) intent.getSerializableExtra(SHIFTED_DATA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(shiftedPatientReceiver, new IntentFilter(AppConstants.getShiftedPatientReceiver()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(shiftedPatientReceiver);
    }

    private final BroadcastReceiver shiftedPatientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showShiftedPatientDialog(intent);
        }
    };
}
