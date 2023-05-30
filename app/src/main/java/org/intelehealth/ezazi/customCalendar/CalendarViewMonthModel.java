package org.intelehealth.ezazi.customCalendar;

public class CalendarViewMonthModel {
    String monthName;
    int monthNo;
    boolean isCurrentMonth;

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public int getMonthNo() {
        return monthNo;
    }

    public void setMonthNo(int monthNo) {
        this.monthNo = monthNo;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setCurrentMonth(boolean currentMonth) {
        isCurrentMonth = currentMonth;
    }

    public CalendarViewMonthModel(String monthName, int monthNo, boolean isCurrentMonth) {
        this.monthName = monthName;
        this.monthNo = monthNo;
        this.isCurrentMonth = isCurrentMonth;
    }
}
