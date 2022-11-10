package org.intelehealth.app.horizontalcalendar;

public class CalendarModel {
    String day;
    int date, currentDate;
    boolean isCurrentDate;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(int currentDate) {
        this.currentDate = currentDate;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public CalendarModel(String day, int date, int currentDate, boolean isCurrentDate) {
        this.day = day;
        this.date = date;
        this.currentDate = currentDate;
        this.isCurrentDate = isCurrentDate;

    }
}
