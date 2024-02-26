package org.intelehealth.ezazi.builder;

import static org.intelehealth.ezazi.utilities.UuidDictionary.DECISION_PENDING;
import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;

import android.util.Log;

import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.ui.visit.model.CompletedVisitStatus;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;

/**
 * Created by Vaghela Mithun R. on 25-06-2023 - 10:58.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PatientQueryBuilder extends QueryBuilder {
    public static final String TAG = "PatientQueryBuilder";

    public String searchQuery(String keyword) {
        String query = selectQuery()
                .where(" LOWER(fullName) LIKE LOWER('%" + keyword + "%') "
                        + "OR LOWER(P.openmrs_id) LIKE LOWER('%" + keyword + "%') "
                        + "OR LOWER(birthStatus) LIKE LOWER('%" + keyword + "%') ")
                .groupBy("P.uuid")
                .orderBy("P.dateCreated")
                .orderIn("DESC")
                .build();
        Log.e(TAG, "searchQuery => " + query);
        return query;
    }

    public String pagingQuery(int offset, int limit) {
        String query = selectQuery()
                .groupBy("P.uuid")
                .orderBy("P.dateCreated")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "pagingQuery => " + query);
        return query;
    }

    private QueryBuilder selectQuery() {
        return select("P.uuid, P.openmrs_id, P.first_name, P.last_name, P.middle_name, P.date_of_birth, P.dateCreated," +
                "CASE WHEN P.middle_name IS NULL THEN P.first_name || ' ' || P.last_name " +
                "ELSE P.first_name || ' ' || P.middle_name || ' ' || P.last_name " +
                "END fullName, " + getCompletedVisitStatusCase() + getCurrentStageCase() + ", " + caseOfMotherDeceased() +
                "(select count(uuid) from tbl_encounter where visituuid = V.uuid) as alertCount, " +
                "CASE PA.person_attribute_type_uuid WHEN '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END phoneNumber, " +
                "CASE WHEN PA.person_attribute_type_uuid  != '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END bedNo ")
                .from("tbl_patient P")
                .join("LEFT OUTER JOIN tbl_visit V ON P.uuid = V.patientuuid " +
                        "LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid " +
                        "AND PA.person_attribute_type_uuid IN ((SELECT uuid FROM tbl_patient_attribute_master " +
                        "WHERE name = '" + PatientAttributesDTO.Columns.BED_NUMBER.value + "'), '14d4f066-15f5-102d-96e4-000c29c2a5d7')");
    }

    public String activeVisitsQuery(int offset, int limit) {
        String providerId = new SessionManager(IntelehealthApplication.getAppContext()).getProviderID();
        String query = select("V.uuid as visitUuid, " +
                "V.enddate, V. startdate, V.patientuuid," +
                "P.openmrs_id, V.sync, P.gender," +
                "P.first_name, " +
                "P.last_name, " +
                "P.middle_name, " +
                "P.date_of_birth, " +
                "CASE WHEN PA.person_attribute_type_uuid  != '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END bedNo, " +
                "CASE PA.person_attribute_type_uuid WHEN '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END phoneNumber, " +
                "(SELECT uuid FROM tbl_encounter where visituuid = V.uuid and voided IN ('0', 'false', 'FALSE') " +
                "AND encounter_type_uuid != '" + ENCOUNTER_VISIT_COMPLETE + "' ORDER BY encounter_time DESC limit 1) " +
                "as latestEncounterId,  (SELECT value FROM tbl_visit_attribute where " +
                "visit_attribute_type_uuid ='" + DECISION_PENDING + "' AND visit_uuid = V.uuid) as outcomePending, " +
                getCurrentStageCase())
                .from("tbl_visit  V")
                .join("LEFT OUTER JOIN tbl_patient P ON P.uuid = V.patientuuid " +
                        " LEFT OUTER JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid " +
                        " LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid " +
                        " AND PA.person_attribute_type_uuid = (SELECT uuid FROM tbl_patient_attribute_master " +
                        " WHERE name = '" + PatientAttributesDTO.Columns.BED_NUMBER.value + "')")
                .where("V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" +
                        ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided IN ('0', 'false', 'FALSE') AND VA.value = '" + providerId + "'" +
                        " AND outcomePending = 'false'  AND  (V.enddate IS NULL OR  V.enddate = '')")
                .groupBy("V.uuid")
                .orderBy("V.startdate")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "activePatientQuery => " + query);
        return query;
    }

    private String getCompletedVisitStatusCase() {

        return "(SELECT CASE " +
                // start refer type case
                "WHEN O.conceptuuid = '" + CompletedVisitStatus.ReferType.conceptUuid() + "' THEN " +
                "(CASE WHEN O.value = '" + CompletedVisitStatus.ReferType.REFER_TO_OTHER.value() +
                "' THEN '" + CompletedVisitStatus.ReferType.REFER_TO_OTHER.sortValue() + "'" +
                " WHEN O.value = '" + CompletedVisitStatus.ReferType.SELF_DISCHARGE.value() +
                "' THEN '" + CompletedVisitStatus.ReferType.SELF_DISCHARGE.sortValue() + "'" +
                " WHEN O.value = '" + CompletedVisitStatus.ReferType.REFER_TO_ICU.value() +
                "' THEN '" + CompletedVisitStatus.ReferType.REFER_TO_ICU.sortValue() + "'" +
                " WHEN O.value = '" + CompletedVisitStatus.ReferType.SHIFT_TO_C_SECTION.value() +
                "' THEN '" + CompletedVisitStatus.ReferType.SHIFT_TO_C_SECTION.sortValue() + "'" +
                " WHEN O.value = '" + CompletedVisitStatus.ReferType.OTHER.value() +
                "' THEN '" + CompletedVisitStatus.ReferType.OTHER.sortValue() + "'" +
                "ELSE O.value END) " +
                // end refer type case
                // start birth outcome case
                "WHEN O.conceptuuid = '" + CompletedVisitStatus.Labour.conceptUuid() + "' THEN O.value " +
                // end birth outcome case
                // start out of time case
                "WHEN O.conceptuuid = '" + CompletedVisitStatus.OutOfTime.OUT_OF_TIME.uuid() + "' THEN '" + CompletedVisitStatus.OutOfTime.OUT_OF_TIME.value() + "' " +
                // end out of time case
                "ELSE O.value " +
                "END outcome " +
                "FROM tbl_encounter E, tbl_obs O " +
                "WHERE E.visituuid =V.uuid  and E.voided = '0' and O.encounteruuid = E.uuid " +
                "AND O.conceptuuid IN ('" + UuidDictionary.BIRTH_OUTCOME + "', " +
                "'" + UuidDictionary.REFER_TYPE + "','" + UuidDictionary.OUT_OF_TIME + "') LIMIT 1) as birthStatus, ";
    }

    private String caseOfMotherDeceased() {
        return " (SELECT O.value " +
                "FROM tbl_encounter E, tbl_obs O " +
                "WHERE E.visituuid =V.uuid  and E.voided = '0' and O.encounteruuid = E.uuid " +
                "AND O.conceptuuid = '" + UuidDictionary.MOTHER_DECEASED_FLAG + "' LIMIT 1) as motherDeceased, ";
    }

    private String getCurrentStageCase() {
        return "(SELECT CASE " +
                "WHEN U.name LIKE '%Stage1%'  THEN 'Stage-1' " +
                "WHEN U.name LIKE '%Stage2%'  THEN 'Stage-2' ELSE U.name " +
                "END Stage FROM tbl_encounter E, tbl_uuid_dictionary U " +
                "WHERE E.visituuid =V.uuid  and E.voided = '0'  and U.uuid = E.encounter_type_uuid  ORDER BY U.name DESC LIMIT 1)  as stage ";
    }

    public String upcomingPatientQuery(int offset, int limit) {
//        0c812a8b-a65e-4891-9a5c-ebb9d401344b
        String creatorId = new SessionManager(IntelehealthApplication.getAppContext()).getCreatorID();

        String query = select("uuid, openmrs_id, dateCreated," +
                "CASE WHEN middle_name IS NULL THEN first_name || ' ' || last_name " +
                "ELSE first_name || ' ' || middle_name || ' ' || last_name END fullName")
                .from(" tbl_patient ")
                .where(" uuid NOT IN (Select patientuuid from tbl_visit where creator = '" + creatorId + "')" +
                        " AND creatoruuid = '" + creatorId + "'")
                .groupBy("uuid")
                .orderBy("dateCreated")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "upcomingPatientQuery => " + query);
        return query;
    }

    public String outcomePendingPatientQuery(int offset, int limit, String providerId) {
        String query = select("P.date_of_birth, P.uuid, V.enddate, V. startdate,V.uuid as visitId, P.openmrs_id, P.dateCreated, " +
                "CASE WHEN middle_name IS NULL THEN first_name || ' ' || last_name " +
                "ELSE first_name || ' ' || middle_name || ' ' || last_name END fullName, " + getCurrentStageCase() + "," +
                "(SELECT value FROM tbl_visit_attribute where visit_attribute_type_uuid = '" + DECISION_PENDING + "' AND visit_uuid = V.uuid) as outcomePending")
                .from(" tbl_visit V ")
                .join(" LEFT JOIN tbl_patient P ON P.uuid = V.patientuuid ")
                .joinPlus(" LEFT JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid ")
                .where(" V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided IN ('0', 'false', 'FALSE') AND VA.value = '" + providerId + "' AND outcomePending = 'true'")
                .groupBy(" V.uuid ")
                .orderBy(" P.dateCreated ")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "outcomePendingPatientQuery => " + query);
        return query;
    }

    public String outcomePendingPatientCountQuery(String providerId) {
        String query = select("COUNT(*) as record")
                .from(" tbl_visit V ")
                .join(" LEFT JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid ")
                .where(" V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided IN ('0', 'false', 'FALSE') AND VA.value = '" + providerId + "' " +
                        "AND (SELECT value FROM tbl_visit_attribute where visit_attribute_type_uuid = '" + DECISION_PENDING + "' AND visit_uuid = V.uuid) = 'true'")
                .build();
        Log.e(TAG, "outcomePendingPatientCountQuery => " + query);
        return query;
    }

    public String completedVisitPatientQuery(int offset, int limit, String providerId) {
        String query = select("V.uuid as visitId, P.openmrs_id, P.dateCreated, " +
                "CASE WHEN middle_name IS NULL THEN first_name || ' ' || last_name " +
                "ELSE first_name || ' ' || middle_name || ' ' || last_name END fullName, " +
                "(SELECT O.value FROM tbl_encounter E, tbl_obs O WHERE E.visituuid =V.uuid  and E.voided = '0' and O.encounteruuid = E.uuid " +
                "AND O.conceptuuid IN ('" + UuidDictionary.BIRTH_OUTCOME + "', '" + UuidDictionary.REFER_TYPE + "') limit 1) as birthStatus, " +
                "(SELECT O.value FROM tbl_encounter E, tbl_obs O WHERE E.visituuid =V.uuid  and E.voided = '0' and O.encounteruuid = E.uuid " +
                "AND O.conceptuuid='" + UuidDictionary.MOTHER_DECEASED_FLAG + "' limit 1) as motherDeceased ")
                .from(" tbl_visit V ")
                .join(" LEFT JOIN tbl_patient P ON P.uuid = V.patientuuid ")
                .joinPlus(" LEFT JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid ")
                .where(" V.uuid IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided IN ('0', 'false', 'FALSE') AND VA.value = '" + providerId + "' ")
                .groupBy(" V.uuid ")
                .orderBy(" P.dateCreated ")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "completedVisitPatientQuery => " + query);
        return query;
    }

    public String outcomePendingStatusQuery(String visitId) {
        String query = select(" value ")
                .from(" tbl_visit_attribute ")
                .where(" visit_attribute_type_uuid = '" + DECISION_PENDING + "' AND visit_uuid = '" + visitId + "' AND value = 'true'")
                .build();
        return query;
    }
}
