package io.intelehealth.client.dao;

import java.util.List;

import io.intelehealth.client.dto.EncounterDTO;
import io.intelehealth.client.dto.LocationDTO;
import io.intelehealth.client.dto.ObsDTO;
import io.intelehealth.client.dto.PatientAttributeTypeMasterDTO;
import io.intelehealth.client.dto.PatientAttributesDTO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.dto.ProviderDTO;
import io.intelehealth.client.dto.ResponseDTO;
import io.intelehealth.client.dto.VisitDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;

public class SyncDAO {
    public static String TAG = "SyncDAO";

    public boolean SyncData(ResponseDTO responseDTO) throws DAOException {
        boolean isSynced = true;


        try {
            if (insertPatients(responseDTO.getData().getPatientDTO())) {
                Logger.logD(TAG, "Patients SYNC" + responseDTO.getData().getPatientDTO());
            } else {
                Logger.logD(TAG, "FAiled Synced");
            }
            if (insertPatientAttributes(responseDTO.getData().getPatientAttributesDTO())) {
                Logger.logD(TAG, "Patients Attributes SYNC" + responseDTO.getData().getPatientDTO());
            } else {
                Logger.logD(TAG, "FAiled Synced");
            }
            if (insertPatientAttributesMaster(responseDTO.getData().getPatientAttributeTypeMasterDTO())) {
                Logger.logD(TAG, "Patients Attributes Master SYNC" + responseDTO.getData().getPatientDTO());
            } else {
                Logger.logD(TAG, "FAiled Synced");
            }

            if (insertVisits(responseDTO.getData().getVisitDTO())) {
                Logger.logD(TAG, "Visits SYNC" + responseDTO.getData().getVisitDTO());
            } else {
                Logger.logD(TAG, "Visits sync FAiled");
            }

            if (insertEncounters(responseDTO.getData().getEncounterDTO())) {
                Logger.logD(TAG, "Encounter SYNC" + responseDTO.getData().getEncounterDTO());
            } else {
                Logger.logD(TAG, "Encounter sync FAiled");
            }

            if (insertObs(responseDTO.getData().getObsDTO())) {
                Logger.logD(TAG, "Obs SYNC" + responseDTO.getData().getObsDTO());
            } else {
                Logger.logD(TAG, "Obs sync FAiled");
            }

            if (insertLocations(responseDTO.getData().getLocationDTO())) {
                Logger.logD(TAG, "Location SYNC" + responseDTO.getData().getLocationDTO());
            } else {
                Logger.logD(TAG, "Location sync FAiled");
            }

            if (insertProviders(responseDTO.getData().getProviderlist())) {
                Logger.logD(TAG, "Provider SYNC" + responseDTO.getData().getProviderlist());
            } else {
                Logger.logD(TAG, "Provider sync FAiled");
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isSynced;

    }

    private boolean insertPatients(List<PatientDTO> patientList) throws DAOException {
        boolean isInserted = true;

        PatientsDAO patientsDAO = new PatientsDAO();
        try {
            if (patientsDAO.insertPatients(patientList)) {
                Logger.logD(TAG, "Inserted patients" + patientList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + patientList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertVisits(List<VisitDTO> visitList) throws DAOException {
        boolean isInserted = true;

        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            if (visitsDAO.insertVisitTemp(visitList)) {
                Logger.logD(TAG, "Inserted patients" + visitList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + visitList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertEncounters(List<EncounterDTO> encounterList) throws DAOException {
        boolean isInserted = true;

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            if (encounterDAO.insertEncounter(encounterList)) {
                Logger.logD(TAG, "Inserted patients" + encounterList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + encounterList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertObs(List<ObsDTO> obsList) throws DAOException {
        boolean isInserted = true;

        ObsDAO obsDAO = new ObsDAO();
        try {
            if (obsDAO.insertObsTemp(obsList)) {
                Logger.logD(TAG, "Inserted obs" + obsList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + obsList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertLocations(List<LocationDTO> locationList) throws DAOException {
        boolean isInserted = true;

        LocationDAO locationDAO = new LocationDAO();
        try {
            if (locationDAO.insertLocations(locationList)) {
                Logger.logD(TAG, "Inserted patients attributes" + locationList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + locationList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

    private boolean insertProviders(List<ProviderDTO> providerList) throws DAOException {
        boolean isInserted = true;

        ProviderDAO providerDAO = new ProviderDAO();
        try {
            if (providerDAO.insertProviders(providerList)) {
                Logger.logD(TAG, "Inserted patients attributes" + providerList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + providerList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

    private boolean insertPatientAttributes(List<PatientAttributesDTO> patientAttributesDTOList) throws DAOException {
        boolean isInserted = true;

        PatientsDAO patientsDAO = new PatientsDAO();
        try {
            if (patientsDAO.patientAttributes(patientAttributesDTOList)) {
                Logger.logD(TAG, "Inserted patients attributes" + patientAttributesDTOList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + patientAttributesDTOList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

    private boolean insertPatientAttributesMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOList) throws DAOException {
        boolean isInserted = true;

        PatientsDAO patientsDAO = new PatientsDAO();
        try {
            if (patientsDAO.patinetAttributeMaster(patientAttributeTypeMasterDTOList)) {
                Logger.logD(TAG, "Inserted patients attributes" + patientAttributeTypeMasterDTOList);
            } else {
                Logger.logD(TAG, "Error in Inserting" + patientAttributeTypeMasterDTOList);
            }

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

}
