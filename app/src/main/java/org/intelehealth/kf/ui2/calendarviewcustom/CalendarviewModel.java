package org.intelehealth.kf.ui2.calendarviewcustom;

public class CalendarviewModel {
    int date, headerPositionForFirstDay, headerPositionForLastDay, selectedMonth, selectedYear;

    public boolean isCurrentMonthCompletedDate() {
        return isCurrentMonthCompletedDate;
    }

    public void setCurrentMonthUpcomingDate(boolean currentMonthUpcomingDate) {
        isCurrentMonthCompletedDate = currentMonthUpcomingDate;
    }

    boolean isCurrentDate, isPrevMonth, isNextMonth, isCurrentMonthCompletedDate;

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

    public int getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(int selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(int selectedYear) {
        this.selectedYear = selectedYear;
    }

    public void setCurrentMonthCompletedDate(boolean currentMonthCompletedDate) {
        isCurrentMonthCompletedDate = currentMonthCompletedDate;
    }

    public CalendarviewModel(int date, int headerPositionForFirstDay,
                             int headerPositionForLastDay, boolean isCurrentDate,
                             boolean isPrevMonth, boolean isNextMonth, boolean isCurrentMonthCompletedDate,
                             int selectedMonth, int selectedYear) {
        this.date = date;
        this.headerPositionForFirstDay = headerPositionForFirstDay;
        this.headerPositionForLastDay = headerPositionForLastDay;
        this.isCurrentDate = isCurrentDate;
        this.isPrevMonth = isPrevMonth;
        this.isNextMonth = isNextMonth;
        this.isCurrentMonthCompletedDate = isCurrentMonthCompletedDate;
        this.selectedMonth = selectedMonth;
        this.selectedYear = selectedYear;



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
