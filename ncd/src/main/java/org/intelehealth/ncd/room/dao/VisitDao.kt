package org.intelehealth.ncd.room.dao

import androidx.lifecycle.LiveData
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


    suspend fun getPatientVisitRawDataBelowAgeForGeneral(
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

}