package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
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
    private SQLiteDatabase db = null;

    public boolean SyncData(ResponseDTO responseDTO) throws DAOException {
        boolean isSynced = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();
        LocationDAO locationDAO = new LocationDAO();
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            Logger.logD(TAG, "pull sync started");

            patientsDAO.insertPatients(responseDTO.getData().getPatientDTO());
            patientsDAO.patientAttributes(responseDTO.getData().getPatientAttributesDTO());
            patientsDAO.patinetAttributeMaster(responseDTO.getData().getPatientAttributeTypeMasterDTO());
            visitsDAO.insertVisitTemp(responseDTO.getData().getVisitDTO());
            encounterDAO.insertEncounter(responseDTO.getData().getEncounterDTO());
            obsDAO.insertObsTemp(responseDTO.getData().getObsDTO());
            locationDAO.insertLocations(responseDTO.getData().getLocationDTO());
            providerDAO.insertProviders(responseDTO.getData().getProviderlist());

            insertAfterPull();
            Logger.logD(TAG, "Pull sync ended");
            sessionManager.setFirstTimeSyncExecute(false);
        } catch (Exception e) {
            Logger.logE(TAG, "Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isSynced;

    }

    private boolean insertAfterPull() throws DAOException {
        boolean isInserted = true;

        BackgroundSyncDAO backgroundSyncDAO = new BackgroundSyncDAO();
        try {
            backgroundSyncDAO.insertAfterPull();

        } catch (Exception e) {
            Logger.logE(TAG, "Patients Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isInserted;

    }
}
