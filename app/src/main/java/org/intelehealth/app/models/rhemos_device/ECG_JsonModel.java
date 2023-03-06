package org.intelehealth.app.models.rhemos_device;

/**
 * Created by Prajwal Waingankar
 * on February 2023.
 * Github: prajwalmw
 */
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

    public String getR_r_interval() {
        return r_r_interval;
    }

    public void setR_r_interval(String r_r_interval) {
        this.r_r_interval = r_r_interval;
    }

    public String getHeart_rate() {
        return heart_rate;
    }

    public void setHeart_rate(String heart_rate) {
        this.heart_rate = heart_rate;
    }

    public String getHrv() {
        return hrv;
    }

    public void setHrv(String hrv) {
        this.hrv = hrv;
    }

    public String getRespiratory_rate() {
        return respiratory_rate;
    }

    public void setRespiratory_rate(String respiratory_rate) {
        this.respiratory_rate = respiratory_rate;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getHeart_age() {
        return heart_age;
    }

    public void setHeart_age(String heart_age) {
        this.heart_age = heart_age;
    }

    public String getHeart_beat() {
        return heart_beat;
    }

    public void setHeart_beat(String heart_beat) {
        this.heart_beat = heart_beat;
    }

    public String getRobust_heart_rate() {
        return robust_heart_rate;
    }

    public void setRobust_heart_rate(String robust_heart_rate) {
        this.robust_heart_rate = robust_heart_rate;
    }

    public String getStress_level() {
        return stress_level;
    }

    public void setStress_level(String stress_level) {
        this.stress_level = stress_level;
    }
}
