package org.intelehealth.ezazi.ui.prescription.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.intelehealth.ezazi.partogram.PartogramConstants.AccessMode
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment
import java.io.Serializable

/**
 * Created by Vaghela Mithun R. on 22-02-2024 - 14:26.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

@Parcelize
data class PrescriptionArg(
    val visitId: String,
    val prescriptionType: PrescriptionFragment.PrescriptionType = PrescriptionFragment.PrescriptionType.FULL,
    val allowAdminister: Boolean = false,
    val accessMode: AccessMode = AccessMode.READ
) : Parcelable
