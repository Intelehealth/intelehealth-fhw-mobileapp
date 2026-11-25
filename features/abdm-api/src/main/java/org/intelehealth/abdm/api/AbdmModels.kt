package org.intelehealth.abdm.api

import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AbdmConfig(
    val baseUrl: String,
    /** Base64 "user:password" used for Basic auth against the EMR/OpenMRS server. */
    val encodedAuth: String,
    val locationUuid: String,
    val hwFullName: String,
    val database: SQLiteDatabase?,
    /** ABHA address domain for this environment: "@sbx" (sandbox) or "@abdm" (production). */
    val abhaAddressSuffix: String
)

enum class AbdmFlow { VERIFY_ABHA, CREATE_ABHA }

enum class AbdmNavigation {
    /** Open patient registration (new patient, or existing one when [AbdmFlowResult.patientUuid] is set). */
    PATIENT_REGISTRATION,

    /** Open patient details for [AbdmFlowResult.patientUuid]. */
    PATIENT_DETAILS
}

/** Tokens needed after a flow finishes (e.g. to download the ABHA card). */
@Parcelize
data class AbdmSession(
    val accessToken: String?,
    val cardToken: String?,
    val cardScope: String?
) : Parcelable

/**
 * Patient data taken from the ABHA profile, already in the app's formats.
 * A null address part means "leave the patient's existing value unchanged".
 */
@Parcelize
data class AbhaPatientDetails(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val gender: String?,
    /** yyyy-MM-dd */
    val dateOfBirth: String?,
    val phoneNumber: String?,
    val address1: String?,
    val cityVillage: String?,
    val district: String?,
    val stateProvince: String?,
    val postalCode: String?,
    val abhaNumber: String?,
    val abhaAddress: String?,
    val photoBase64: String?
) : Parcelable

@Parcelize
data class AbdmFlowResult(
    val navigation: AbdmNavigation,
    val patientUuid: String?,
    /** True when the server was updated and a sync should be triggered. */
    val syncRequired: Boolean,
    val patientDetails: AbhaPatientDetails?,
    val session: AbdmSession
) : Parcelable {
    companion object {
        /** Intent extra key used by the app to pass this result between screens. */
        const val EXTRA = "org.intelehealth.abdm.api.FLOW_RESULT"
    }
}
