package org.intelehealth.app.ayu.visit.ncd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * One BP reading of an NCD screening visit. As agreed with the backend, every reading is saved as obs of the
 * usual SBP/DBP concepts, with its number (1, 2 or 3) in the obs comment.
 */
public final class BpReading implements Serializable {

    private final int number;
    private final int systolic;
    private final int diastolic;

    public BpReading(int number, int systolic, int diastolic) {
        this.number = number;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }

    // null unless both values are whole numbers
    @Nullable
    public static BpReading of(int number, @Nullable String systolic, @Nullable String diastolic) {
        try {
            return new BpReading(number, Integer.parseInt(systolic.trim()), Integer.parseInt(diastolic.trim()));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    // Reading number from an obs comment; anything else, including no comment, is reading 1
    public static int numberFromComment(@Nullable String comment) {
        if (comment == null) return 1;
        try {
            int number = Integer.parseInt(comment.trim());
            return number >= 1 && number <= BpScreeningRules.MAX_READINGS ? number : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public int getNumber() {
        return number;
    }

    public int getSystolic() {
        return systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    @NonNull
    @Override
    public String toString() {
        return systolic + "/" + diastolic;
    }
}
