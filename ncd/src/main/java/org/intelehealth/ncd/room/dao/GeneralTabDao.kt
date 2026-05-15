package org.intelehealth.ncd.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.model.PrescriptionExistsResult
import org.intelehealth.ncd.model.VisitAttributeResult

@Dao
interface GeneralTabDao {

    /**
     * Latest visit per patient + paging, ordered by visit start (nulls last).
     *
     * Previous shape correlated two subqueries per patient before LIMIT (very slow on large DBs).
     * This version: one GROUP BY over visits for "pick key", join, sort + LIMIT in inner query,
     * then phone lookup only for the returned page rows.
     */
    @Query(
        """
        SELECT
            innerQ.patientId AS patientId,
            innerQ.firstName AS firstName,
            innerQ.middleName AS middleName,
            innerQ.lastName AS lastName,
            innerQ.dateOfBirth AS dateOfBirth,
            innerQ.gender AS gender,
            innerQ.patientPhoto AS patientPhoto,
            innerQ.openmrs_id AS openmrs_id,
            phoneAttr.value AS patientPhoneNumber,
            innerQ.visitId AS visitId,
            innerQ.visitStartDate AS visitStartDate,
            innerQ.visitEndDate AS visitEndDate
        FROM (
            SELECT
                P.uuid AS patientId,
                P.first_name AS firstName,
                P.middle_name AS middleName,
                P.last_name AS lastName,
                P.date_of_birth AS dateOfBirth,
                P.gender AS gender,
                P.patient_photo AS patientPhoto,
                P.openmrs_id AS openmrs_id,
                V.uuid AS visitId,
                V.startdate AS visitStartDate,
                V.enddate AS visitEndDate
            FROM tbl_patient P
            LEFT JOIN (
                SELECT
                    patientuuid,
                    MAX(printf('%19s|%s', substr(IFNULL(startdate, ''), 1, 19), uuid)) AS visit_pick
                FROM tbl_visit
                WHERE startdate IS NOT NULL
                GROUP BY patientuuid
            ) AS visitPick ON visitPick.patientuuid = P.uuid
            LEFT JOIN tbl_visit V ON V.patientuuid = P.uuid
                AND visitPick.visit_pick IS NOT NULL
                AND printf('%19s|%s', substr(IFNULL(V.startdate, ''), 1, 19), V.uuid) = visitPick.visit_pick
            ORDER BY datetime(V.startdate) DESC, P.uuid
            LIMIT :limit OFFSET :offset
        ) AS innerQ
        LEFT JOIN tbl_patient_attribute phoneAttr
            ON phoneAttr.uuid = (
                SELECT pa2.uuid
                FROM tbl_patient_attribute pa2
                WHERE pa2.patientuuid = innerQ.patientId
                  AND pa2.person_attribute_type_uuid = :patientPhoneNoAttribute
                ORDER BY pa2.rowid DESC
                LIMIT 1
            )
    """
    )
    suspend fun getPatientsAndVisitsPage(limit: Int, offset: Int, patientPhoneNoAttribute: String): List<PatientVisitDetails>

    @Query("""
        SELECT V.uuid AS visitId,
               CASE WHEN EXISTS(
                   SELECT 1
                   FROM tbl_encounter E
                   WHERE E.visituuid = V.uuid
                     AND E.encounter_type_uuid = :encounterUuid
               ) THEN 1 ELSE 0 END AS prescriptionExists
        FROM tbl_visit V
        WHERE V.uuid IN (:visitIds)
    """)
    suspend fun getPrescriptionExistsBatch(
        encounterUuid: String,
        visitIds: List<String>
    ): List<PrescriptionExistsResult>

    @Query("""
        SELECT VA.visit_uuid AS visitId,
               VA.visit_attribute_type_uuid AS typeUuid,
               VA.value AS value
        FROM tbl_visit_attribute VA
        WHERE VA.visit_uuid IN (:visitIds)
          AND VA.visit_attribute_type_uuid IN (:typeUuids)
        ORDER BY VA.rowid DESC
    """)
    suspend fun getVisitAttributesBatch(
        visitIds: List<String>,
        typeUuids: List<String>
    ): List<VisitAttributeResult>


    @Query("""
    SELECT 
        P.uuid AS patientId,
        P.first_name AS firstName,
        P.middle_name AS middleName,
        P.last_name AS lastName,
        P.openmrs_id AS openmrsId,
        P.date_of_birth AS dateOfBirth,
        P.gender AS gender,
        P.patient_photo AS patientPhoto,

        -- phone attribute (latest entry)
        phoneAttr.value AS patientPhoneNumber,

        V.uuid AS visitId,
        V.startdate AS startDate,

        -- prescriptionExists (1/0)
        CASE WHEN EXISTS(
            SELECT 1
            FROM tbl_encounter E
            WHERE E.visituuid = V.uuid
              AND E.encounter_type_uuid = :encounterUuid
        ) THEN 1 ELSE 0 END AS prescriptionExists,

        -- visit attributes
        ncdAttr.value AS isNcdVisit,
        specAttr.value AS visitSpeciality

    FROM tbl_patient P
    INNER JOIN tbl_visit V ON V.patientuuid = P.uuid

    -- phone attribute join
    LEFT JOIN tbl_patient_attribute phoneAttr
        ON phoneAttr.uuid = (
            SELECT pa2.uuid
            FROM tbl_patient_attribute pa2
            WHERE pa2.patientuuid = P.uuid
              AND pa2.person_attribute_type_uuid = :phoneAttrUuid
            ORDER BY pa2.rowid DESC
            LIMIT 1
        )

    -- visit attribute for NCD
    LEFT JOIN tbl_visit_attribute ncdAttr
        ON ncdAttr.visit_uuid = V.uuid
       AND ncdAttr.visit_attribute_type_uuid = :ncdAttrUuid

    -- visit attribute for Speciality
    LEFT JOIN tbl_visit_attribute specAttr
        ON specAttr.visit_uuid = V.uuid
       AND specAttr.visit_attribute_type_uuid = :specialityAttrUuid

    WHERE
        (:query IS NULL OR :query = '' OR
            (P.first_name || ' ' || COALESCE(P.middle_name,'') || ' ' || COALESCE(P.last_name,''))
                LIKE '%' || :query || '%' OR
            P.openmrs_id LIKE '%' || :query || '%' OR
            phoneAttr.value LIKE '%' || :query || '%'
        )

    ORDER BY V.startdate DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getPagedPatientsSql(
        query: String,
        encounterUuid: String,
        ncdAttrUuid: String,
        specialityAttrUuid: String,
        phoneAttrUuid: String,
        limit: Int,
        offset: Int
    ): List<PatientVisitDetails>
}
