package org.intelehealth.ncd.linelisting.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.ncd.model.PatientVisitDetails


@Dao
interface PatientVisitDao {
    @Query(
        """
    SELECT 
        P.uuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,
        P.openmrs_id AS openmrs_id,
        CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        
        -- latest OTHER_MEDICAL_HISTORY attribute
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,

        -- latest completed visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,

        -- latest phone attribute
        phoneAttr.value AS patientPhoneNumber,

        -- prescription check for ENCOUNTER_VISIT_COMPLETE
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM tbl_encounter E 
                WHERE E.visituuid = V.uuid 
                  AND E.encounter_type_uuid = :visitNoteEncounterUuid
            ) THEN 1 
            ELSE 0 
        END AS prescriptionExists,

        -- visit isncd
        (
            SELECT VA.value
            FROM tbl_visit_attribute VA
            WHERE VA.visit_uuid = V.uuid
              AND VA.visit_attribute_type_uuid = 'bc79d2ab-3c83-48f2-820d-08a02b32faab'
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- visit speciality
        (
            SELECT VA2.value
            FROM tbl_visit_attribute VA2
            WHERE VA2.visit_uuid = V.uuid
              AND VA2.visit_attribute_type_uuid = '3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d'
            ORDER BY VA2.rowid DESC
            LIMIT 1
        ) AS visit_speciality,

        -- chief complaint OBS from encounter type 8d5b27bc-c2cc-11de-8d13-0010c6dffd0f
        (
            SELECT O.value
            FROM tbl_obs O
            INNER JOIN tbl_encounter E
                ON O.encounteruuid = E.uuid
            WHERE E.visituuid = V.uuid
              AND E.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f'
              AND O.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'
            ORDER BY O.rowid DESC
            LIMIT 1
        ) AS chief_complaint_data

    FROM tbl_patient P

    -- latest OTHER_MEDICAL_HISTORY attribute
    LEFT JOIN tbl_patient_attribute A 
        ON A.uuid = (
            SELECT pa.uuid
            FROM tbl_patient_attribute pa
            WHERE pa.patientuuid = P.uuid
              AND pa.person_attribute_type_uuid = :attributeTypeUuid
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    -- latest phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = '14d4f066-15f5-102d-96e4-000c29c2a5d7'
            ORDER BY pa2.rowid DESC
            LIMIT 1
        )

    -- latest completed visit (skip non-ended)
    LEFT JOIN tbl_visit V
        ON V.uuid = (
            SELECT v2.uuid
            FROM tbl_visit v2
            WHERE v2.patientuuid = P.uuid
              AND v2.startdate IS NOT NULL
              AND v2.enddate IS NOT NULL
            ORDER BY substr(v2.enddate,1,19) DESC
            LIMIT 1
        )

    WHERE 
        (
            :searchQuery IS NULL
            OR :searchQuery = ''
            OR P.first_name LIKE '%' || :searchQuery || '%'
            OR P.middle_name LIKE '%' || :searchQuery || '%'
            OR P.last_name LIKE '%' || :searchQuery || '%'
            OR P.openmrs_id LIKE '%' || :searchQuery || '%'
            OR phoneAttr.value LIKE '%' || :searchQuery || '%'
        )

    ORDER BY datetime(V.startdate) DESC
    """
    )
    fun getAllVisitsPaged(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String,
        searchQuery: String
    ): PagingSource<Int, PatientVisitDetails>

