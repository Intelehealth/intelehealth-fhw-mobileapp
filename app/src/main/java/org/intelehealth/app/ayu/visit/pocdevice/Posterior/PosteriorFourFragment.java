package org.intelehealth.app.ayu.visit.pocdevice.Posterior;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.ble.constants.DeviceConnectionState;
import com.ayudevice.ayusynksdk.playback.listener.RecorderListener;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.EncounterManager;
import org.intelehealth.app.ayu.visit.pocdevice.UploadManager;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentPosteriorFourBinding;
import org.intelehealth.app.databinding.FragmentPosteriorThreeBinding;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class PosteriorFourFragment extends Fragment implements RecorderListener {

    private FragmentPosteriorFourBinding mBinding;
    private static final int AUDIO_PERMISSION_REQUEST = 101;
    private String lastRecordedFilePath = "";
    long safeTime;
    EncounterManager manager;
    String filePath;
    InteleHealthDatabaseHelper db;
    private static final String ARG_PATIENT_UUID = "patientUuid";

    private static  final String ARG_PATINT_NAME = "patientName";
    private static final String ARG_VISIT_UUID = "visitUuid";
    private static final String ARG_PATIENT_NAME = "patientName";
    private static final String ARG_INTENT_TAG = "intentTag";
    private static final String ARG_AGE = "float_ageYear_Month";
    private static final String ARG_TYPE = "type";
    private static final String ENCOUNTER_UUID = "encounterUuid";

    static SessionManager sessionManager;

    String patientUuid,patientName, visitUuid, encounterUuid, intentTag, type;
    public static final String KEY_RECORDING_STATUS = "recordingStatus";

    int recordingStatus;
    UploadManager uploadManager;
    float float_ageYear_Month;


    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;

    private final int MAX_DURATION = 10000; // 10 seconds
    static String trackerId;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

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
    private void stopManually() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    public static PosteriorFourFragment newInstance(boolean isEditMode,
                                                     String patientName,
                                                     String patientUuid,
                                                     String visitUuid,
                                                     String encounterUuid,
                                                     String intentTag,
                                                     float float_ageYear_Month,
                                                     String type) {

        PosteriorFourFragment posteriorFourFragment = new PosteriorFourFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEditMode", isEditMode);

        args.putString(ARG_PATIENT_UUID, patientUuid);
        args.putString(ARG_PATINT_NAME,patientName);
        args.putString(ARG_VISIT_UUID, visitUuid);
        args.putString(ENCOUNTER_UUID, encounterUuid);
        args.putString(ARG_INTENT_TAG, intentTag);
        args.putFloat(ARG_AGE, float_ageYear_Month);
        args.putString(ARG_TYPE, type); // Heart / Lung

        posteriorFourFragment.setArguments(args);
        return posteriorFourFragment;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_posterior_four, container, false);
        return mBinding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkAudioPermission();
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        db = new InteleHealthDatabaseHelper(getContext());
        manager = new EncounterManager();
        uploadManager = new UploadManager();

        if (getArguments() != null) {
            patientUuid = getArguments().getString(ARG_PATIENT_UUID);
            patientName = getArguments().getString(ARG_PATINT_NAME);
            visitUuid = getArguments().getString(ARG_VISIT_UUID);
            encounterUuid = getArguments().getString(ENCOUNTER_UUID);
            intentTag = getArguments().getString(ARG_INTENT_TAG);
            float_ageYear_Month = getArguments().getFloat(ARG_AGE);
            type = getArguments().getString(ARG_TYPE);
        }

        CustomLog.v("RecordHeartSoundsFragment", "Patient ID: " + patientUuid);
        CustomLog.v("RecordHeartSoundsFragment", "Visit ID: " + visitUuid);
        CustomLog.v("RecordHeartSoundsFragment", "ENcounter UUID: " + encounterUuid);
        CustomLog.v("RecordHeartSoundsFragment", "Intent Tag: " + intentTag);
        CustomLog.v("RecordHeartSoundsFragment", "Age: " + float_ageYear_Month);


        mBinding.btnStartRecording.setOnClickListener(v -> {
            if (AyuSynk.getBleInstance().isDeviceConnected() != DeviceConnectionState.DEVICE_CONNECTED) {
                Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            AyuSynk.getBleInstance().startRecording();
            AyuSynk.getBleInstance().setRecorderListener(this);
            AyuSynk.getBleInstance().setAyuVisualizerView(mBinding.waveView);
            startTime = System.currentTimeMillis();
            timerHandler.post(timerRunnable);
            mBinding.llButtonStart.setVisibility(GONE);
            mBinding.llButtonStop.setVisibility(VISIBLE);
        });

        mBinding.btnStopRecording.setOnClickListener(v -> {
            stopManually();
            mBinding.llButtonStop.setVisibility(View.GONE);
            mBinding.llButtonStart.setVisibility(View.VISIBLE);
        });
        mBinding.btnPause.setOnClickListener(v -> {
            AyuSynk.getBleInstance().pauseRecording();
        });
        mBinding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.btnPause.setVisibility(VISIBLE);
                // String path = getLastAudioPath();
                // playAudio(path);
            }
        });
        mBinding.btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.btnPlay.setVisibility(VISIBLE);
            }
        });

        mBinding.btnSaveRecordingMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db = new InteleHealthDatabaseHelper(getContext());
                // ✅ insert sample row
                db.insertRecord(
                        patientUuid,
                        visitUuid,
                        encounterUuid,
                        "lung",
                        "posteriorfour",
                        recordingStatus,
                        filePath,
                        "Normal"
                );
            }
        });
        mBinding.btnReCord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

    }

    @Override
    public void elapsedTime(long currentTimeMs, long totalTimeMs) {
        System.out.println("currentTimeMs" + currentTimeMs + " " + totalTimeMs);
        safeTime = currentTimeMs;
        if (!isAdded() || getActivity() == null || mBinding == null) {
            safeTime = currentTimeMs;
        }
    }

    @Override
    public void recordingComplete(int status) {
        Log.d("Ayu", "recordingComplete = " + status);
        getActivity().runOnUiThread(() -> {
            short[] audioShorts = AyuSynk.getBleInstance().getAudioData(status);
            recordingStatus = status;
            if (audioShorts != null && audioShorts.length > 0) {
                byte[] audioBytes = shortToByte(audioShorts);
                lastRecordedFilePath = saveToFile(audioBytes);
                filePath = lastRecordedFilePath;
                if (!isAdded() || getActivity() == null || mBinding == null) return;
                Toast.makeText(getContext(),
                        "Recording saved:\n" + lastRecordedFilePath,
                        Toast.LENGTH_LONG).show();
                AyuSynk.getBleInstance().setAyuVisualizerView(null);
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



    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION_REQUEST);
        }
    }

    // short[] → byte[]
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