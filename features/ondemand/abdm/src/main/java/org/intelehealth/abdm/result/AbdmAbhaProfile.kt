package org.intelehealth.abdm.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The ABHA profile data handed back to the host so it can pre-fill patient registration.
 * Public, Parcelable, and decoupled from the module's internal domain models.
 */
@Parcelize
data class AbdmAbhaProfile(
    val abhaNumber: String,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val dateOfBirth: String,
    val gender: String,
    val mobile: String,
    val address: String,
    val pinCode: String,
    val profilePhoto: String?,
    val phrAddresses: List<String>,
    val preferredAbhaAddress: String?,
) : Parcelable
