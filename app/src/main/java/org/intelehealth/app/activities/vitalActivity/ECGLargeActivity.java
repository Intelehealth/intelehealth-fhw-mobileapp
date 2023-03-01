package org.intelehealth.app.activities.vitalActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import org.intelehealth.app.R;
import org.intelehealth.app.models.rhemos_device.ECG;
import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.WaveView;
import org.intelehealth.app.widget.materialprogressbar.rhemos_widget.wave.ECGDrawWave;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;

public class ECGLargeActivity extends AppCompatActivity {
    private ECGDrawWave ecgDrawWave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecglarge);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.ecg);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        WaveView waveView = findViewById(R.id.wave_view);
        Intent intent = getIntent();
        ECGDrawWave.PaperSpeed paperSpeed = (ECGDrawWave.PaperSpeed) intent.getSerializableExtra("paperSpeed");
        ECGDrawWave.Gain gain = (ECGDrawWave.Gain) intent.getSerializableExtra("gain");
        ECG model = (ECG) intent.getSerializableExtra("model");
        ecgDrawWave = new ECGDrawWave();
        ecgDrawWave.setGain(gain);
        ecgDrawWave.setPaperSpeed(paperSpeed);
        waveView.setDrawWave(ecgDrawWave);

        showChart(model.getWave());
    }

    private void showChart(String wave) {
        Observable.just(wave)
                .subscribeOn(Schedulers.newThread()) // 指定 subscribe() 发生在子线程
                .map(s -> s.split(","))
                .map(strings -> {
                    final List<Integer> list = new ArrayList<>();
                    for (String str : strings) {
                        list.add(Integer.valueOf(str));
                    }
                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())// 指定 Subscriber 的回调发生在UI程
                .subscribe(new Subject<List<Integer>>() {
                    @Override
                    public boolean hasObservers() {
                        return false;
                    }

                    @Override
                    public boolean hasThrowable() {
                        return false;
                    }

                    @Override
                    public boolean hasComplete() {
                        return false;
                    }

                    @Override
                    public Throwable getThrowable() {
                        return null;
                    }

                    @Override
                    protected void subscribeActual(Observer<? super List<Integer>> observer) {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Integer> list) {
                        ecgDrawWave.addDataList(list);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        // todo: add case here...
                    }
                });

    }

}