    @Query(
        """
    SELECT 
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,
        V.patientuuid AS patientId,
        
        -- NCD visit flag
        (
            SELECT VA.value
            FROM tbl_visit_attribute VA
            WHERE VA.visit_uuid = V.uuid
              AND VA.visit_attribute_type_uuid = 'bc79d2ab-3c83-48f2-820d-08a02b32faab'
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- Chief complaint OBS
        O.value AS chief_complaint_data
    FROM tbl_visit V
    LEFT JOIN tbl_encounter E
        ON E.visituuid = V.uuid
        AND E.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f'
    LEFT JOIN tbl_obs O
        ON O.encounteruuid = E.uuid
        AND O.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'
    WHERE V.patientuuid = :patientUuid
      AND V.enddate IS NOT NULL
      AND (
            SELECT VA.value
            FROM tbl_visit_attribute VA
            WHERE VA.visit_uuid = V.uuid
              AND VA.visit_attribute_type_uuid = 'bc79d2ab-3c83-48f2-820d-08a02b32faab'
            ORDER BY VA.rowid DESC
            LIMIT 1
          ) = 'true'
    ORDER BY substr(V.startdate,1,19) DESC
    """
    )
     suspend fun getNcdCompletedVisitsForProtocolFlags(patientUuid: String): List<PatientVisitDetails>

    @Query(
        """
    SELECT 
        P.uuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,
        P.openmrs_id AS openmrs_id,
        CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        
        -- latest OTHER_MEDICAL_HISTORY attribute
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,

        -- latest completed visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,

        -- latest phone attribute
        phoneAttr.value AS patientPhoneNumber,

        -- prescription check for ENCOUNTER_VISIT_COMPLETE
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM tbl_encounter E 
                WHERE E.visituuid = V.uuid 
                  AND E.encounter_type_uuid = :visitNoteEncounterUuid
            ) THEN 1 
            ELSE 0 
        END AS prescriptionExists,

        -- visit isncd
        (
            SELECT VA.value
            FROM tbl_visit_attribute VA
            WHERE VA.visit_uuid = V.uuid
              AND VA.visit_attribute_type_uuid = 'bc79d2ab-3c83-48f2-820d-08a02b32faab'
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- visit speciality
        (
            SELECT VA2.value
            FROM tbl_visit_attribute VA2
            WHERE VA2.visit_uuid = V.uuid
              AND VA2.visit_attribute_type_uuid = '3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d'
            ORDER BY VA2.rowid DESC
            LIMIT 1
        ) AS visit_speciality,

        -- chief complaint OBS from encounter type 8d5b27bc-c2cc-11de-8d13-0010c6dffd0f
        (
            SELECT O.value
            FROM tbl_obs O
            INNER JOIN tbl_encounter E
                ON O.encounteruuid = E.uuid
            WHERE E.visituuid = V.uuid
              AND E.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f'
              AND O.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'
            ORDER BY O.rowid DESC
            LIMIT 1
        ) AS chief_complaint_data

    FROM tbl_patient P

    -- latest OTHER_MEDICAL_HISTORY attribute
    LEFT JOIN tbl_patient_attribute A 
        ON A.uuid = (
            SELECT pa.uuid
            FROM tbl_patient_attribute pa
            WHERE pa.patientuuid = P.uuid
              AND pa.person_attribute_type_uuid = :attributeTypeUuid
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    -- latest phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = '14d4f066-15f5-102d-96e4-000c29c2a5d7'
            ORDER BY pa2.rowid DESC
            LIMIT 1
        )

    -- latest completed visit (skip non-ended)
    LEFT JOIN tbl_visit V
        ON V.uuid = (
            SELECT v2.uuid
            FROM tbl_visit v2
            WHERE v2.patientuuid = P.uuid
              AND v2.startdate IS NOT NULL
              AND v2.enddate IS NOT NULL
            ORDER BY substr(v2.enddate,1,19) DESC
            LIMIT 1
        )

    WHERE 
         (
            :searchQuery IS NULL
            OR :searchQuery = ''
            OR P.first_name LIKE '%' || :searchQuery || '%'
            OR P.middle_name LIKE '%' || :searchQuery || '%'
            OR P.last_name LIKE '%' || :searchQuery || '%'
            OR P.openmrs_id LIKE '%' || :searchQuery || '%'
            OR phoneAttr.value LIKE '%' || :searchQuery || '%'
        )

    ORDER BY datetime(V.startdate) DESC
    """
    )
    fun getPatientAndLatestVisitDetailsByPatientId(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String,
        searchQuery: String
    ): PatientVisitDetails

}

