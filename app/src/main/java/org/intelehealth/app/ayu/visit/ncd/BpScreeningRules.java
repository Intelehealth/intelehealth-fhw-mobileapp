package org.intelehealth.app.ayu.visit.ncd;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * BP readings for hypertension screening, from the PO's flowchart (no Android code, so it is unit tested).
 * Reading 2 is taken when reading 1 is at or above 140/90; reading 3 when readings 1 and 2 differ by
 * more than 5 mmHg in systolic or diastolic. The last reading taken is the final BP: the PO chose this
 * over the average the ticket first described.
 */
public final class BpScreeningRules {

    // Screening threshold: 140/90 mmHg
    public static final int SYSTOLIC_THRESHOLD = 140;
    public static final int DIASTOLIC_THRESHOLD = 90;
    // Readings 1 and 2 "differ" when systolic or diastolic is more than this apart (mmHg)
    public static final int MAX_DIFFERENCE = 5;
    // Readings are numbered 1 to 3
    public static final int MAX_READINGS = 3;
    // Wait before readings 2 and 3 (PO; Figma says "01–02 minutes")
    public static final int WAIT_SECONDS = 120;

    // Rules only; not meant to be created
    private BpScreeningRules() {
    }

    // True when the visit takes up to 3 readings, i.e. its NcdScreeningPlan offers HTN screening (the plan applies
    // consent, eligibility, known case and CBAC). A null plan means no NCD screening, so a single BP.
    public static boolean usesMultipleReadings(@Nullable NcdScreeningPlan plan) {
        return plan != null && plan.getHtnSkipReason() == null;
    }

    // True when systolic is 140 or more, or diastolic 90 or more. Calls for reading 2, and makes a final BP
    // count towards camp eligibility.
    public static boolean atOrAboveThreshold(BpReading reading) {
        return reading.getSystolic() >= SYSTOLIC_THRESHOLD || reading.getDiastolic() >= DIASTOLIC_THRESHOLD;
    }

    // True when systolic or diastolic differs by more than 5 mmHg; reading 3 is then needed
    public static boolean differ(BpReading first, BpReading second) {
        return Math.abs(first.getSystolic() - second.getSystolic()) > MAX_DIFFERENCE
                || Math.abs(first.getDiastolic() - second.getDiastolic()) > MAX_DIFFERENCE;
    }

    // How many readings the entered ones call for: 1 when reading 1 is missing or below the threshold, 2 while
    // reading 2 is missing or close to reading 1, otherwise 3
    public static int requiredCount(@Nullable BpReading reading1, @Nullable BpReading reading2) {
        if (reading1 == null || !atOrAboveThreshold(reading1)) return 1;
        if (reading2 == null || !differ(reading1, reading2)) return 2;
        return MAX_READINGS;
    }

    // The final BP while readings are being entered: the last required one. Null while that reading is still
    // missing, which keeps Next disabled.
    @Nullable
    public static BpReading finalReading(@Nullable BpReading reading1, @Nullable BpReading reading2, @Nullable BpReading reading3) {
        switch (requiredCount(reading1, reading2)) {
            case 1:
                return reading1;
            case 2:
                return reading2;
            default:
                return reading3;
        }
    }

    // Final BP of saved readings: the highest-numbered one, since only the readings that were needed are saved.
    // Null when there are none.
    @Nullable
    public static BpReading lastReading(@Nullable List<BpReading> readings) {
        BpReading last = null;
        if (readings == null) return null;
        for (BpReading reading : readings) {
            if (reading != null && (last == null || reading.getNumber() > last.getNumber())) last = reading;
        }
        return last;
    }
}
