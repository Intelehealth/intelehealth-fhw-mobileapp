package org.intelehealth.ezazi.builder;

import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;

import android.util.Log;

import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.VisitDTO;
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
                .orderBy("P.first_name")
                .orderIn("ASC")
                .build();
        Log.e(TAG, "searchQuery => " + query);
        return query;
    }

    public String pagingQuery(int offset, int limit) {
        String query = selectQuery()
                .groupBy("P.uuid")
                .orderBy("P.first_name")
                .orderIn("ASC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "pagingQuery => " + query);
        return query;
    }

    private QueryBuilder selectQuery() {
        return select("P.uuid, P.openmrs_id, P.first_name, P.last_name, P.middle_name, P.date_of_birth, " +
                "CASE WHEN P.middle_name IS NULL THEN P.first_name || ' ' || P.last_name " +
                "ELSE P.first_name || ' ' || P.middle_name || ' ' || P.last_name " +
                "END fullName, " +
                "(SELECT CASE " +
                "WHEN O.value LIKE '%discharge%'  THEN '" + VisitDTO.CompletedStatus.DAMA.value + "' " +
                "WHEN O.value LIKE '%Refer%'  THEN '" + VisitDTO.CompletedStatus.RTOH.value + "' " +
                "WHEN O.conceptuuid = '" + UuidDictionary.OUT_OF_TIME + "' THEN '" + VisitDTO.CompletedStatus.OUT_OF_TIME.value + "' " +
                "ELSE O.value " +
                "END outcome " +
                "FROM tbl_encounter E, tbl_obs O " +
                "WHERE E.visituuid =V.uuid  and E.voided = '0' and O.encounteruuid = E.uuid " +
                "AND O.conceptuuid IN ('" + UuidDictionary.BIRTH_OUTCOME + "', '"
                + UuidDictionary.REFER_TYPE + "','" + UuidDictionary.OUT_OF_TIME + "') LIMIT 1) as birthStatus, " +
                "(SELECT CASE " +
                "WHEN U.name LIKE '%Stage1%'  THEN 'Stage1' " +
                "WHEN U.name LIKE '%Stage2%'  THEN 'Stage2' ELSE U.name " +
                "END Stage FROM tbl_encounter E, tbl_uuid_dictionary U " +
                "WHERE E.visituuid =V.uuid  and E.voided = '0'  and U.uuid = E.encounter_type_uuid  ORDER BY U.name DESC LIMIT 1)  as stage, " +
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
//        SELECT V.uuid as visitUuid, V.enddate, V. startdate, V.patientuuid, V.sync, P.gender, P.openmrs_id, P.first_name, P.last_name, P.middle_name, P.date_of_birth, PA.value as bedNo,
//        (SELECT CASE  WHEN U.name LIKE '%Stage1%'  THEN 'Stage1'  WHEN U.name LIKE '%Stage2%'  THEN 'Stage2' ELSE ''  END Stage FROM tbl_encounter E, tbl_uuid_dictionary U
//        WHERE E.visituuid =V.uuid  and E.voided = '0'  and U.uuid = E.encounter_type_uuid   ORDER BY U.name DESC LIMIT 1)  as stage,
//        CASE PA.person_attribute_type_uuid WHEN '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END phoneNumber,
//                (SELECT uuid FROM tbl_encounter where visituuid = V.uuid and voided = '0' AND encounter_type_uuid != 'bd1fbfaa-f5fb-4ebd-b75c-564506fc309e' ORDER BY encounter_time DESC limit 1) as latestEncounterId
//        FROM tbl_visit  V
//        LEFT OUTER JOIN tbl_patient P ON P.uuid = V.patientuuid
//        LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid
//        AND PA.person_attribute_type_uuid IN ((SELECT uuid FROM tbl_patient_attribute_master  WHERE name = 'Bed Number'), '14d4f066-15f5-102d-96e4-000c29c2a5d7')
//        WHERE V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='bd1fbfaa-f5fb-4ebd-b75c-564506fc309e' ) AND V.voided = '0' AND V.creator = '90bde499-9548-4ab0-8849-8f288ad82109'  GROUP BY V.uuid  ORDER BY V.modified_date DESC

//        String query = select("V.uuid as visitUuid, " +
//                "V.enddate, V. startdate, V.patientuuid," +
//                "P.openmrs_id, V.sync, P.gender," +
//                "P.first_name, " +
//                "P.last_name, " +
//                "P.middle_name, " +
//                "P.date_of_birth, " +
//                "CASE WHEN PA.person_attribute_type_uuid  != '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END bedNo, " +
//                "CASE PA.person_attribute_type_uuid WHEN '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END phoneNumber, " +
//                "(SELECT uuid FROM tbl_encounter where visituuid = V.uuid and voided = '0' AND encounter_type_uuid != '" + ENCOUNTER_VISIT_COMPLETE + "' ORDER BY encounter_time DESC limit 1) as latestEncounterId, " +
//                "(SELECT CASE " +
//                " WHEN U.name LIKE '%Stage1%' THEN 'Stage-1' " +
//                " WHEN U.name LIKE '%Stage2%' THEN 'Stage-2' ELSE '' " +
//                " END Stage FROM tbl_encounter E, tbl_uuid_dictionary U  " +
//                " WHERE E.visituuid =V.uuid  and E.voided = '0'  and U.uuid = E.encounter_type_uuid  " +
//                " ORDER BY U.name DESC LIMIT 1)  as stage")
//                .from("tbl_visit  V")
//                .join("LEFT OUTER JOIN tbl_patient P ON P.uuid = V.patientuuid " +
//                        "LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid " +
//                        " AND PA.person_attribute_type_uuid = (SELECT uuid FROM tbl_patient_attribute_master " +
//                        " WHERE name = '" + PatientAttributesDTO.Columns.BED_NUMBER.value + "')")
//                .where("V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
//                        "AND V.voided = '0' AND V.creator = '" + myCreatorUUID + "'")
//                .groupBy("V.uuid")
//                .orderBy("V.modified_date")
//                .orderIn("DESC")
//                .limit(limit)
//                .offset(offset)
//                .build();
        String query = select("V.uuid as visitUuid, " +
                "V.enddate, V. startdate, V.patientuuid," +
                "P.openmrs_id, V.sync, P.gender," +
                "P.first_name, " +
                "P.last_name, " +
                "P.middle_name, " +
                "P.date_of_birth, " +
                "CASE WHEN PA.person_attribute_type_uuid  != '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END bedNo, " +
                "CASE PA.person_attribute_type_uuid WHEN '14d4f066-15f5-102d-96e4-000c29c2a5d7' THEN PA.value END phoneNumber, " +
                "(SELECT uuid FROM tbl_encounter where visituuid = V.uuid and voided IN ('0', 'false', 'FALSE') AND encounter_type_uuid != '" + ENCOUNTER_VISIT_COMPLETE + "' ORDER BY encounter_time DESC limit 1) as latestEncounterId, " +
                "(SELECT CASE " +
                " WHEN U.name LIKE '%Stage1%' THEN 'Stage-1' " +
                " WHEN U.name LIKE '%Stage2%' THEN 'Stage-2' ELSE '' " +
                " END Stage FROM tbl_encounter E, tbl_uuid_dictionary U  " +
                " WHERE E.visituuid =V.uuid  and E.voided IN ('0', 'false', 'FALSE')  and U.uuid = E.encounter_type_uuid  " +
                " ORDER BY U.name DESC LIMIT 1)  as stage")
                .from("tbl_visit  V")
                .join("LEFT OUTER JOIN tbl_patient P ON P.uuid = V.patientuuid " +
                        " LEFT OUTER JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid " +
                        " LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid " +
                        " AND PA.person_attribute_type_uuid = (SELECT uuid FROM tbl_patient_attribute_master " +
                        " WHERE name = '" + PatientAttributesDTO.Columns.BED_NUMBER.value + "')")
                .where("V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided IN ('0', 'false', 'FALSE') AND VA.value = '" + providerId + "'" +
                        " AND V.enddate IS NULL ")
                .groupBy("V.uuid")
                .orderBy("V.startdate")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "activePatientQuery => " + query);
        return query;
    }
}
