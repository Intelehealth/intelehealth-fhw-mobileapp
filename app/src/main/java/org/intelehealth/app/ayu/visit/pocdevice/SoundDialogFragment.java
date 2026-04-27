package org.intelehealth.app.ayu.visit.pocdevice;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.ble.constants.DeviceConnectionState;
import com.ayudevice.ayusynksdk.playback.listener.RecorderListener;

import org.intelehealth.app.R;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentAorticBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class SoundDialogFragment extends DialogFragment implements RecorderListener {

    private FragmentAorticBinding binding;

    private String patientUuid, visitUuid, encounterUuid;
    private String type, position;

    private ArrayList<String> sounds;
    private int currentIndex = 0;
    private String currentSound;

    private String filePath = "";
    private int recordingStatus;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private final int MAX_DURATION = 10000;

    private InteleHealthDatabaseHelper db;

    // ✅ CALLBACK
    public interface OnAllSoundsCompleted {
        void onCompleted();
    }

    private OnAllSoundsCompleted listener;

    public void setListener(OnAllSoundsCompleted listener) {
        this.listener = listener;
    }

    // ✅ CREATE INSTANCE
    public static SoundDialogFragment newInstance(
            String type,
            ArrayList<String> sounds,
            String patientUuid,
            String visitUuid,
            String encounterUuid) {

        SoundDialogFragment fragment = new SoundDialogFragment();

        Bundle args = new Bundle();
        args.putString("type", type);
        args.putStringArrayList("sounds", sounds);
        args.putString("patientUuid", patientUuid);
        args.putString("visitUuid", visitUuid);
        args.putString("encounterUuid", encounterUuid);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_aortic, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        db = new InteleHealthDatabaseHelper(getContext());

        Bundle args = getArguments();
        if (args != null) {
            sounds = args.getStringArrayList("sounds");
            type = args.getString("type");
            patientUuid = args.getString("patientUuid");
            visitUuid = args.getString("visitUuid");
            encounterUuid = args.getString("encounterUuid");
        }

        if (sounds == null || sounds.isEmpty()) {
            Toast.makeText(getContext(), "No sounds found", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // ✅ FIRST SOUND
        currentIndex = 0;
        currentSound = sounds.get(currentIndex);
        position = currentSound;

        //binding.txtSoundName.setText(currentSound);

        setupUI();
    }

    private void setupUI() {

        binding.btnStartRecording.setOnClickListener(v -> startRecording());
        binding.btnStopRecording.setOnClickListener(v -> stopRecording());
        binding.btnSaveRecordingMain.setOnClickListener(v -> saveAndNext());
    }

    private void startRecording() {
        if (AyuSynk.getBleInstance().isDeviceConnected()
                != DeviceConnectionState.DEVICE_CONNECTED) {

            Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        AyuSynk.getBleInstance().startRecording();
        AyuSynk.getBleInstance().setRecorderListener(this);
        AyuSynk.getBleInstance().setAyuVisualizerView(binding.waveView);

        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);

        binding.llButtonStart.setVisibility(View.GONE);
        binding.llButtonStop.setVisibility(View.VISIBLE);
    }

    private void stopRecording() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;

            binding.txtTimer.setText((elapsed / 1000f) + " / 10.0");

            if (elapsed < MAX_DURATION) {
                timerHandler.postDelayed(this, 100);
            } else {
                stopRecording();
            }
        }
    };

    @Override
    public void recordingComplete(int status) {

        short[] audioShorts = AyuSynk.getBleInstance().getAudioData(status);
        recordingStatus = status;

        if (audioShorts != null) {

            byte[] bytes = shortToByte(audioShorts);
            filePath = saveToFile(bytes);

            Toast.makeText(getContext(), "Saved: " + position, Toast.LENGTH_SHORT).show();

            binding.llButtonStop.setVisibility(View.GONE);
            binding.llButtonSaveRecordingMain.setVisibility(View.VISIBLE);
        }
    }

    private void saveAndNext() {

        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(getContext(), "Please record first", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ SAVE DB
        db.insertRecord(
                patientUuid,
                visitUuid,
                encounterUuid,
                type,
                position,
                recordingStatus,
                filePath,
                "Pending"
        );

        currentIndex++;

        if (currentIndex < sounds.size()) {

            // 👉 NEXT SOUND
            currentSound = sounds.get(currentIndex);
            position = currentSound;

           // binding.txtSoundName.setText(currentSound);

            binding.llButtonSaveRecordingMain.setVisibility(View.GONE);
            binding.llButtonStart.setVisibility(View.VISIBLE);
            binding.llButtonStop.setVisibility(View.GONE);

            filePath = "";

        } else {

            // ✅ ALL DONE
            Toast.makeText(getContext(), "All sounds recorded", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onCompleted();
            }

            dismiss(); // 🔥 CLOSE POPUP
        }
    }

    private String saveToFile(byte[] bytes) {
        try {
            File file = new File(requireContext().getExternalFilesDir(null),
                    position + "_" + System.currentTimeMillis() + ".pcm");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            return "";
        }
    }

    private byte[] shortToByte(short[] shorts) {
        ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (short s : shorts) buffer.putShort(s);

        return buffer.array();
    }

    @Override
    public void elapsedTime(long currentTimeMs, long totalTimeMs) {}

    @Override
    public void playingComplete() {}
}
