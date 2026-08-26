package org.intelehealth.abdm.config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A local (HMIS) patient record, surfaced across the module boundary for the verify→compare flow.
 * Public (not internal) so the host can both construct it (lookups) and read a merged copy back
 * (save). Parcelable so it can also carry the compare screen's local/ABHA sides between activities.
 * Empty strings rather than nulls keep the compare UI simple.
 */
@Parcelize
data class LocalPatientRecord(
    val uuid: String,
    val openMrsId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val address: String = "",
    val pinCode: String = "",
    val phoneNumber: String = "",
    val abhaNumber: String = "",
    val abhaAddress: String = "",
) : Parcelable
