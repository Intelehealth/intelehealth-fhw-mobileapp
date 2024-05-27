package org.intelehealth.config.network.response

import org.intelehealth.config.room.entity.PatientRegistrationFields

/**
 * Created by Vaghela Mithun R. on 19-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class PatientRegFieldConfig(
    val personal: List<PatientRegistrationFields>,
    val address: List<PatientRegistrationFields>,
    val other: List<PatientRegistrationFields>
)
