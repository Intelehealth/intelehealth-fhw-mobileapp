package io.intelehealth.client.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import io.intelehealth.client.dto.EncounterDTO;
import io.intelehealth.client.dto.LocationDTO;
import io.intelehealth.client.dto.ObsDTO;
import io.intelehealth.client.dto.PatientAttributeTypeMasterDTO;
import io.intelehealth.client.dto.PatientAttributesDTO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.dto.ProviderDTO;
import io.intelehealth.client.dto.VisitDTO;

@Dao
public interface InteleHealthDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPatients(PatientDTO patientDTO);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePatinets(PatientDTO patientDTO);

    @Query("SELECT * FROM tbl_patients where uuid is :uuid")
    PatientDTO findPatientsUuid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVisits(VisitDTO visit);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateVisits(VisitDTO visit);

    @Query("SELECT * FROM tbl_visits where uuid is :uuid")
    VisitDTO findVisitsUuid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertObs(ObsDTO obsDTO);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateObs(ObsDTO obsDTO);

    @Query("SELECT * FROM tbl_obs where uuid is :uuid")
    ObsDTO findObsUuid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEncounters(EncounterDTO encounte);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateEncounters(EncounterDTO encounterDTO);

    @Query("SELECT * FROM tbl_encounter where uuid is :uuid")
    EncounterDTO findEncounterUuid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProviders(ProviderDTO providerDTO);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateProviders(ProviderDTO providerDTO);

    @Query("SELECT * FROM tbl_provider where uuid is :uuid")
    ProviderDTO findProvidersUuid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPatientAttributes(PatientAttributesDTO patientAttributesDTO);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePatinetAttributes(PatientAttributesDTO patientAttributesDTO);

    @Query("SELECT * FROM tbl_patientattributes where uuid is :uuid")
    PatientAttributesDTO findPatientAttributesUUid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertpatientAttributesMaster(PatientAttributeTypeMasterDTO patientAttributeTypeMasterDTO);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePatinetAttributesMaster(PatientAttributeTypeMasterDTO patientAttributeTypeMasterDTO);

    @Query("SELECT * FROM tbl_patientattributesmaster where uuid is :uuid")
    PatientAttributeTypeMasterDTO findpatientAttributesMasterUUid(String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocations(LocationDTO locationDTO);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLocatinon(LocationDTO locationDTO);

    @Query("SELECT * FROM tbl_location where locationuuid is :uuid")
    LocationDTO findLocationUUid(String uuid);

//    @Query("SELECT * FROM tbl_visits, tbl_patients WHERE tbl_visits.patientuuid = tbl_patients.uuid  AND tbl_visits.enddate IS NULL OR tbl_visits.enddate = '' ORDER BY tbl_visits.startdate ASC")
//    ActivePatientModel findActivePatinets();


}
