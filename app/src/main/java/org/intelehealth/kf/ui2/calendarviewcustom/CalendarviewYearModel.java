package org.intelehealth.kf.ui2.calendarviewcustom;

public class CalendarviewYearModel {
    public int getYear() {
        return year;
    }

    public CalendarviewYearModel(int year, boolean isCurrentYear) {
        this.year = year;
        this.isCurrentYear = isCurrentYear;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isCurrentYear() {
        return isCurrentYear;
    }

    public void setCurrentYear(boolean currentYear) {
        isCurrentYear = currentYear;
    }

    int year;
    boolean isCurrentYear;
}
