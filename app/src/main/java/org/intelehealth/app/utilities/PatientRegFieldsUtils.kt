package org.intelehealth.app.utilities

import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.hbb20.CountryCodePicker
import org.intelehealth.config.room.entity.PatientRegistrationFields
import java.lang.StringBuilder

/**
 * Created by Tanvir Hasan on 30-04-2024 : 12-31.
 * Email: mhasan@intelehealth.org
 */
class PatientRegFieldsUtils {
    companion object {
        fun configField(
            isEditMode: Boolean,
            field: PatientRegistrationFields,
            layoutView: View,
            fieldViewPrimary: View,
            fieldViewSecondary: View?,
            titleTv: TextView
        ) {
            layoutView.visibility = View.VISIBLE

            //setting asterisk for mandatory fields
            if (field.isMandatory) {
                val titleStr = titleTv.text
                titleTv.text = StringBuilder().append(titleStr).append(" *")
            }

            //view updating while edit mode is false
            if (isEditMode && !field.isEditable) {
                //disabling radio group child here
                if (fieldViewPrimary is RadioGroup) {
                    for (i in 0 until fieldViewPrimary.childCount) {
                        val child: View = fieldViewPrimary.getChildAt(i)
                        val radioButton = child as RadioButton
                        radioButton.isClickable = false
                    }
                } else {
                    fieldViewPrimary.isEnabled = false
                    //some fields has secondary fields, ex: phone num has country picker
                    //this type of view are handling here
                    fieldViewSecondary?.let {
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

        /**
         * checking enable status of each field
         */
        fun getFieldEnableStatus(
            patientRegistrationFields: List<PatientRegistrationFields>,
            fieldType: String
        ): Boolean {
            if (patientRegistrationFields.isEmpty()) return false
            patientRegistrationFields.find { it.idKey == fieldType }?.let {
                return it.isEnabled
            }
            return false
        }

        /**
         * checking mandatory status of each field
         */
        fun getFieldMandatoryStatus(
            patientRegistrationFields: List<PatientRegistrationFields>,
            fieldType: String
        ): Boolean {
            if (patientRegistrationFields.isEmpty()) return false
            patientRegistrationFields.find { it.idKey == fieldType }?.let {
                return it.isMandatory
            }
            return false
        }

    }
}