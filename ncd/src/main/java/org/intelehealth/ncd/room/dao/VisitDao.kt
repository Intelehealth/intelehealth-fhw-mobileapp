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
    /* @Query(
         """
     SELECT
         CASE
             WHEN :visitUuid IN (
                 SELECT visituuid
                 FROM tbl_encounter
                 WHERE encounter_type_uuid = :visitCompletedId
             ) THEN 'Received'
             ELSE 'Pending'
         END AS status,

         V.startdate AS visitStartDate,
         P.first_name AS firstName,
         P.last_name AS lastName,
         P.gender AS gender,
         P.date_of_birth AS dob,

         (
             SELECT value
             FROM tbl_patient_attribute
             WHERE patientuuid = P.uuid
               AND person_attribute_type_uuid = :attributeTypeUuid
             LIMIT 1
         ) AS attributeValue

     FROM tbl_visit V
     JOIN tbl_patient P ON P.uuid = V.patientuuid
     WHERE V.uuid = :visitUuid
     """
     )
     fun getPatientVisitDetails(
         visitUuid: String,
         visitCompletedId: String,
         attributeTypeUuid: String
     ): LiveData<PatientVisitDetails>*/

    @Query(
        """
    SELECT 
        V.uuid AS visitUuid,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate, 
        
        P.uuid AS patientUuid,
        P.first_name AS firstName,
        P.last_name AS lastName,
        P.gender AS gender,
        P.date_of_birth AS dob,
        P.openmrs_id AS openmrs_id

    FROM tbl_visit V
    JOIN tbl_patient P ON P.uuid = V.patientuuid
    WHERE V.patientuuid = :patientUuid
    """
    )
    fun getVisitsByPatientUuid(
        patientUuid: String
    ): List<PatientVisitDetails>

    @Query("SELECT uuid, startdate, enddate, sync FROM tbl_visit WHERE patientuuid = :patientUUID LIMIT 1")
    fun getVisitForPatient(patientUUID: String): List<Visit>

    @Query(
        """
        SELECT uuid 
        FROM tbl_encounter 
        WHERE visituuid = :visitUuid 
          AND encounter_type_uuid = :encounterTypeUuid 
          AND (sync = '1' OR sync = 'true' OR sync = 'TRUE') 
        COLLATE NOCASE 
        LIMIT 1
        """
    )
    suspend fun getStartVisitNoteEncounterByVisitUUID(
        visitUuid: String,
        encounterTypeUuid: String
    ): String

   /* @Query(
        """
    SELECT 
        P.uuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM tbl_encounter E 
                WHERE E.visituuid = V.uuid 
                  AND E.encounter_type_uuid = :visitNoteEncounterUuid
            ) THEN 1 
            ELSE 0 
        END AS prescriptionExists
    FROM tbl_patient P

    LEFT JOIN tbl_patient_attribute A 
        ON A.patientuuid = P.uuid 
        AND A.person_attribute_type_uuid = :attributeTypeUuid
        AND A.modified_date = (
            SELECT MAX(A2.modified_date)
            FROM tbl_patient_attribute A2
            WHERE A2.patientuuid = P.uuid 
              AND A2.person_attribute_type_uuid = :attributeTypeUuid
        )

    LEFT JOIN (
        SELECT *
        FROM tbl_visit
        WHERE startdate IS NOT NULL
        AND uuid IN (
            SELECT uuid FROM (
                SELECT uuid,
                       patientuuid,
                       MAX(datetime(startdate)) AS max_date
                FROM tbl_visit
                GROUP BY patientuuid
            )
        )
    ) V ON V.patientuuid = P.uuid

    WHERE DATE('now') >= DATE(P.date_of_birth, :age || ' years')

    ORDER BY datetime(V.startdate) DESC
    """
    )


    suspend fun getPatientVisitRawData(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String
    ): List<PatientVisitDetails>*/

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
        P.modified_date AS modifiedDate,
        P.openmrs_id AS openmrs_id,
        NULL AS attributeValue,
        NULL AS attributeTypeUuid,
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM tbl_encounter E 
                WHERE E.visituuid = V.uuid 
                  AND E.encounter_type_uuid = :visitNoteEncounterUuid
            ) THEN 1 
            ELSE 0 
        END AS prescriptionExists
    FROM tbl_patient P

    LEFT JOIN (
        SELECT *
        FROM tbl_visit
        WHERE startdate IS NOT NULL
          AND uuid IN (
              SELECT uuid
              FROM (
                  SELECT uuid,
                         patientuuid,
                         MAX(datetime(startdate)) AS max_date
                  FROM tbl_visit
                  GROUP BY patientuuid
              )
          )
    ) V ON V.patientuuid = P.uuid

    WHERE DATE('now') < DATE(P.date_of_birth, :age || ' years')

    ORDER BY datetime(P.modified_date) DESC
    """
    )
    suspend fun getPatientVisitRawDataBelowAgeForGeneralOld(
        age: Int,
        visitNoteEncounterUuid: String
    ): List<PatientVisitDetails>


    /*@Query(
        """
    SELECT 
        P.uuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,
        NULL AS attributeValue,
        NULL AS attributeTypeUuid,
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM tbl_encounter E 
                WHERE E.visituuid = V.uuid 
                  AND E.encounter_type_uuid = :visitNoteEncounterUuid
            ) THEN 1 
            ELSE 0 
        END AS prescriptionExists
    FROM tbl_patient P

    LEFT JOIN (
        SELECT *
        FROM tbl_visit
        WHERE startdate IS NOT NULL
        AND uuid IN (
            SELECT uuid FROM (
                SELECT uuid,
                       patientuuid,
                       MAX(datetime(startdate)) AS max_date
                FROM tbl_visit
                GROUP BY patientuuid
            )
        )
    ) V ON V.patientuuid = P.uuid

    WHERE DATE('now') < DATE(P.date_of_birth, :age || ' years')

    ORDER BY datetime(V.startdate) DESC
    """
    )
    suspend fun getPatientVisitRawDataBelowAgeForGeneral(
        age: Int,
        visitNoteEncounterUuid: String
    ): List<PatientVisitDetails>*/


   /* @Query(
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
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM tbl_encounter E 
                WHERE E.visituuid = V.uuid 
                  AND E.encounter_type_uuid = :visitNoteEncounterUuid
            ) THEN 1 
            ELSE 0 
        END AS prescriptionExists
    FROM tbl_patient P
    LEFT JOIN tbl_patient_attribute A 
        ON A.uuid = (
            SELECT pa.uuid
            FROM tbl_patient_attribute pa
            WHERE pa.patientuuid = P.uuid
              AND pa.person_attribute_type_uuid = :attributeTypeUuid
            ORDER BY datetime(pa.modified_date) DESC
            LIMIT 1
        )
    LEFT JOIN tbl_visit V
        ON V.uuid = (
            SELECT v2.uuid
            FROM tbl_visit v2
            WHERE v2.patientuuid = P.uuid
              AND v2.startdate IS NOT NULL
            ORDER BY datetime(v2.startdate) DESC
            LIMIT 1
        )
    WHERE DATE('now') >= DATE(P.date_of_birth, :age || ' years')
    ORDER BY datetime(V.startdate) DESC;
    """
    )
    suspend fun getPatientVisitRawData(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String
    ): List<PatientVisitDetails>

    */
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

        -- latest patient attribute
        A.value AS attributeValue,
        A.person_attribute_type_uuid AS attributeTypeUuid,

        -- latest visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,

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
    suspend fun getPatientVisitRawDataForFollowup(
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

        -- latest visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,

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
        ) AS visit_speciality

    FROM tbl_patient P

    LEFT JOIN tbl_visit V
        ON V.uuid = (
            SELECT v2.uuid
            FROM tbl_visit v2
            WHERE v2.patientuuid = P.uuid
              AND v2.startdate IS NOT NULL
            ORDER BY substr(v2.startdate, 1, 19) DESC
            LIMIT 1
        )

    ORDER BY datetime(V.startdate) DESC;
    """
    )
    suspend fun getPatientVisitRawDataGeneral(
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

        -- latest visit
        V.uuid AS visitId,
        V.startdate AS visitStartDate,
        V.enddate AS visitEndDate,
        '' AS attributeValue,
        '' AS attributeTypeUuid,

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
       '' AS chief_complaint_data


    FROM tbl_patient P

    LEFT JOIN tbl_visit V
        ON V.uuid = (
            SELECT v2.uuid
            FROM tbl_visit v2
            WHERE v2.patientuuid = P.uuid
              AND v2.startdate IS NOT NULL
            ORDER BY substr(v2.startdate, 1, 19) DESC
            LIMIT 1
        )

    ORDER BY datetime(V.startdate) DESC
    """
    )
     fun getPatientVisitPagingSource(
        visitNoteEncounterUuid: String
    ): PagingSource<Int, PatientVisitDetails>

}