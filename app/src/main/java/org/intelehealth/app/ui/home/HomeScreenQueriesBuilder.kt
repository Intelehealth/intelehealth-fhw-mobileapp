package org.intelehealth.app.ui.home

import org.intelehealth.app.shared.builder.QueryBuilder
import org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE

class HomeScreenQueriesBuilder : QueryBuilder() {
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
            //.where("(o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0")
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
            .where("a.status = 'booked' AND datetime(a.slot_js_date) >= datetime('now', 'localtime')")
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

