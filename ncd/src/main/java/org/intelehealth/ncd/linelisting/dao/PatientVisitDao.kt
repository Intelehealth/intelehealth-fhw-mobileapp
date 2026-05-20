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
        --CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        CAST(strftime('%Y', 'now') - strftime('%Y', P.date_of_birth) - (strftime('%m-%d', 'now') < strftime('%m-%d', P.date_of_birth)) AS INT) AS age,
        
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
              AND VA.visit_attribute_type_uuid = :ncdVisitAttribute
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- visit speciality
        (
            SELECT VA2.value
            FROM tbl_visit_attribute VA2
            WHERE VA2.visit_uuid = V.uuid
              AND VA2.visit_attribute_type_uuid = :visitSpecialityAttribute
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
              AND E.encounter_type_uuid = :chiefComplaintEncounterConceptUuid
              AND O.conceptuuid = :chiefComplaintObsConceptUuid
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
              AND pa.person_attribute_type_uuid = :medicalHistoryAttribute
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    -- latest phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = :patientPhoneNoAttribute
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
        medicalHistoryAttribute: String,
        visitNoteEncounterUuid: String,
        searchQuery: String,
        ncdVisitAttribute: String,
        patientPhoneNoAttribute: String,
        visitSpecialityAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
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
              AND VA.visit_attribute_type_uuid = :ncdVisitAttribute
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- Chief complaint OBS
        O.value AS chief_complaint_data
    FROM tbl_visit V
    LEFT JOIN tbl_encounter E
        ON E.visituuid = V.uuid
        AND E.encounter_type_uuid = :chiefComplaintEncounterConceptUuid
    LEFT JOIN tbl_obs O
        ON O.encounteruuid = E.uuid
        AND O.conceptuuid = :chiefComplaintObsConceptUuid
    WHERE V.patientuuid = :patientUuid
      AND V.enddate IS NOT NULL
      AND (
            SELECT VA.value
            FROM tbl_visit_attribute VA
            WHERE VA.visit_uuid = V.uuid
              AND VA.visit_attribute_type_uuid = :ncdVisitAttribute
            ORDER BY VA.rowid DESC
            LIMIT 1
          ) = 'true'
    ORDER BY substr(V.startdate,1,19) DESC
    """
    )
     suspend fun getNcdCompletedVisitsForProtocolFlags(patientUuid: String,
                                                       ncdVisitAttribute: String,
                                                       chiefComplaintEncounterConceptUuid: String,
                                                       chiefComplaintObsConceptUuid: String): List<PatientVisitDetails>

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
        --CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        CAST(strftime('%Y', 'now') - strftime('%Y', P.date_of_birth) - (strftime('%m-%d', 'now') < strftime('%m-%d', P.date_of_birth)) AS INT) AS age,
        
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
              AND VA.visit_attribute_type_uuid = :ncdVisitAttribute
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- visit speciality
        (
            SELECT VA2.value
            FROM tbl_visit_attribute VA2
            WHERE VA2.visit_uuid = V.uuid
              AND VA2.visit_attribute_type_uuid = :visitSpecialityAttribute
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
              AND E.encounter_type_uuid = :chiefComplaintEncounterConceptUuid
              AND O.conceptuuid = :chiefComplaintObsConceptUuid
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
              AND pa.person_attribute_type_uuid = :medicalHistoryAttribute
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    -- latest phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = :patientPhoneNoAttribute
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
            (:patientUuid IS NOT NULL AND :patientUuid != '' AND P.uuid = :patientUuid)
            
        )
        AND 
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
        medicalHistoryAttribute: String,
        visitNoteEncounterUuid: String,
        patientUuid: String,
        searchQuery: String,
        ncdVisitAttribute: String,
        patientPhoneNoAttribute: String,
        visitSpecialityAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
    ): PatientVisitDetails


    @Query(
        """
    WITH latest_med AS (
        SELECT
            pa.patientuuid AS patientuuid,
            pa.value AS attributeValue,
            pa.person_attribute_type_uuid AS attributeTypeUuid
        FROM tbl_patient_attribute pa
        INNER JOIN (
            SELECT patientuuid, MAX(rowid) AS max_rid
            FROM tbl_patient_attribute
            WHERE person_attribute_type_uuid = :medicalHistoryAttribute
            GROUP BY patientuuid
        ) lm ON lm.patientuuid = pa.patientuuid
            AND lm.max_rid = pa.rowid
            AND pa.person_attribute_type_uuid = :medicalHistoryAttribute
    ),
    latest_phone AS (
        SELECT
            pa.patientuuid AS patientuuid,
            pa.value AS patientPhoneNumber
        FROM tbl_patient_attribute pa
        INNER JOIN (
            SELECT patientuuid, MAX(rowid) AS max_rid
            FROM tbl_patient_attribute
            WHERE person_attribute_type_uuid = :patientPhoneNoAttribute
            GROUP BY patientuuid
        ) lp ON lp.patientuuid = pa.patientuuid
            AND lp.max_rid = pa.rowid
            AND pa.person_attribute_type_uuid = :patientPhoneNoAttribute
    )
    SELECT
        P.uuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,
        P.openmrs_id AS openmrs_id,
        --CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        CAST(strftime('%Y', 'now') - strftime('%Y', P.date_of_birth) - (strftime('%m-%d', 'now') < strftime('%m-%d', P.date_of_birth)) AS INT) AS age,
        lm.attributeValue AS attributeValue,
        lm.attributeTypeUuid AS attributeTypeUuid,
        lp.patientPhoneNumber AS patientPhoneNumber
    FROM tbl_patient P
    LEFT JOIN latest_med lm ON lm.patientuuid = P.uuid
    LEFT JOIN latest_phone lp ON lp.patientuuid = P.uuid
    WHERE
        (:searchQuery IS NULL
            OR :searchQuery = ''
            OR P.first_name LIKE '%' || :searchQuery || '%'
            OR P.middle_name LIKE '%' || :searchQuery || '%'
            OR P.last_name LIKE '%' || :searchQuery || '%'
            OR P.openmrs_id LIKE '%' || :searchQuery || '%'
            OR lp.patientPhoneNumber LIKE '%' || :searchQuery || '%')
    ORDER BY P.rowid ASC
            """
    )
    fun getAllVisitsPagedNew(
        medicalHistoryAttribute: String,
        searchQuery: String,
        patientPhoneNoAttribute: String): PagingSource<Int, PatientVisitDetails>


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
        --CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        CAST(strftime('%Y', 'now') - strftime('%Y', P.date_of_birth) - (strftime('%m-%d', 'now') < strftime('%m-%d', P.date_of_birth)) AS INT) AS age,
        
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
              AND VA.visit_attribute_type_uuid = :ncdVisitAttribute
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,
        
        -- visit speciality
        (
            SELECT VA2.value
            FROM tbl_visit_attribute VA2
            WHERE VA2.visit_uuid = V.uuid
              AND VA2.visit_attribute_type_uuid = :visitSpecialityAttribute
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
              AND E.encounter_type_uuid = :chiefComplaintEncounterConceptUuid
              AND O.conceptuuid = :chiefComplaintObsConceptUuid
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
              AND pa.person_attribute_type_uuid = :medicalHistoryAttribute
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    -- latest phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = :patientPhoneNoAttribute
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
    fun getNcdCompletedVisitsForProtocolFlagsNew(
        medicalHistoryAttribute: String,
        visitNoteEncounterUuid: String,
        searchQuery: String,
        ncdVisitAttribute: String,
        patientPhoneNoAttribute: String,
        visitSpecialityAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
    ): List<PatientVisitDetails>
    @Query("""
    SELECT 
        V.patientuuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,
        P.openmrs_id AS openmrs_id,
        --CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        CAST(strftime('%Y', 'now') - strftime('%Y', P.date_of_birth) - (strftime('%m-%d', 'now') < strftime('%m-%d', P.date_of_birth)) AS INT) AS age,
        
        -- latest OTHER_MEDICAL_HISTORY attribute
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,

        -- visit info
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,

        -- latest phone attribute
        phoneAttr.value AS patientPhoneNumber,

        -- prescription check
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
              AND VA.visit_attribute_type_uuid = :ncdVisitAttribute
            ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit,

        -- visit speciality
        (
            SELECT VA2.value
            FROM tbl_visit_attribute VA2
            WHERE VA2.visit_uuid = V.uuid
              AND VA2.visit_attribute_type_uuid = :visitSpecialityAttribute
            ORDER BY VA2.rowid DESC
            LIMIT 1
        ) AS visit_speciality,

        -- chief complaint
        (
            SELECT O.value
            FROM tbl_obs O
            INNER JOIN tbl_encounter E
                ON O.encounteruuid = E.uuid
            WHERE E.visituuid = V.uuid
              AND E.encounter_type_uuid = :chiefComplaintEncounterConceptUuid
              AND O.conceptuuid = :chiefComplaintObsConceptUuid
            ORDER BY O.rowid DESC
            LIMIT 1
        ) AS chief_complaint_data

    FROM tbl_visit V
    INNER JOIN tbl_patient P ON P.uuid = V.patientuuid

    -- latest OTHER_MEDICAL_HISTORY attribute
    LEFT JOIN tbl_patient_attribute A 
        ON A.uuid = (
            SELECT pa.uuid
            FROM tbl_patient_attribute pa
            WHERE pa.patientuuid = V.patientuuid
              AND pa.person_attribute_type_uuid = :medicalHistoryAttribute
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    -- latest phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = V.patientuuid
              AND pa2.person_attribute_type_uuid = :patientPhoneNoAttribute
            ORDER BY pa2.rowid DESC
            LIMIT 1
        )

    WHERE V.patientuuid IN (:patientUuidsList)
      AND V.enddate IS NOT NULL

    ORDER BY substr(V.startdate,1,19) DESC
""")
    suspend fun getVisitsForPatients(
        patientUuidsList: List<String>,
        medicalHistoryAttribute: String,
        visitNoteEncounterUuid: String,
        ncdVisitAttribute: String,
        patientPhoneNoAttribute: String,
        visitSpecialityAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
    ): List<PatientVisitDetails>


}

