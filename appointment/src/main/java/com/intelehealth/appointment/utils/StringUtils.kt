/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package com.intelehealth.appointment.utils

import android.content.Context
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object StringUtils {
    private const val NULL_AS_STRING = "null"
    private const val SPACE_CHAR = " "
    
    fun en__hi_dob(dob: String?): String { //English dob is replaced to Hindi text.
        //added this logic to handle crash when dob is null
        if (dob.isNullOrEmpty()) return ""
        val mdob_text = dob
            .replace("January", "जनवरी")
            .replace("February", "फ़रवरी")
            .replace("March", "मार्च")
            .replace("April", "अप्रैल")
            .replace("May", "मई")
            .replace("June", "जून")
            .replace("July", "जुलाई")
            .replace("August", "अगस्त")
            .replace("September", "सितंबर")
            .replace("October", "अक्टूबर")
            .replace("November", "नवंबर")
            .replace("December", "दिसंबर")

        return mdob_text
    }

    
}