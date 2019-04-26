package io.intelehealth.client.dao;

import java.util.List;

import io.intelehealth.client.app.IntelehealthApplication;
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
import io.intelehealth.client.utilities.SessionManager;

public class SyncDAO {
    public static String TAG = "SyncDAO";
    SessionManager sessionManager = null;
    public boolean SyncData(ResponseDTO responseDTO) throws DAOException {
        boolean isSynced = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

        try {
            Logger.logD(TAG, "pull sync started");
            insertPatients(responseDTO.getData().getPatientDTO());

            insertPatientAttributes(responseDTO.getData().getPatientAttributesDTO());

            insertPatientAttributesMaster(responseDTO.getData().getPatientAttributeTypeMasterDTO());


            insertVisits(responseDTO.getData().getVisitDTO());


            insertEncounters(responseDTO.getData().getEncounterDTO());

            insertObs(responseDTO.getData().getObsDTO());


            insertLocations(responseDTO.getData().getLocationDTO());


            insertProviders(responseDTO.getData().getProviderlist());

            Logger.logD(TAG, "Pull sync ended");
            sessionManager.setFirstTimeSyncExecute(false);
        } catch (Exception e) {
            Logger.logE(TAG, "Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isSynced;

    }

    private boolean insertPatients(List<PatientDTO> patientList) throws DAOException {
        boolean isInserted = true;

        PatientsDAO patientsDAO = new PatientsDAO();
        try {
            patientsDAO.insertPatients(patientList);

        } catch (Exception e) {
            Logger.logE(TAG, "Patients Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertVisits(List<VisitDTO> visitList) throws DAOException {
        boolean isInserted = true;

        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.insertVisitTemp(visitList);

        } catch (Exception e) {
            Logger.logE(TAG, "Visit Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertEncounters(List<EncounterDTO> encounterList) throws DAOException {
        boolean isInserted = true;

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.insertEncounter(encounterList);


        } catch (Exception e) {
            Logger.logE(TAG, "Encounter Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertObs(List<ObsDTO> obsList) throws DAOException {
        boolean isInserted = true;

        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDAO.insertObsTemp(obsList);


        } catch (Exception e) {
            Logger.logE(TAG, "Obs Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;


    }

    private boolean insertLocations(List<LocationDTO> locationList) throws DAOException {
        boolean isInserted = true;

        LocationDAO locationDAO = new LocationDAO();
        try {
            locationDAO.insertLocations(locationList);


        } catch (Exception e) {
            Logger.logE(TAG, "location Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

    private boolean insertProviders(List<ProviderDTO> providerList) throws DAOException {
        boolean isInserted = true;

        ProviderDAO providerDAO = new ProviderDAO();
        try {
            providerDAO.insertProviders(providerList);

        } catch (Exception e) {
            Logger.logE(TAG, "provider Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

    private boolean insertPatientAttributes(List<PatientAttributesDTO> patientAttributesDTOList) throws DAOException {
        boolean isInserted = true;

        PatientsDAO patientsDAO = new PatientsDAO();
        try {
            patientsDAO.patientAttributes(patientAttributesDTOList);


        } catch (Exception e) {
            Logger.logE(TAG, "patient attribute Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

    private boolean insertPatientAttributesMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOList) throws DAOException {
        boolean isInserted = true;

        PatientsDAO patientsDAO = new PatientsDAO();
        try {
            patientsDAO.patinetAttributeMaster(patientAttributeTypeMasterDTOList);


        } catch (Exception e) {
            Logger.logE(TAG, "master Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }

}
