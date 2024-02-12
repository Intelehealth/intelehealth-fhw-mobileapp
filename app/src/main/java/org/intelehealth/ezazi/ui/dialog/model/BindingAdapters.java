package org.intelehealth.ezazi.ui.dialog.model;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import org.intelehealth.ezazi.partogram.model.Medication;

/**
 * Created by Kaveri Zaware on 09-02-2024
 * email - kaveri@intelehealth.org
 **/
public class BindingAdapters {
    @BindingAdapter("medicationText")
    public static void setMedicationText(TextView textView, Medication medication) {
        if (medication != null) {
            String displayText;
            if ("other".equalsIgnoreCase(medication.getType())) {
                displayText = medication.getOtherType();
            } else {
                displayText = medication.getType();
            }
            textView.setText(displayText);
        }
    }
}