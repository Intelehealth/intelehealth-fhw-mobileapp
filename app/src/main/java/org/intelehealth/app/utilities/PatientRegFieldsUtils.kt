package org.intelehealth.app.utilities

import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.hbb20.CountryCodePicker
import org.intelehealth.config.room.entity.PatientRegistrationFields

/**
 * Created by Tanvir Hasan on 30-04-2024 : 12-31.
 * Email: mhasan@intelehealth.org
 */
class PatientRegFieldsUtils {
    companion object{
        fun configField(
            isEditMode: Boolean,
            field: PatientRegistrationFields,
            layoutView: View,
            fieldViewPrimary: View,
            fieldViewSecondary: View?
        ) {
            layoutView.visibility = View.VISIBLE
            if (isEditMode && !field.isEditable) {
                if (fieldViewPrimary is RadioGroup) {
                    for (i in 0 until fieldViewPrimary.childCount) {
                        val child: View = fieldViewPrimary.getChildAt(i)
                        val radioButton = child as RadioButton
                        radioButton.isClickable = false
                    }
                } else {
                    fieldViewPrimary.isEnabled = false
                    if (fieldViewSecondary != null) {
                        when (fieldViewSecondary) {
                            is CountryCodePicker -> {
                                fieldViewSecondary.setCcpClickable(false)
                            }

                            is Button -> {
                                fieldViewSecondary.setVisibility(View.GONE)
                            }

                            is TextView -> {
                                fieldViewSecondary.setVisibility(View.GONE)
                            }

                            else -> {
                                fieldViewSecondary.isEnabled = false
                            }
                        }
                    }
                }
            }
        }

        fun getFieldEnableStatus(
            patientRegistrationFields: List<PatientRegistrationFields>,
            fieldType: String
        ): Boolean {
            if (patientRegistrationFields.isEmpty()) return false
            for (field in  patientRegistrationFields) {
                if (field.idKey == fieldType) return field.isEnabled
            }
            return false
        }

        fun getFieldMandatoryStatus(
            patientRegistrationFields: List<PatientRegistrationFields>,
            fieldType: String
        ): Boolean {
            if (patientRegistrationFields.isEmpty()) return false
            for (field in  patientRegistrationFields) {
                if (field.idKey == fieldType) return field.isMandatory
            }
            return false
        }

    }
}