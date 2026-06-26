package org.intelehealth.app.ayu.visit.pocdevice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
    private int recordingStatus = 0;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;

    private static final int MAX_DURATION_MS = 30000;
    private boolean mSaveButtonShown = false;

    private Runnable timerRunnable;

    private int heartSoundsSize = 0;
    private int lungSoundsSize  = 0;
    private com.ayudevice.ayusynksdk.visualizer.AyuVisualizerView activeWaveView;


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
        activeWaveView = binding.waveView;
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
            Toast.makeText(getContext(), "No sound positions configured", Toast.LENGTH_SHORT).show();
            popBack();
            return;
        }

        currentIndex = 0;
        currentSound = sounds.get(currentIndex);
        position     = currentSound;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding == null) return;
                long elapsed = System.currentTimeMillis() - startTime;
                binding.txtTimer.setText(String.format("%.1f / 30.0", elapsed / 1000f));
                if (elapsed < MAX_DURATION_MS) {
                    timerHandler.postDelayed(this, 100);
                } else {
                    binding.txtTimer.setText("30.0 / 30.0");
                    stopRecordingInternal();
                }
            }
        };
        heartSoundsSize = args.getInt("heartSoundsSize", 0);
        lungSoundsSize  = args.getInt("lungSoundsSize", 0);

