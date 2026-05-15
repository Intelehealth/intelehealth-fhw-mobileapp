package org.intelehealth.app.ayu.visit.pocdevice;

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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

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

public class SoundFragment extends Fragment implements RecorderListener {

    public static final String RESULT_SOUND_DONE = "sound_done";

    public interface OnSoundSavedListener {
        void onSoundSaved();
    }

    private FragmentAorticBinding binding;
    private InteleHealthDatabaseHelper db;

    private String patientUuid, visitUuid, encounterUuid;
    private String type, position;
    private ArrayList<String> sounds;

    private int currentIndex = 0;
    private String currentSound;
    private String filePath = "";
    private int recordingStatus;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;

    // FIX: 30 seconds — SDK needs ≥30s for heart BPM calculation
    // At 4000Hz: 30s = 120,000 samples → actual BPM returned
    private static final int MAX_DURATION_MS = 30000;

    private Runnable timerRunnable;

    public static SoundFragment newInstance(Bundle args) {
        SoundFragment fragment = new SoundFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_aortic, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new InteleHealthDatabaseHelper(getContext());

        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(), "Missing args", Toast.LENGTH_SHORT).show();
            popBack();
            return;
        }
        sounds        = args.getStringArrayList("sounds");
        type          = args.getString("type", "heart");
        patientUuid   = args.getString("patientUuid", "");
        visitUuid     = args.getString("visitUuid", "");
        encounterUuid = args.getString("encounterUuid", "");

        if (sounds == null || sounds.isEmpty()) {
            Toast.makeText(getContext(),
                    "No sound positions configured", Toast.LENGTH_SHORT).show();
            popBack();
            return;
        }

        currentIndex = 0;
        currentSound = sounds.get(currentIndex);
        position = currentSound;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding == null) return;
                long elapsed = System.currentTimeMillis() - startTime;

                // FIX: Display 30.0 max, update every 100ms
                binding.txtTimer.setText(String.format("%.1f / 30.0", elapsed / 1000f));

                if (elapsed < MAX_DURATION_MS) {
                    timerHandler.postDelayed(this, 100);
                } else {
                    // FIX: Timer reached 30s — show final value then stop SDK recording
                    binding.txtTimer.setText("30.0 / 30.0");
                    stopRecordingInternal();
                }
            }
        };

        binding.btnStartRecording.setEnabled(false);
        binding.btnBack.setOnClickListener(v -> popBack());

        getParentFragmentManager().setFragmentResultListener(
                AyuConnectDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    boolean connected = bundle.getBoolean(
                            AyuConnectDialogFragment.RESULT_CONNECTED, false);
                    if (connected) {
                        binding.btnStartRecording.setEnabled(true);
                        updatePositionUI(currentSound);
                        Toast.makeText(getContext(),
                                "Device connected. Record: " + currentSound,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        popBack();
                    }
                });

        if (savedInstanceState == null) {
            AyuConnectDialogFragment.newInstance()
                    .show(getParentFragmentManager(), "ayu_connect");
        }

        setupRecordingUi();
    }

    /**
     * Updates txtPosition text and imgdirection image based on the current sound position.
     * Called on connect (first position) and after every Save → next position.
     */
    private void updatePositionUI(String sound) {
        if (binding == null || sound == null) return;
        binding.txtPostion.setText(sound);
        binding.imgdirection.setImageResource(getImageForPosition(sound));

        // FIX: Clear waveform when switching to a new position
        // so the user sees a fresh/blank waveform before next recording
        try {
            AyuSynk.getBleInstance().setAyuVisualizerView(null);
            AyuSynk.getBleInstance().setAyuVisualizerView(binding.waveView);
            AyuSynk.getBleInstance().setAyuVisualizerView(null); // detach so it resets
        } catch (Exception ignored) {}
    }

    /**
     * Maps position name → drawable resource ID.
     * Heart: aortic_img, pulmanic, tricusiped, mital
     * Lung Anterior: anterior_one … anterior_six
     * Lung Lateral:  lateral_one … lateral_four
     * Lung Posterior: posterior_one_img … posterior_six_img
     */
    private int getImageForPosition(String sound) {
        if (sound == null) return R.drawable.aortic_img;
        switch (sound) {
            // ── Heart ─────────────────────────────────────────────
            case "Aortic":    return R.drawable.aortic_img;
            case "Pulmonic":  return R.drawable.pulmanic;
            case "Tricuspid": return R.drawable.tricusiped;
            case "Mitral":    return R.drawable.mital;

            // ── Lung Anterior ─────────────────────────────────────
            case "Anterior-1-Left-Top":    return R.drawable.anterior_one;
            case "Anterior-2-Right-Top":   return R.drawable.anterior_two;
            case "Anterior-3-Left-Middle": return R.drawable.anterior_three;
            case "Anterior-4-Right-Middle":return R.drawable.anterior_four;
            case "Anterior-5-Left-Lower":  return R.drawable.anterior_five;
            case "Anterior-6-Right-Lower": return R.drawable.anterior_six;

            // ── Lung Lateral ──────────────────────────────────────
            case "Lateral-1-Left-Top":    return R.drawable.lateral_one;
            case "Lateral-2-Left-Lower":  return R.drawable.lateral_two;
            case "Lateral-3-Right-Top":   return R.drawable.lateral_three;
            case "Lateral-4-Right-Lower": return R.drawable.lateral_four;

            // ── Lung Posterior ────────────────────────────────────
            case "Posterior-1-Left-Top":    return R.drawable.posterior_one_img;
            case "Posterior-2-Right-Top":   return R.drawable.posterior_two_img;
            case "Posterior-3-Left-Middle": return R.drawable.posterior_three_img;
            case "Posterior-4-Right-Middle":return R.drawable.posterior_four_img;
            case "Posterior-5-Left-Lower":  return R.drawable.posterior_five_img;
            case "Posterior-6-Right-Lower": return R.drawable.posterior_six_img;

            default: return R.drawable.aortic_img;
        }
    }

    private void setupRecordingUi() {
        binding.btnStartRecording.setOnClickListener(v -> startRecording());
        binding.btnStopRecording.setOnClickListener(v -> stopRecordingInternal());
        binding.btnSaveRecordingMain.setOnClickListener(v -> saveAndAdvance());
    }

    private void startRecording() {
        if (AyuSynk.getBleInstance().isDeviceConnected()
                != DeviceConnectionState.DEVICE_CONNECTED) {
            Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX: Attach waveview BEFORE starting recording so waveform shows for full 30s
        AyuSynk.getBleInstance().setAyuVisualizerView(binding.waveView);
        AyuSynk.getBleInstance().setRecorderListener(this);
        AyuSynk.getBleInstance().startRecording();

        startTime = System.currentTimeMillis();

        // Reset timer and start 30s countdown
        binding.txtTimer.setText("0.0 / 30.0");
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);

        binding.llButtonStart.setVisibility(View.GONE);
        binding.llButtonStop.setVisibility(View.VISIBLE);
    }

    private void stopRecordingInternal() {
        // FIX: Remove timer callbacks first
        timerHandler.removeCallbacks(timerRunnable);

        // FIX: Tell SDK to stop recording — this triggers recordingComplete() callback
        // Without this call the SDK keeps running internally and fires at its own
        // default duration (10s) causing the "save button appears at 10s" bug
        try {
            AyuSynk.getBleInstance().pauseRecording();
        } catch (Exception e) {
            Log.e("SOUND_FLOW", "stopRecording error: " + e.getMessage());
        }
    }

    @Override
    public void recordingComplete(int status) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            short[] audio = AyuSynk.getBleInstance().getAudioData(status);
            recordingStatus = status;

            if (audio != null && audio.length > 0) {
                byte[] bytes = shortToByte(audio);
                filePath = saveToFile(bytes);

                Log.d("SOUND_FLOW", "recordingComplete: samples=" + audio.length
                        + " | durationSec=" + (audio.length / 4000f)
                        + " | position=" + position);

                // FIX: Detach waveform after recording completes
                AyuSynk.getBleInstance().setAyuVisualizerView(null);

                if (binding != null) {
                    binding.llButtonStop.setVisibility(View.GONE);
                    binding.llButtonStart.setVisibility(View.GONE);
                    binding.llButtonSaveRecordingMain.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getContext(), "Recording failed", Toast.LENGTH_SHORT).show();
                if (binding != null) {
                    binding.llButtonStop.setVisibility(View.GONE);
                    binding.llButtonStart.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override public void elapsedTime(long currentTimeMs, long totalTimeMs) {}
    @Override public void playingComplete() {}

    private void saveAndAdvance() {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(getContext(), "Please record first", Toast.LENGTH_SHORT).show();
            return;
        }

        db.insertRecord(
                patientUuid, visitUuid, encounterUuid,
                type, position, recordingStatus,
                filePath, "Pending");

        Log.d("SOUND_FLOW", "Saved index " + currentIndex + " (" + position + ")");

        currentIndex++;
        filePath = "";

        if (currentIndex < sounds.size()) {
            currentSound = sounds.get(currentIndex);
            position = currentSound;

            if (binding != null) {
                binding.llButtonSaveRecordingMain.setVisibility(View.GONE);
                binding.llButtonStart.setVisibility(View.VISIBLE);
                binding.llButtonStop.setVisibility(View.GONE);
                // FIX: Reset timer display for fresh recording
                binding.txtTimer.setText("0.0 / 30.0");
            }

            // FIX: updatePositionUI clears waveform + updates image + text
            updatePositionUI(currentSound);

            Toast.makeText(getContext(), "Next: " + position, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "All sounds recorded", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof org.intelehealth.app.ayu.visit.VisitCreationActivity) {
                ((org.intelehealth.app.ayu.visit.VisitCreationActivity) getActivity())
                        .completedSoundTypes.add(type);
            }

            Bundle resultBundle = new Bundle();
            resultBundle.putString("type", type);
            resultBundle.putInt("count", sounds.size());
            getParentFragmentManager()
                    .setFragmentResult(RESULT_SOUND_DONE, resultBundle);
            popBack();
        }
    }

    private void popBack() {
        if (isAdded() && !isStateSaved()) {
            getParentFragmentManager().popBackStack();
        }
    }

    private byte[] shortToByte(short[] shorts) {
        ByteBuffer buf = ByteBuffer.allocate(shorts.length * 2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : shorts) buf.putShort(s);
        return buf.array();
    }

    private String saveToFile(byte[] bytes) {
        try {
            File dir = new File(requireContext().getExternalFilesDir(null), "records");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, position + "_" + System.currentTimeMillis() + ".pcm");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onDestroyView() {
        timerHandler.removeCallbacksAndMessages(null);
        try {
            AyuSynk.getBleInstance().setRecorderListener(null);
            AyuSynk.getBleInstance().setAyuVisualizerView(null);
        } catch (Exception ignored) {}
        binding = null;
        super.onDestroyView();
    }
}