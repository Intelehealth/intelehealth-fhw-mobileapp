package org.intelehealth.ezazi.ui.prescription.data

import org.intelehealth.ezazi.builder.QueryBuilder
import org.intelehealth.ezazi.partogram.PartogramConstants.Params

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 16:13.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionQueryBuilder : QueryBuilder() {
    fun buildPrescriptionQuery(visitId: String): String {
        return select("O.uuid, O.conceptuuid, O.value, O.created_date, O.creator ")
            .from(" tbl_obs O ")
            .join(" LEFT JOIN tbl_encounter E ON E.uuid = O.encounteruuid ")
            .joinPlus(" LEFT JOIN tbl_visit V ON V.uuid = E.visituuid ")
            .where(
                " WHERE  V.uuid = '$visitId' AND O.conceptuuid IN ('${Params.MEDICINE.conceptId}', " +
                        "'${Params.PLAN.conceptId}', '${Params.OXYTOCIN.conceptId}', '${Params.IV_FLUID.conceptId}') "
            ).build()
    }
}