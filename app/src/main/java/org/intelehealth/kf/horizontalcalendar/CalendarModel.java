package org.intelehealth.kf.horizontalcalendar;

public class CalendarModel {
    String day;
    int date, currentDate;
    boolean isSelected;

    public String getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(String selectedYear) {
        this.selectedYear = selectedYear;
    }

    boolean isCurrentDate;
    String selectedMonth;
    String selectedYear;
    String selectedMonthForDays;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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

    public boolean isCurrentDate() {
        return isCurrentDate;
    }

    public void setCurrentDate(boolean currentDate) {
        isCurrentDate = currentDate;
    }

    public String getSelectedMonth() {
        return selectedMonth;
    }

    public String getSelectedMonthForDays() {
        return selectedMonthForDays;
    }

    public void setSelectedMonthForDays(String selectedMonthForDays) {
        this.selectedMonthForDays = selectedMonthForDays;
    }

    public void setSelectedMonth(String selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public CalendarModel(String day, int date, int currentDate,
                         boolean isCurrentDate, String selectedMonth, String selectedYear,boolean isSelected,String selectedMonthForDays) {
        this.day = day;
        this.date = date;
        this.currentDate = currentDate;
        this.isCurrentDate = isCurrentDate;
        this.selectedMonth = selectedMonth;
        this.selectedYear = selectedYear;
        this.isSelected = isSelected;
        this.selectedMonthForDays = selectedMonthForDays;

    }
}
