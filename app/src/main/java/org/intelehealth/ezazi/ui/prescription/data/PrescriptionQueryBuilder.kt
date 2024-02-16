package org.intelehealth.ezazi.ui.prescription.data

import org.intelehealth.ezazi.builder.QueryBuilder
import org.intelehealth.ezazi.partogram.PartogramConstants.Params

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 16:13.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionQueryBuilder : QueryBuilder() {
    fun buildPrescriptionQuery(visitId: String, creatorId: String): String {
        return select("O.uuid, O.conceptuuid, O.value, O.created_date, O.creator, P.given_name ")
                .from(" tbl_obs O ")
                .join(" LEFT JOIN tbl_encounter E ON E.uuid = O.encounteruuid ")
                .joinPlus(" LEFT JOIN tbl_visit V ON V.uuid = E.visituuid ")
                .joinPlus(" LEFT JOIN tbl_provider P ON P.useruuid = O.creatoruuid AND P.role = 'Organizational: Doctor'")
                .where(
                        " V.uuid = '$visitId' AND P.role != 'Organizational: Nurse' " +
                                "AND O.voided = '0' AND O.conceptuuid IN ('" +
                                "${Params.PRESCRIBED_MEDICINE.conceptId}', " +
                                "'${Params.PLAN.conceptId}', " +
                                "'${Params.PRESCRIBED_OXYTOCIN.conceptId}', " +
                                "'${Params.PRESCRIBED_IV_FLUID.conceptId}') "
                ).build()
    }

    fun buildPlansPrescriptionQuery(visitId: String, creatorId: String): String {
        return select("O.uuid, O.conceptuuid, O.value, O.created_date, O.creator, P.given_name ")
                .from(" tbl_obs O ")
                .join(" LEFT JOIN tbl_encounter E ON E.uuid = O.encounteruuid ")
                .joinPlus(" LEFT JOIN tbl_visit V ON V.uuid = E.visituuid ")
                .joinPlus(" LEFT JOIN tbl_provider P ON P.useruuid = O.creatoruuid AND P.role = 'Organizational: Doctor'")
                .where(
                        " V.uuid = '$visitId' AND P.role != 'Organizational: Nurse' " +
                                "AND O.voided = '0' AND O.conceptuuid IN (" +
                                "'${Params.PLAN.conceptId}') "
                ).build()
    }

    fun buildAssessmentPrescriptionQuery(visitId: String, creatorId: String): String {
        return select("O.uuid, O.conceptuuid, O.value, O.created_date, O.creator, P.given_name ")
                .from(" tbl_obs O ")
                .join(" LEFT JOIN tbl_encounter E ON E.uuid = O.encounteruuid ")
                .joinPlus(" LEFT JOIN tbl_visit V ON V.uuid = E.visituuid ")
                .joinPlus(" LEFT JOIN tbl_provider P ON P.useruuid = O.creatoruuid AND P.role = 'Organizational: Doctor'")
                .where(
                        " V.uuid = '$visitId' AND P.role != 'Organizational: Nurse' " +
                                "AND O.voided = '0' AND O.conceptuuid IN (" +
                                "'${Params.ASSESSMENT.conceptId}') "
                ).build()
    }
}