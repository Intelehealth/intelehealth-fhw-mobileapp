package org.intelehealth.app.models.rhemos_device;

public class ECG_JsonModel {
    private String r_r_interval;
    private String heart_rate;
    private String hrv;
    private String respiratory_rate;
    private String mood;
    private String heart_age;
    private String heart_beat;
    private String robust_heart_rate;
    private String stress_level;

    public ECG_JsonModel(String r_r_interval, String heart_rate, String hrv,
                         String respiratory_rate, String mood, String heart_age,
                         String heart_beat, String robust_heart_rate, String stress_level) {
        this.r_r_interval = r_r_interval;
        this.heart_rate = heart_rate;
        this.hrv = hrv;
        this.respiratory_rate = respiratory_rate;
        this.mood = mood;
        this.heart_age = heart_age;
        this.heart_beat = heart_beat;
        this.robust_heart_rate = robust_heart_rate;
        this.stress_level = stress_level;
    }
}
