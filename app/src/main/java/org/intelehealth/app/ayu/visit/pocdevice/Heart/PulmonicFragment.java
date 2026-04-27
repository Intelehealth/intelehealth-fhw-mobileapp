package org.intelehealth.app.ayu.visit.pocdevice.Heart;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
import com.github.ajalt.timberkt.Timber;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.EncounterManager;
import org.intelehealth.app.ayu.visit.model.HeartLungRecordModel;
import org.intelehealth.app.ayu.visit.pocdevice.UploadManager;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.databinding.FragmentPulmonicBinding;
import org.intelehealth.app.utilities.PCMToWavConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


public class PulmonicFragment extends Fragment implements RecorderListener {
    private FragmentPulmonicBinding mBinding;
    private static final int AUDIO_PERMISSION_REQUEST = 101;
    private String lastRecordedFilePath = "";
    long safeTime;
    EncounterManager manager;
    String filePath;
    InteleHealthDatabaseHelper db;
    private static final String ARG_PATIENT_UUID = "patientUuid";
    private static final String ARG_VISIT_UUID = "visitUuid";
    private static final String ARG_PATIENT_NAME = "patientName";
    private static final String ARG_INTENT_TAG = "intentTag";
    private static final String ARG_AGE = "float_ageYear_Month";
    private static final String ARG_TYPE = "type";
    private static final String ENCOUNTER_UUID = "encounterUuid";
    private static  final String ARG_PATINT_NAME = "patientName";

    String patientUuid, visitUuid, encounterUuid, intentTag, type;
    int recordingStatus;
    UploadManager uploadManager;
    float float_ageYear_Month;


    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;

    private final int MAX_DURATION = 10000; // 10 seconds

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

    public static PulmonicFragment newInstance(boolean isEditMode,
                                                String patientName,
                                                String patientUuid,
                                                String visitUuid,
                                                String encounterUuid,
                                                String intentTag,
                                                float float_ageYear_Month,
                                                String type) {

        PulmonicFragment pulmonicFragment = new PulmonicFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEditMode", isEditMode);

        args.putString(ARG_PATIENT_UUID, patientUuid);
        args.putString(ARG_PATINT_NAME,patientName);
        args.putString(ARG_VISIT_UUID, visitUuid);
        args.putString(ENCOUNTER_UUID, encounterUuid);
        args.putString(ARG_INTENT_TAG, intentTag);
        args.putFloat(ARG_AGE, float_ageYear_Month);
        args.putString(ARG_TYPE, type); // Heart / Lung

        pulmonicFragment.setArguments(args);
        return pulmonicFragment;

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_pulmonic, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkAudioPermission();
        db = new InteleHealthDatabaseHelper(getContext());
        manager = new EncounterManager();
        uploadManager = new UploadManager();
        UploadManager.setUploadListener(new UploadManager.UploadListener() {
            @Override
            public void onUploadSuccess(String trackerId) {
                afterUpload(trackerId);
            }

            @Override
            public void onAIResultSuccess(String trackerId, String result) {
                onWebhookResult(result);
            }
        });

        if (getArguments() != null) {
            patientUuid = getArguments().getString(ARG_PATIENT_UUID);
            visitUuid = getArguments().getString(ARG_VISIT_UUID);
            encounterUuid = getArguments().getString(ENCOUNTER_UUID);
            intentTag = getArguments().getString(ARG_INTENT_TAG);
            float_ageYear_Month = getArguments().getFloat(ARG_AGE);
            type = getArguments().getString(ARG_TYPE);
            System.out.println("Nagarjuna" +  type);
        }


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
       /* mBinding.btnReRecordingOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.llRecordingAorticHeart.setVisibility(GONE);
                mBinding.llPlayAorticHeart.setVisibility(VISIBLE);
            }
        });*/
        mBinding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.btnPause.setVisibility(VISIBLE);
                String path = getLastAudioPath();
                playAudio(path);


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
                        "heart",
                        "pulmonic",
                        recordingStatus,
                        filePath,
                        "Normal"
                );

                // ✅ Trigger sync to ensure visit_uuid exists on server before upload
                org.intelehealth.app.database.dao.SyncDAO syncDAO = new org.intelehealth.app.database.dao.SyncDAO();
                syncDAO.pushDataApi();

