package org.intelehealth.ncd.room.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.model.Visit

@Dao
interface VisitDao {

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

        -- latest patient attribute
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,

        -- latest visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,

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

        -- visit speciality
        (
            SELECT VA.value
            FROM tbl_visit_attribute VA
            WHERE VA.visit_uuid = V.uuid
              AND VA.visit_attribute_type_uuid = 'bc79d2ab-3c83-48f2-820d-08a02b32faab'
               ORDER BY VA.rowid DESC
            LIMIT 1
        ) AS is_ncd_visit

    FROM tbl_patient P

    LEFT JOIN tbl_patient_attribute A 
        ON A.uuid = (
            SELECT pa.uuid
            FROM tbl_patient_attribute pa
            WHERE pa.patientuuid = P.uuid
              AND pa.person_attribute_type_uuid = :attributeTypeUuid
            ORDER BY pa.rowid DESC
            LIMIT 1
        )

    LEFT JOIN tbl_visit V
        ON V.uuid = (
            SELECT v2.uuid
            FROM tbl_visit v2
            WHERE v2.patientuuid = P.uuid
              AND v2.startdate IS NOT NULL
                ORDER BY substr(startdate, 1, 19) DESC
            LIMIT 1
        )

WHERE (julianday('now') - julianday(P.date_of_birth)) / 365.25 >= :age
    ORDER BY datetime(V.startdate) DESC;
    """
    )
    suspend fun getPatientVisitRawData(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String
    ): List<PatientVisitDetails>


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
        -- calculated age (in years)
        --CAST((julianday('now') - julianday(P.date_of_birth)) / 365.25 AS INT) AS age,
        CAST(strftime('%Y', 'now') - strftime('%Y', P.date_of_birth) - (strftime('%m-%d', 'now') < strftime('%m-%d', P.date_of_birth)) AS INT) AS age,
        -- latest patient attribute
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,

        -- latest visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,
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

        -- next follow up date (raw obs value)
        (
            SELECT O.value
            FROM tbl_obs O
            WHERE O.encounteruuid = (
                SELECT E.uuid
                FROM tbl_encounter E
                WHERE E.visituuid = V.uuid
                ORDER BY E.rowid DESC
                LIMIT 1
            )
              AND O.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'
            ORDER BY O.rowid DESC
            LIMIT 1
        ) AS chief_complaint_data

    FROM tbl_patient P

    LEFT JOIN tbl_patient_attribute A 
        ON A.uuid = (
            SELECT pa.uuid
            FROM tbl_patient_attribute pa
            WHERE pa.patientuuid = P.uuid
              AND pa.person_attribute_type_uuid = :attributeTypeUuid
            ORDER BY pa.rowid DESC
            LIMIT 1
        )
        
         -- join for phone attribute
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = '14d4f066-15f5-102d-96e4-000c29c2a5d7'
            ORDER BY pa2.rowid DESC
            LIMIT 1
        )

LEFT JOIN tbl_visit V
    ON V.uuid = (
        SELECT v2.uuid
        FROM tbl_visit v2
        WHERE v2.patientuuid = P.uuid
          AND v2.startdate IS NOT NULL
          AND v2.enddate IS NOT NULL   -- fetch only completed visits
        ORDER BY substr(v2.enddate, 1, 19) DESC
        LIMIT 1
    )


    WHERE 
        (
            (:patientUuid IS NOT NULL AND :patientUuid != '' AND P.uuid = :patientUuid)
            OR
            (
                (:patientUuid IS NULL OR :patientUuid = '')
               AND ((julianday('now') - julianday(P.date_of_birth)) / 365.25 >= :age)
            )
        )

    ORDER BY datetime(V.startdate) DESC;
    """
    )
    suspend fun getPatientVisitRawDataForFollowup(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String
    ): List<PatientVisitDetails>


}