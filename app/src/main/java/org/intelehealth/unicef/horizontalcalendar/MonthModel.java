package org.intelehealth.unicef.horizontalcalendar;

public class MonthModel {
    String monthName;
    int monthNo;
    boolean isCurrentMonth;

    public MonthModel(String monthName, int monthNo, boolean isCurrentMonth) {
        this.monthName = monthName;
        this.monthNo = monthNo;
        this.isCurrentMonth = isCurrentMonth;
    }
}
