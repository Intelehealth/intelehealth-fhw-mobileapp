package org.intelehealth.app.ui.language.activity

import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.utilities.PatientRegConfigKeys
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.PatientRegistrationFields

object StaticLanguageEnabledFieldsHelper {

    fun getEnabledLanguageFields(): List<ActiveLanguage> {
        val fields: MutableList<ActiveLanguage> = mutableListOf()

        // English language
        var currentField = ActiveLanguage(
            generalName = "English",
            name = "English",
            code = AppConstants.LANGUAGE_EN,
            isDefault = true
        )

        fields.add(currentField)

        // Hindi Language
        currentField = ActiveLanguage(
            generalName = "Hindi",
            name = "हिंदी",
            code = AppConstants.LANGUAGE_HI,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Bengali",
            name = "বাংলা",
            code = AppConstants.LANGUAGE_BN,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Gujarati",
            name = "ગુજરાતી",
            code = AppConstants.LANGUAGE_GU,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Marathi",
            name = "मराठी",
            code = AppConstants.LANGUAGE_MR,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Kannada",
            name = "ಕನ್ನಡ",
            code = AppConstants.LANGUAGE_KN,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Odia",
            name = "ଓଡିଆ",
            code = AppConstants.LANGUAGE_OR,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Assamese",
            name = "অসমীয়া",
            code = AppConstants.LANGUAGE_AS,
            isDefault = false
        )

        fields.add(currentField)

        currentField = ActiveLanguage(
            generalName = "Telugu",
            name = "తెలుగు",
            code = AppConstants.LANGUAGE_TE,
            isDefault = false
        )

        fields.add(currentField)

        return fields
    }

}