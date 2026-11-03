package org.intelehealth.app.ui.home

import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.shared.builder.QueryBuilder
import org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE

class HomeScreenQueriesBuilder : QueryBuilder() {
   /* fun getReceivedPrescriptionVisitsCount(): String {
        return select("COUNT(DISTINCT p.openmrs_id)")
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid")
            .join("tbl_encounter e ON v.uuid = e.visituuid")
            .join("tbl_obs o ON e.uuid = o.encounteruuid")
//            .where(
//                "e.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}' " +
//                        "AND (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
//                        "AND o.voided = 0"
//            )
            //changed for pres count not updated correctly in homescreen from IDA-development_master
            .where(
                "e.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}' " +
                        "AND (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +
                        "AND (CASE " +
                        "WHEN EXISTS ( " +
                        "SELECT 1 FROM tbl_encounter e1  " +
                        "WHERE e1.visituuid = v.uuid  " +
                        "AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30'  ) THEN 1 ELSE 0 END) = 0 " +
                        "AND (CASE  " +
                        "WHEN EXISTS ( " +
                        "SELECT 1 FROM tbl_encounter e2  " +
                        "WHERE e2.visituuid = v.uuid  " +
                        "AND e2.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}') THEN 1 ELSE 0 END) = 1"
            )
            .build()
    }
    fun getPendingPrescriptionVisitsCount(): String {
        return select("COUNT(DISTINCT p.openmrs_id) AS total_count")
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid")
            .join("tbl_encounter e ON v.uuid = e.visituuid")
            .join("tbl_obs o ON e.uuid = o.encounteruuid")
//            .where("(o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0")

            // changed for pres count not updated correctly in homescreen from IDA-development_master
            .where(
                "(o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +
                        "AND (CASE " +
                        "WHEN EXISTS ( " +
                        "SELECT 1 FROM tbl_encounter e1  " +
                        "WHERE e1.visituuid = v.uuid  " +
                        "AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30'  ) THEN 1 ELSE 0 END) = 0 " +
                        "AND (CASE  " +
                        "WHEN EXISTS ( " +
                        "SELECT 1 FROM tbl_encounter e2  " +
                        "WHERE e2.visituuid = v.uuid  " +
                        "AND e2.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}') THEN 1 ELSE 0 END) = 0"
            )
            .build()
    }*/

    fun getReceivedPrescriptionVisitsCount(): String {
        return select("COUNT(DISTINCT p.openmrs_id)")
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid")
            .join("tbl_encounter e ON v.uuid = e.visituuid")
            .join("tbl_obs o ON e.uuid = o.encounteruuid")
            .where(
                "e.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}' " +
                        "AND (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +
                        "AND NOT EXISTS ( " +
                        "   SELECT 1 FROM tbl_encounter e1 " +
                        "   WHERE e1.visituuid = v.uuid " +
                        "   AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' " +
                        ") " +
                        "AND EXISTS ( " +
                        "   SELECT 1 FROM tbl_encounter e2 " +
                        "   WHERE e2.visituuid = v.uuid " +
                        "   AND e2.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}' " +
                        ")"
            )
            .build()
    }
    fun getPendingPrescriptionVisitsCount(): String {
        return select("COUNT(DISTINCT p.openmrs_id) AS total_count")
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid")
            .join("tbl_encounter e ON v.uuid = e.visituuid")
            .join("tbl_obs o ON e.uuid = o.encounteruuid")
            .where(pendingPrescriptionWhere())
            .build()
    }

    /**
     * Count for the Prescriptions screen's Pending tab (tab label + "doctor is yet to send" banner).
     *
     * Same as [getPendingPrescriptionVisitsCount] but also leaves out visits referred to a
     * specialist. After the doctor shares the initial prescription with a referral, the visit
     * moves to the Referrals tab and VisitPendingFragment's list hides it (has_referral = 0);
     * without this filter the Pending label kept counting it until the final prescription
     * (visit-complete encounter) arrived.
     *
     * The Home card and the Received tab's "awaiting" banner keep [getPendingPrescriptionVisitsCount]
     * on purpose: a referred visit is still awaiting its final prescription.
     */
    fun getPendingTabVisitsCount(): String {
        return select("COUNT(DISTINCT p.openmrs_id) AS total_count")
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid")
            .join("tbl_encounter e ON v.uuid = e.visituuid")
            .join("tbl_obs o ON e.uuid = o.encounteruuid")
            .where(pendingPrescriptionWhere() + " AND NOT " + EncounterDAO.specialistReferralSql("v.uuid"))
            .build()
    }

    // Open visit (no exit survey) with no final prescription (visit-complete encounter) yet.
    private fun pendingPrescriptionWhere(): String {
        return "(o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                "AND o.voided = 0 " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM tbl_encounter e1 " +
                "   WHERE e1.visituuid = v.uuid " +
                "   AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' " +
                ") " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM tbl_encounter e2 " +
                "   WHERE e2.visituuid = v.uuid " +
                "   AND e2.encounter_type_uuid = '${ENCOUNTER_VISIT_COMPLETE}' " +
                ")"
    }


    fun getRecentNotEndedVisitsQuery(): String {
        return select(
            """
        p.uuid, v.uuid AS visitUUID, p.patient_photo, p.first_name, p.middle_name, 
        p.last_name, p.phone_number, p.date_of_birth, p.gender, p.openmrs_id, v.startdate
        """.trimIndent()
        )
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid") // Ensure valid JOIN syntax
            .where(
                """
            (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') 
            AND v.voided = 0 
            AND v.startdate > DATETIME('now', '-4 days')  -- Fix duration format
            AND v.enddate IS NULL
            """.trimIndent()
            )
            .orderBy("v.startdate DESC")
            .build()
    }

    fun getUpcomingAppointmentsCount(): String {
        return select("COUNT(*)")
            .from("tbl_patient p")
            .join("tbl_appointments a ON p.uuid = a.patient_id")
            .where("a.status = 'booked' AND datetime(a.slot_js_date) >= datetime('now')")
            .build()
    }
    fun getPastAppointmentsCount(): String {
        return select("COUNT(*)")
            .from("tbl_patient p")
            .join("tbl_appointments a ON p.uuid = a.patient_id")
            .where("datetime(a.slot_js_date) < datetime('now')")
            .build()
    }
    fun getOlderNotEndedVisits(): String {
        return select(
            "p.uuid, v.uuid AS visitUUID, p.patient_photo, p.first_name, p.middle_name, " +
                    "p.last_name, p.phone_number, p.date_of_birth, p.gender, p.openmrs_id, v.startdate"
        )
            .from("tbl_patient p")
            .join("tbl_visit v ON p.uuid = v.patientuuid")
            .where("(v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true')")
            .where("v.voided = 0")
            .where("v.startdate < DATETIME('now', '-4 day')")
            .where("v.enddate IS NULL")
            .orderBy("v.startdate DESC")
            .build()
    }

}

