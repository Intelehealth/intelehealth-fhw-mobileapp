package org.intelehealth.app.ayu.visit.pocdevice;

import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ayudevice.ayusynksdk.AyuSynk;
import com.ayudevice.ayusynksdk.ble.constants.DeviceConnectionState;

import java.util.List;

public class SoundViewModel extends ViewModel {

    private final MutableLiveData<List<String>> sounds = new MutableLiveData<>();
    private final MutableLiveData<String> selectedSound = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRecording = new MutableLiveData<>(false);

    public void setSounds(List<String> list) {
        sounds.setValue(list);
    }

    public LiveData<List<String>> getSounds() {
        return sounds;
    }

    public void selectSound(String sound) {
        selectedSound.setValue(sound);
    }

    public LiveData<String> getSelectedSound() {
        return selectedSound;
    }

    public LiveData<Boolean> getRecordingState() {
        return isRecording;
    }

    public void startRecording(String sound) {

        // 👉 integrate AyuSynk recording here
        // Simulate stop after some time (replace with real callback)
        new android.os.Handler().postDelayed(() -> {
            isRecording.setValue(false);
        }, 2000);
    }
}
