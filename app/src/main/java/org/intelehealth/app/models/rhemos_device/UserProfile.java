package org.intelehealth.app.models.rhemos_device;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.linktop.constant.IUserProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserProfile extends BaseObservable implements IUserProfile {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    private String username = "";
    private int gender;//0 means female; 1 means male.
    private long birthday;
    private int height;
    private int weight;

    public UserProfile() {
    }

    public UserProfile(String username, int gender, long birthday, int height, int weight) {
        this.username = username;
        this.gender = gender;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
    }

    @Bindable
    @NonNull
    @Override
    public String getUsername() {
        return this.username;
    }

    public void setUsername(@NonNull String username) {
        if (!username.equals(this.username)) {
            this.username = username;
        }
    }

    @Override
    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        if (gender != this.gender) {
            this.gender = gender;
        }
    }

    @Override
    public long getBirthday() {
        return birthday;
    }

    @Bindable
    public String getFormatBirthday() {
        if (birthday != 0) {
            return sdf.format(new Date(birthday));
        }
        return "";
    }

    public void setBirthday(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        setBirthday(calendar.getTimeInMillis());
    }

    public void setBirthday(long birthday) {
        if (birthday != this.birthday) {
            this.birthday = birthday;
        }
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height != this.height) {
            this.height = height;
        }
    }

    @Bindable
    public String getHeightStr() {
        return String.valueOf(this.height);
    }

    public void setHeightStr(String height) {
        int heightInt;
        try {
            heightInt = Integer.parseInt(height);
        } catch (NumberFormatException ignored) {
            heightInt = 0;
        }
        setHeight(heightInt);
    }

    @Override
    public int getWeight() {
        return weight;
    }


    public void setWeight(int weight) {
        if (weight != this.weight) {
            this.weight = weight;
        }
    }

    @Bindable
    public String getWeightStr() {
        return String.valueOf(this.weight);
    }

    public void setWeightStr(String weight) {
        int weightInt;
        try {
            weightInt = Integer.parseInt(weight);
        } catch (NumberFormatException ignored) {
            weightInt = 0;
        }
        setWeight(weightInt);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", gender=" + gender +
                ", birthday=" + birthday +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }
}