package org.intelehealth.app.ayu.visit.pocdevice;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import org.intelehealth.app.ayu.EncounterManager;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentAorticBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


public class SoundFragment extends DialogFragment implements RecorderListener {

    private FragmentAorticBinding binding;

    private String patientUuid, visitUuid, encounterUuid;
    private String type, position;
    private ArrayList<String> sounds;
    private String currentSound;

    private String filePath = "";
    private int recordingStatus;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private final int MAX_DURATION = 10000;

    private InteleHealthDatabaseHelper db;
    private EncounterManager manager;
    long safeTime;
    private Runnable timerRunnable;
    private OnSoundSavedListener listener;
    private int currentIndex = 0;

    public interface OnSoundSavedListener {
        void onSoundSaved();
    }


    public static SoundFragment newInstance(Bundle args) {
        SoundFragment fragment = new SoundFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnSoundSavedListener) {
            listener = (OnSoundSavedListener) getParentFragment();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_aortic, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        db = new InteleHealthDatabaseHelper(getContext());
        manager = new EncounterManager();
        // uploadManager = new UploadManager();


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
            return;
        }
        currentIndex = 0;
        currentSound = sounds.get(currentIndex);
        position = currentSound;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;

                binding.txtTimer.setText((elapsed / 1000f) + " / 10.0");

                if (elapsed < MAX_DURATION) {
                    timerHandler.postDelayed(this, 100); // ✅ use "this"
                } else {
                    stopRecording();
                }
            }
        };
        setupUI();
    }

    private void setupUI() {

        binding.btnStartRecording.setOnClickListener(v -> startRecording());

        binding.btnStopRecording.setOnClickListener(v -> stopRecording());

        binding.btnSaveRecordingMain.setOnClickListener(v -> saveAndUpload());

        binding.btnPlay.setOnClickListener(v -> playAudio(filePath));
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

    @Override
    public void elapsedTime(long currentTimeMs, long totalTimeMs) {
        System.out.println("currentTimeMs" + currentTimeMs + " " + totalTimeMs);
        safeTime = currentTimeMs;
        if (!isAdded() || getActivity() == null || binding == null) {
            safeTime = currentTimeMs;
        }
    }

    @Override
    public void recordingComplete(int status) {

        short[] audioShorts = AyuSynk.getBleInstance().getAudioData(status);
        recordingStatus = status;

        if (audioShorts != null) {
            byte[] bytes = shortToByte(audioShorts);
            filePath = saveToFile(bytes);

            Toast.makeText(getContext(), "Saved: " + filePath, Toast.LENGTH_SHORT).show();

            binding.llButtonStop.setVisibility(View.GONE);
            binding.llButtonSaveRecordingMain.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void playingComplete() {
        if (!isAdded() || getActivity() == null) return;
        getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(),
                        "Playback completed",
                        Toast.LENGTH_SHORT).show());
    }

    private void saveAndUpload() {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(getContext(), "Please record first", Toast.LENGTH_SHORT).show();
            return;
        }
        // ✅ Save in DB
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
        Log.d("SOUND_FLOW", "Index: " + currentIndex + " / " + sounds.size());
        Log.d("SOUND_FLOW", "Current: " + currentSound);


        Toast.makeText(getContext(), "Saved: " + position, Toast.LENGTH_SHORT).show();

        // 🔥 MOVE TO NEXT
        currentIndex++;

        if (currentIndex < sounds.size()) {
            currentSound = sounds.get(currentIndex);
            position = currentSound;
            binding.llButtonSaveRecordingMain.setVisibility(View.GONE);
            binding.llButtonStart.setVisibility(View.VISIBLE);
            binding.llButtonStop.setVisibility(View.GONE);
            // ✅ ALL COMPLETED
          /*  Toast.makeText(getContext(), "All sounds recorded", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().setFragmentResult("sound_done", new Bundle());
            // 🔥 GO BACK TO PhysicalExam
            getParentFragmentManager().popBackStack(
                    "poc_device",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);*/
            if (getTargetFragment() instanceof OnSoundSavedListener) {
                ((OnSoundSavedListener) getTargetFragment()).onSoundSaved();
            }
            requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {

        }
    }

    private void afterUpload(String url) {
        String enc = db.getEncounter(visitUuid);

        if (enc == null) {
            manager.createEncounter(patientUuid, visitUuid, uuid -> {
                db.saveEncounter(visitUuid, uuid);
                manager.sendAudioObs(uuid, url);
            });
        } else {
            manager.sendAudioObs(enc, url);
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

    private void playAudio(String path) {
        // same as your AorticFragment (reuse)
    }

}