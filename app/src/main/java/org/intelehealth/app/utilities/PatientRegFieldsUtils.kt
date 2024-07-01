package org.intelehealth.app.utilities

import android.util.Log
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
        @JvmStatic
        fun configField(
            isEditMode: Boolean,
            field: PatientRegistrationFields,
            layoutView: View,
            fieldViewPrimary: View?,
            fieldViewSecondary: View?,
            titleTv: TextView?
        ) {
            layoutView.visibility = if (field.isEnabled) View.VISIBLE else View.GONE

            //if existing title has * then removing it first
//            var titleStr = titleTv?.text
//            titleStr?.let {
//                if(it.contains('*')){
//                    titleStr = it.toString().replace("*","")
//                }
//            }
            //setting asterisk for mandatory fields
            var title = titleTv?.text ?: field.name
            if (title.contains("*")) title = title.toString().replace("*", "")
            if (field.isMandatory) {
                titleTv?.text = StringBuilder().append(title.trim()).append(" *")
            } else {
                titleTv?.text = title
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
                    fieldViewPrimary?.isEnabled = false
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
        @JvmStatic
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
        @JvmStatic
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