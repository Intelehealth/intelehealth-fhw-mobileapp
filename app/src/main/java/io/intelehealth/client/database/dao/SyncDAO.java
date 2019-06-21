package io.intelehealth.client.database.dao;

import android.database.sqlite.SQLiteDatabase;

import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.ResponseDTO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.exception.DAOException;

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
            visitsDAO.insertVisit(responseDTO.getData().getVisitDTO());
//            visitsDAO.insertVisitAttrib(responseDTO.getData().getVisitAttributeList());
//            visitsDAO.insertVisitAttribType(responseDTO.getData().getVisitAttributeTypeList());
            encounterDAO.insertEncounter(responseDTO.getData().getEncounterDTO());
            obsDAO.insertObsTemp(responseDTO.getData().getObsDTO());
            locationDAO.insertLocations(responseDTO.getData().getLocationDTO());
            providerDAO.insertProviders(responseDTO.getData().getProviderlist());

            insertAfterPull();

            Logger.logD(TAG, "Pull sync ended");
            sessionManager.setPullExcutedTime(sessionManager.isPulled());
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