// If not passed separately, fall back to current type size
        if ("heart".equalsIgnoreCase(type) && heartSoundsSize == 0) heartSoundsSize = sounds.size();
        if ("lung".equalsIgnoreCase(type)  && lungSoundsSize  == 0) lungSoundsSize  = sounds.size();

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
            // Pass type + visitUuid + total count for this type
            // Dialog uses totalForType to show "0 / N recorded" correctly
            // REPLACE the dialog open call:
            int heartSize = args.getInt("heartSoundsSize",
                    "heart".equalsIgnoreCase(type) ? sounds.size() : 4);
            int lungSize  = args.getInt("lungSoundsSize",
                    "lung".equalsIgnoreCase(type)  ? sounds.size() : 6);
            AyuConnectDialogFragment
                    .newInstance(type, visitUuid, heartSize, lungSize)
                    .show(getParentFragmentManager(), "ayu_connect");
        }

        setupRecordingUi();
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void updatePositionUI(String sound) {
        if (binding == null || sound == null) return;
        binding.txtPostion.setText(sound);
        binding.imgdirection.setImageResource(getImageForPosition(sound));
        // Only reset wave if not the very first position
        // First position already has the XML waveView ready
        if (currentIndex > 0) {
            resetWaveView();
        }
    }
    private int getImageForPosition(String sound) {
        if (sound == null) return R.drawable.aortic_img;
        switch (sound) {
            case "Aortic":              return R.drawable.aortic_img;
            case "Pulmonic":            return R.drawable.pulmanic;
            case "Tricuspid":           return R.drawable.tricusiped;
            case "Mitral":              return R.drawable.mital;
            case "Anterior-1-Left-Top":     return R.drawable.anterior_one;
            case "Anterior-2-Right-Top":    return R.drawable.anterior_two;
            case "Anterior-3-Left-Middle":  return R.drawable.anterior_three;
            case "Anterior-4-Right-Middle": return R.drawable.anterior_four;
            case "Anterior-5-Left-Lower":   return R.drawable.anterior_five;
            case "Anterior-6-Right-Lower":  return R.drawable.anterior_six;
            case "Lateral-1-Left-Top":    return R.drawable.lateral_one;
            case "Lateral-2-Left-Lower":  return R.drawable.lateral_two;
            case "Lateral-3-Right-Top":   return R.drawable.lateral_three;
            case "Lateral-4-Right-Lower": return R.drawable.lateral_four;
            case "Posterior-1-Left-Top":    return R.drawable.posterior_one_img;
            case "Posterior-2-Right-Top":   return R.drawable.posterior_two_img;
            case "Posterior-3-Left-Middle": return R.drawable.posterior_three_img;
            case "Posterior-4-Right-Middle":return R.drawable.posterior_four_img;
            case "Posterior-5-Left-Lower":  return R.drawable.posterior_five_img;
            case "Posterior-6-Right-Lower": return R.drawable.posterior_six_img;
            default: return R.drawable.aortic_img;
        }
    }

    // ── Recording controls ────────────────────────────────────────────────────

    private void setupRecordingUi() {
        binding.btnStartRecording.setOnClickListener(v -> startRecording());
        binding.btnStopRecording.setOnClickListener(v -> stopRecordingInternal());
        binding.btnSaveRecordingMain.setOnClickListener(v -> saveAndAdvance());

        // PAUSE — pauses recording, stays on stop layout
        // (does NOT advance or show save — user can resume or stop)
        binding.btnPause.setOnClickListener(v -> {
            try {
                AyuSynk.getBleInstance().pauseRecording();
                timerHandler.removeCallbacks(timerRunnable);
                Log.d("SOUND_FLOW", "Paused: " + position);
            } catch (Exception e) {
                Log.e("SOUND_FLOW", "pauseRecording error: " + e.getMessage());
            }
        });

        // RETRY — discard current recording, reset UI back to start
        binding.btnRetryMain.setOnClickListener(v -> {
            // Reset state
            filePath        = "";
            recordingStatus = 0;
            mSaveButtonShown = false;

            // Reset timer display
            binding.txtTimer.setText("0.0 / 30.0");

            // Clear visualizer
            resetWaveView();

            // Show start layout, hide save layout
            binding.llButtonSaveRecordingMain.setVisibility(View.GONE);
            binding.llButtonStop.setVisibility(View.GONE);
            binding.llButtonStart.setVisibility(View.VISIBLE);

            Log.d("SOUND_FLOW", "Retry: reset to start for position=" + position);
        });
    }

  /*  private void startRecording() {
        if (AyuSynk.getBleInstance().isDeviceConnected()
                != DeviceConnectionState.DEVICE_CONNECTED) {
            Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        mSaveButtonShown = false;
        filePath         = "";
        recordingStatus  = 0;

        // ADD — detach first, then re-attach fresh so wave starts from zero
        AyuSynk.getBleInstance().setAyuVisualizerView(binding.waveView);
        AyuSynk.getBleInstance().setRecorderListener(this);
        AyuSynk.getBleInstance().startRecording();

        startTime = System.currentTimeMillis();
        binding.txtTimer.setText("0.0 / 30.0");
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);

        binding.llButtonStart.setVisibility(View.GONE);
        binding.llButtonStop.setVisibility(View.VISIBLE);
        Log.d("SOUND_FLOW", "startRecording: " + position);
    }*/
  private void startRecording() {
      if (AyuSynk.getBleInstance().isDeviceConnected()
              != DeviceConnectionState.DEVICE_CONNECTED) {
          Toast.makeText(getContext(), "Device not connected", Toast.LENGTH_SHORT).show();
          return;
      }
      mSaveButtonShown = false;
      filePath         = "";
      recordingStatus  = 0;

      // FIX — use activeWaveView not binding.waveView
      AyuSynk.getBleInstance().setAyuVisualizerView(activeWaveView);
      AyuSynk.getBleInstance().setRecorderListener(this);
      AyuSynk.getBleInstance().startRecording();

      startTime = System.currentTimeMillis();
      binding.txtTimer.setText("0.0 / 30.0");
      timerHandler.removeCallbacks(timerRunnable);
      timerHandler.post(timerRunnable);

      binding.llButtonStart.setVisibility(View.GONE);
      binding.llButtonStop.setVisibility(View.VISIBLE);
      Log.d("SOUND_FLOW", "startRecording: " + position);
  }

    private void stopRecordingInternal() {
        if (mSaveButtonShown) return;
        timerHandler.removeCallbacks(timerRunnable);
        try {
            AyuSynk.getBleInstance().pauseRecording();
            Log.d("SOUND_FLOW", "pauseRecording: " + position);
        } catch (Exception e) {
            Log.e("SOUND_FLOW", "pauseRecording error: " + e.getMessage());
        }
        timerHandler.postDelayed(this::pullAudioAndShowSave, 600);
    }

    private void pullAudioAndShowSave() {
        if (binding == null || getActivity() == null || mSaveButtonShown) return;
        recordingStatus = 1;
        short[] audio = AyuSynk.getBleInstance().getAudioData(recordingStatus);
        Log.d("SOUND_FLOW", "pull: samples=" + (audio != null ? audio.length : 0)
                + " | " + position);
        if (audio != null && audio.length > 0) {
            filePath = saveToFile(shortToByte(audio));
            showSaveButton();
        } else {
            timerHandler.postDelayed(() -> {
                if (binding == null || mSaveButtonShown) return;
                short[] retry = AyuSynk.getBleInstance().getAudioData(recordingStatus);
                if (retry != null && retry.length > 0) {
                    filePath = saveToFile(shortToByte(retry));
                }
                showSaveButton();
            }, 500);
        }
    }

    private void showSaveButton() {
        if (mSaveButtonShown) return;
        mSaveButtonShown = true;
        if (binding == null) return;
        try { AyuSynk.getBleInstance().setAyuVisualizerView(null); } catch (Exception ignored) {}
        binding.llButtonStop.setVisibility(View.GONE);
        binding.llButtonStart.setVisibility(View.GONE);
        binding.llButtonSaveRecordingMain.setVisibility(View.VISIBLE);
        Log.d("SOUND_FLOW", "Save button VISIBLE: " + position);
    }

    @Override
    public void recordingComplete(int status) {
        if (getActivity() == null || mSaveButtonShown) return;
        Log.d("SOUND_FLOW", "recordingComplete: status=" + status);
        recordingStatus = status;
        getActivity().runOnUiThread(() -> {
            if (mSaveButtonShown) return;
            short[] audio = AyuSynk.getBleInstance().getAudioData(status);
            if (audio != null && audio.length > 0) {
                filePath = saveToFile(shortToByte(audio));
            }
            showSaveButton();
        });
    }

    @Override public void elapsedTime(long currentTimeMs, long totalTimeMs) {}
    @Override public void playingComplete() {}

    // ── Save & advance ────────────────────────────────────────────────────────

    private void saveAndAdvance() {
        db.insertRecord(
                patientUuid, visitUuid, encounterUuid,
                type, position, recordingStatus,
                filePath, "Pending");

        Log.d("SOUND_FLOW", "Saved: " + currentIndex + " | " + type + " | " + position);

        Bundle savedBundle = new Bundle();
        savedBundle.putString("type", type);
        savedBundle.putString("visitUuid", visitUuid);
        savedBundle.putInt("total", sounds.size());
        savedBundle.putInt("heartTotal", heartSoundsSize);  // ADD
        savedBundle.putInt("lungTotal",  lungSoundsSize);   // ADD
        getParentFragmentManager().setFragmentResult(
                AyuConnectDialogFragment.RECORD_SAVED_KEY, savedBundle);

        currentIndex++;
        filePath         = "";
        recordingStatus  = 0;
        mSaveButtonShown = false;

        if (currentIndex < sounds.size()) {
            currentSound = sounds.get(currentIndex);
            position     = currentSound;

            if (binding != null) {
                binding.llButtonSaveRecordingMain.setVisibility(View.GONE);
                binding.llButtonStop.setVisibility(View.GONE);
                binding.llButtonStart.setVisibility(View.VISIBLE);
                binding.txtTimer.setText("0.0 / 30.0");
            }
            updatePositionUI(currentSound);
            Toast.makeText(getContext(), "Next: " + position, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getContext(), "All sounds recorded", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof org.intelehealth.app.ayu.visit.VisitCreationActivity) {
                ((org.intelehealth.app.ayu.visit.VisitCreationActivity) getActivity())
                        .completedSoundTypes.add(type);
            }

            // ADD: Reopen the connect dialog so it shows "4 / 4 recorded" on heart
            // and "0 / 6 recorded" on lung — loadAndDisplayCounts() reads DB on open
            if (isAdded() && !isStateSaved()) {
                AyuConnectDialogFragment
                        .newInstance(type, visitUuid, heartSoundsSize, lungSoundsSize)
                        .show(getParentFragmentManager(), "ayu_connect");
            }

            Bundle resultBundle = new Bundle();
            resultBundle.putString("type", type);
            resultBundle.putInt("count", sounds.size());
            getParentFragmentManager().setFragmentResult(RESULT_SOUND_DONE, resultBundle);
            popBack();

        }
    }

    private void popBack() {
        if (isAdded() && !isStateSaved())
            getParentFragmentManager().popBackStack();
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

    // ADD this method
    private void resetWaveView() {
        if (binding == null) return;

        // Detach SDK from old view
        try { AyuSynk.getBleInstance().setAyuVisualizerView(null); }
        catch (Exception ignored) {}

        // Remove old view from container
        binding.waveContainer.removeAllViews();

        // Create fresh AyuVisualizerView
        activeWaveView = new com.ayudevice.ayusynksdk.visualizer.AyuVisualizerView(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        activeWaveView.setLayoutParams(params);
        activeWaveView.setBackground(binding.waveView.getBackground());

        // Add fresh view into container
        binding.waveContainer.addView(activeWaveView);

        Log.d("SOUND_FLOW", "waveView reset fresh for: " + position);
    }
}
