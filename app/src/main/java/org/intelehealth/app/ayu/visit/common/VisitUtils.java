package org.intelehealth.app.ayu.visit.common;

import androidx.recyclerview.widget.RecyclerView;

public class VisitUtils {
    public static boolean checkNodeValidByGenderAndAge(String patientGender, float floatAgeYearMonth, String nodeGender, String minAge, String maxAge) {

        float minAgeF = minAge != null && !minAge.isEmpty() ? Float.parseFloat(minAge) : 0f;
        float maxAgeF = maxAge != null && !maxAge.isEmpty() ? Float.parseFloat(maxAge) : 0f;
        boolean isValidByGender = true;
        if (patientGender.equalsIgnoreCase("M") &&
                nodeGender.equalsIgnoreCase("0")) {

            isValidByGender = false;
        } else if (patientGender.equalsIgnoreCase("F") &&
                nodeGender.equalsIgnoreCase("1")) {
            isValidByGender = false;
        }

        if (isValidByGender) {
            if (minAgeF != 0f && maxAgeF != 0f) {
                isValidByGender = minAgeF <= floatAgeYearMonth && floatAgeYearMonth <= maxAgeF;
            } else if (minAgeF != 0f) {
                isValidByGender = floatAgeYearMonth >= minAgeF;
            } else if (maxAgeF != 0f) {
                isValidByGender = floatAgeYearMonth <= maxAgeF;
            }
        }
        return isValidByGender;
    }

    public static void scrollNow(RecyclerView recyclerView, long delayMills, int dx, int dy){
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {

                recyclerView.smoothScrollBy(dx, dy);
            }
        }, delayMills);
    }
}
