package org.intelehealth.app.ayu.visit.pocdevice;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ayudevices.cardiosynksdk.AyuDevice;
import com.ayudevices.cardiosynksdk.ble.constants.DeviceConnectionState;
import com.ayudevices.cardiosynksdk.playback.listener.RecorderListener;

import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentAorticBinding; // You can reuse or create a new layout

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DigitalStethoscopeDialogFragment extends DialogFragment implements RecorderListener {

    public interface OnDismissListener {
        void onDismissed();
    }

    private OnDismissListener onDismissListener;

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }
    InteleHealthDatabaseHelper db;
    private FragmentAorticBinding mBinding;
    private String examType;
    long safeTime;
    private String lastRecordedFilePath = "";
    String patientUuid,patientName, visitUuid, encounterUuid, intentTag, type;
    float float_ageYear_Month;
    int recordingStatus;
    String filePath;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;

    private final int MAX_DURATION = 10000; // 10 seconds
    private static final String ARG_EXAM_TYPE = "exam_type";
    private static final String ARG_PATIENT_UUID = "patientUuid";
    private static  final String ARG_PATINT_NAME = "patientName";
    private static final String ARG_VISIT_UUID = "visitUuid";
    private static final String ARG_ENCOUNTER_UUID = "encounterUuid";

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBinding == null) return;
            long elapsedMillis = System.currentTimeMillis() - startTime;
            float elapsedSec = elapsedMillis / 1000f;

            // Format like: 0.0 / 10.0 sec
            String text = String.format("%.1f / 10.0", elapsedSec);
            mBinding.txtTimer.setText(text);
            if (elapsedMillis < MAX_DURATION) {
                timerHandler.postDelayed(this, 100);
            } else {
                stopManually();
            }
        }
    };

    public static DigitalStethoscopeDialogFragment newInstance(String examType,String patientUuid,String visitUuid,String encounterUuid) {
        DigitalStethoscopeDialogFragment fragment = new DigitalStethoscopeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXAM_TYPE, examType);
        args.putString(ARG_PATIENT_UUID, patientUuid);
        args.putString(ARG_VISIT_UUID, visitUuid);
        args.putString(ARG_ENCOUNTER_UUID, encounterUuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set style for full screen or custom size if needed
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        if (getArguments() != null) {
            examType = getArguments().getString(ARG_EXAM_TYPE);
            patientUuid = getArguments().getString(ARG_PATIENT_UUID);
            visitUuid = getArguments().getString(ARG_VISIT_UUID);
            encounterUuid = getArguments().getString(ARG_ENCOUNTER_UUID);
        }
    }
    private void stopManually() {
        timerHandler.removeCallbacks(timerRunnable);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAorticBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new InteleHealthDatabaseHelper(getContext());
        // Set title based on exam type
        mBinding.txtTitle.setText("Recording: " + examType);
        // Handle Close/Back button
        mBinding.btnBack.setOnClickListener(v -> dismiss());
        setupRecordingLogic();
    }

    private void setupRecordingLogic() {
        mBinding.btnStartRecording.setOnClickListener(v -> {
            if (AyuDevice.getBleInstance().isDeviceConnected() != DeviceConnectionState.DEVICE_CONNECTED) {
                Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            AyuDevice.getBleInstance().startRecording();
            AyuDevice.getBleInstance().setRecorderListener(this);
            AyuDevice.getBleInstance().setAyuVisualizerView(mBinding.waveView);
            startTime = System.currentTimeMillis();
            timerHandler.post(timerRunnable);
            mBinding.llButtonStart.setVisibility(View.GONE);
            mBinding.llButtonStop.setVisibility(View.VISIBLE);
        });

        mBinding.btnStopRecording.setOnClickListener(v -> {
            // Stop recording logic
            mBinding.llButtonStop.setVisibility(View.GONE);
            mBinding.llButtonSaveRecordingMain.setVisibility(View.VISIBLE);
        });

        mBinding.btnSaveRecordingMain.setOnClickListener(v -> {
            // Save logic and then dismiss
            // notifyParentOfSuccess(filePath);

            String category = (examType != null && examType.toLowerCase().contains("lung")) ? "lung" : "heart";

            db = new InteleHealthDatabaseHelper(getContext());
            // ✅ insert sample row
            db.insertRecord(
                    patientUuid,
                    visitUuid,
                    encounterUuid,
                    category,
                    examType,
                    recordingStatus,
                    lastRecordedFilePath,
                    "Normal"
            );
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) onDismissListener.onDismissed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void elapsedTime(long currentTimeMs, long l1) {
        safeTime = currentTimeMs;
        if (!isAdded() || getActivity() == null || mBinding == null) {
            safeTime = currentTimeMs;
        }
    }

    @Override
    public void recordingComplete(int status) {
        Log.d("Ayu", "recordingComplete = " + status);
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            short[] audioShorts = AyuDevice.getBleInstance().getAudioData(status);
            this.recordingStatus = status;
            if (audioShorts != null && audioShorts.length > 0) {
                byte[] audioBytes = shortToByte(audioShorts);
                lastRecordedFilePath = saveToFile(audioBytes);
                if (!isAdded() || getActivity() == null || mBinding == null) return;
                AyuDevice.getBleInstance().setAyuVisualizerView(null);
                mBinding.llButtonStop.setVisibility(GONE);
                mBinding.llButtonStart.setVisibility(GONE);
                mBinding.llButtonSaveRecordingMain.setVisibility(VISIBLE);
            } else {
                Toast.makeText(getContext(),
                        "Recording failed",
                        Toast.LENGTH_SHORT).show();
            }


        });
    }

    @Override
    public void playingComplete() {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(),
                        "Playback completed",
                        Toast.LENGTH_SHORT).show());
    }

    private byte[] shortToByte(short[] shorts) {
        ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : shorts) buffer.putShort(s);
        return buffer.array();
    }

    // Save audio
    private String saveToFile(byte[] audioBytes) {
        try {
            File dir = new File(requireContext().getExternalFilesDir(null), "records");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "record_" + System.currentTimeMillis() + ".pcm";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(audioBytes);
            fos.flush();
            fos.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "File save error";
        }
    }

}