                String pcmPath = filePath;
                if (pcmPath != null && !pcmPath.isEmpty()) {
                    String wavPath = pcmPath.replace(".pcm", ".wav");
                    File wavFile = new File(wavPath);
                    if (wavFile.exists()) {
                        UploadManager.uploadRecording(wavPath, visitUuid, "heart", "pulmonic");
                    } else {
                        Toast.makeText(getContext(), "WAV file not found. Please record again.", Toast.LENGTH_SHORT).show();
                    }
                }

              /*  List<HeartLungRecordModel> records = db.getAllHeartLungRecords();

                for (HeartLungRecordModel r : records) {
                    Timber.tag("DB_DATA").d("ID=" + r.id +
                            " patient=" + r.patientUuid +
                            " visit=" + r.visitUuid +
                            " encounter=" + r.encounterUuid +
                            "type=" + r.type +
                            " pos=" + r.position +
                            " audio=" + r.audioPath +
                            " result=" + r.result);
                }*/
                File pcmFile = new File(filePath);

                if (!pcmFile.exists()) {
                    Log.e("UPLOAD", "PCM file not found: " + filePath);
                    return;
                }

                String wavPath = pcmPath.replace(".pcm", ".wav");

                try {

                    // 1️⃣ Convert PCM → WAV
                    PCMToWavConverter.pcmToWav(pcmPath, wavPath);

                    File wavFile = new File(wavPath);

                    if (!wavFile.exists()) {
                        Log.e("UPLOAD", "WAV conversion failed!");
                        return;
                    }

                    Log.d("UPLOAD", "WAV created: " + wavFile.getAbsolutePath());
                    Log.d("UPLOAD", "WAV size: " + wavFile.length());

                    // 2️⃣ Upload AFTER conversion
                    UploadManager.uploadRecording(String.valueOf(wavFile), visitUuid, "heart", "pulmonic");

                } catch (Exception e) {

                    Log.e("UPLOAD", "Conversion error: " + e.getMessage());
                }
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
        if (!isAdded() || getActivity() == null || mBinding == null){
            safeTime = currentTimeMs;
        }
    }


    @Override
    public void recordingComplete(int status) {
        Log.d("Ayu", "recordingComplete = " + status);

        getActivity().runOnUiThread(() -> {
            recordingStatus = status;
            short[] audioShorts = AyuSynk.getBleInstance().getAudioData(status);

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
                        Toast.LENGTH_SHORT).show()
        );

    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION_REQUEST);
        }
    }
    private void stopManually() {
        timerHandler.removeCallbacks(timerRunnable);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public String getLastAudioPath() {
        InteleHealthDatabaseHelper dbHelper =
                new InteleHealthDatabaseHelper(getContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String path = null;

        Cursor cursor = db.rawQuery(
                "SELECT audio_path FROM tbl_follow_up_heart_lung_recoding " +
                        "ORDER BY id DESC LIMIT 1",
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(0);
        }

        if (cursor != null) cursor.close();
        db.close();

        return path;
    }

    private void playAudio(String path) {
        try {
            File file = new File(path);
            byte[] audioBytes = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            fis.read(audioBytes);
            fis.close();

            playRawAudio(audioBytes);
            AyuSynk.getBleInstance().setAyuVisualizerView(mBinding.waveViewPlay);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Play error", Toast.LENGTH_SHORT).show();
        }
    }


    private void playRawAudio(byte[] audioData) {

        int sampleRate = 44100; // MUST match SDK recording rate
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM
        );

        audioTrack.play();
        AyuSynk.getBleInstance().getAudioData(audioData.length);

        new Thread(() -> {
            audioTrack.write(audioData, 0, audioData.length);
            audioTrack.stop();
            audioTrack.release();
        }).start();
    }

    void afterUpload(String audioUrl) {

        String encounterUuid = db.getEncounter(visitUuid);

        if (encounterUuid == null) {
            // 4. Create encounter once
            manager.createEncounter(patientUuid, visitUuid, uuid -> {
                db.saveEncounter(visitUuid, uuid);

                // 5. Send audio obs
                manager.sendAudioObs(uuid, audioUrl);
            });
        } else {
            manager.sendAudioObs(encounterUuid, audioUrl);
        }

    }


    // 7. webhook result comes here
    public void onWebhookResult(String result) {
        // 8. update DB
        db.updateResult(visitUuid, "pulmonic", result);
        System.out.println("Nagarjunaresult" + result);
        // 9. send result obs
        String enc = db.getEncounter(visitUuid);
        manager.sendResultObs(enc, result);
    }
}