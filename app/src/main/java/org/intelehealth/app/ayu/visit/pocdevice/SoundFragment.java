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

/**
 * Records a sequence of stethoscope sounds (e.g. Aortic, Pulmonic, ...).
 *
 * Flow:
 *   1. PhysicalExaminationFragment opens this fragment (replace into fl_steps_body
 *      with addToBackStack).
 *   2. On start, this fragment shows {@link AyuConnectDialogFragment} so the user
 *      can pair the AyuSynk device.
 *   3. When the dialog reports "connected", the recording UI is enabled and the
 *      user records each position one by one.
 *   4. After the last position is saved, this fragment posts the
 *      "sound_done" fragment result and pops itself off the back stack.
 *      PhysicalExaminationFragment's existing listener then advances to the
 *      next exam question.
 */
public class SoundFragment extends Fragment implements RecorderListener {

    public static final String RESULT_SOUND_DONE = "sound_done";

    /** Optional listener interface kept for backward-compat with older call sites. */
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
    private static final int MAX_DURATION_MS = 10000;
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
                binding.txtTimer.setText(String.format("%.1f / 10.0", elapsed / 1000f));
                if (elapsed < MAX_DURATION_MS) {
                    timerHandler.postDelayed(this, 100);
                } else {
                    stopRecordingInternal();
                }
            }
        };

        // Recording controls are disabled until the user finishes the connect dialog.
        binding.btnStartRecording.setEnabled(false);
        binding.btnBack.setOnClickListener(v -> popBack());

        // Listen for the dialog's result
        getParentFragmentManager().setFragmentResultListener(
                AyuConnectDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    boolean connected = bundle.getBoolean(
                            AyuConnectDialogFragment.RESULT_CONNECTED, false);
                    if (connected) {
                        binding.btnStartRecording.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Device connected. Record: " + currentSound,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // user cancelled — leave the screen
                        popBack();
                    }
                });

        // Show the connect dialog right away
        if (savedInstanceState == null) {
            AyuConnectDialogFragment.newInstance()
                    .show(getParentFragmentManager(), "ayu_connect");
        }

        setupRecordingUi();
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

        AyuSynk.getBleInstance().startRecording();
        AyuSynk.getBleInstance().setRecorderListener(this);
        AyuSynk.getBleInstance().setAyuVisualizerView(binding.waveView);

        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);

        binding.llButtonStart.setVisibility(View.GONE);
        binding.llButtonStop.setVisibility(View.VISIBLE);
    }

    private void stopRecordingInternal() {
        timerHandler.removeCallbacks(timerRunnable);
        // SDK fires recordingComplete() when the audio buffer is ready.
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
            // move to next position; user records again
            currentSound = sounds.get(currentIndex);
            position = currentSound;
            if (binding != null) {
                binding.llButtonSaveRecordingMain.setVisibility(View.GONE);
                binding.llButtonStart.setVisibility(View.VISIBLE);
                binding.llButtonStop.setVisibility(View.GONE);
            }
            Toast.makeText(getContext(), "Next: " + position, Toast.LENGTH_SHORT).show();
        } else {
            // all positions recorded — return to PhysicalExaminationFragment
            Toast.makeText(getContext(), "All sounds recorded", Toast.LENGTH_SHORT).show();

            // Mark this exam type as completed for the visit so the adapter's
            // auto-trigger in onBindViewHolder doesn't re-open SoundFragment
            // when PhysicalExaminationFragment is reattached.
            if (getActivity() instanceof org.intelehealth.app.ayu.visit.VisitCreationActivity) {
                ((org.intelehealth.app.ayu.visit.VisitCreationActivity) getActivity())
                        .completedSoundTypes.add(type);
            }

            Bundle resultBundle = new Bundle();
            resultBundle.putString("type", type);          // "heart" or "lung"
            resultBundle.putInt("count", sounds.size());   // 4 or 6
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
