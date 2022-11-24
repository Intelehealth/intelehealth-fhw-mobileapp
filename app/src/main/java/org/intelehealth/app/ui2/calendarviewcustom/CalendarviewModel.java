package org.intelehealth.app.ui2.calendarviewcustom;

public class CalendarviewModel {
    int date, headerPositionForFirstDay, headerPositionForLastDay;
    boolean isCurrentDate, isPrevMonth, isNextMonth;

    public int getHeaderPositionForFirstDay() {
        return headerPositionForFirstDay;
    }

    public void setHeaderPositionForFirstDay(int headerPositionForFirstDay) {
        this.headerPositionForFirstDay = headerPositionForFirstDay;
    }

    public int getHeaderPositionForLastDay() {
        return headerPositionForLastDay;
    }

    public void setHeaderPositionForLastDay(int headerPositionForLastDay) {
        this.headerPositionForLastDay = headerPositionForLastDay;
    }

    public boolean isPrevMonth() {
        return isPrevMonth;
    }

    public void setPrevMonth(boolean prevMonth) {
        isPrevMonth = prevMonth;
    }

    public boolean isNextMonth() {
        return isNextMonth;
    }

    public void setNextMonth(boolean nextMonth) {
        isNextMonth = nextMonth;
    }

    public CalendarviewModel(int date, int headerPositionForFirstDay,
                             int headerPositionForLastDay, boolean isCurrentDate) {
        this.date = date;
        this.headerPositionForFirstDay = headerPositionForFirstDay;
        this.headerPositionForLastDay = headerPositionForLastDay;
        this.isCurrentDate = isCurrentDate;
    }

    public CalendarviewModel(int date, int headerPositionForFirstDay,
                             int headerPositionForLastDay, boolean isCurrentDate,
                             boolean isPrevMonth, boolean isNextMonth) {
        this.date = date;
        this.headerPositionForFirstDay = headerPositionForFirstDay;
        this.headerPositionForLastDay = headerPositionForLastDay;
        this.isCurrentDate = isCurrentDate;
        this.isPrevMonth = isPrevMonth;
        this.isNextMonth = isNextMonth;

    }

    public boolean isCurrentDate() {
        return isCurrentDate;
    }

    public void setCurrentDate(boolean currentDate) {
        isCurrentDate = currentDate;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

}
