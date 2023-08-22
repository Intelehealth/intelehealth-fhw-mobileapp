package org.intelehealth.ezazi.ui.visit.dialog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 23:23.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class VisitCompletionHelper {
    public SessionManager sessionManager;
    public Context context;
    public String visitId;
    public final LayoutInflater inflater;
    private static final String TAG = "VisitCompletionHelper";

    public VisitCompletionHelper(Context context, String visitId) {
        sessionManager = new SessionManager(context);
        this.visitId = visitId;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public String insertVisitCompleteEncounter() {
        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            String encounterUuid = encounterDAO.insertVisitCompleteEncounterToDb(visitId, sessionManager.getProviderID());
            if (encounterUuid != null && encounterUuid.length() > 0) {
                VisitsDAO visitsDAO = new VisitsDAO();
                visitsDAO.updateVisitEnddate(visitId, AppConstants.dateAndTimeUtils.currentDateTime());
            }
            return encounterUuid;
        } catch (DAOException e) {
            return "";
        }
    }

    public boolean addMotherDeceasedObs(boolean isMotherDeceased, String motherDeceasedReason) {
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            String encounterUuid = insertVisitCompleteEncounter();
            if (encounterUuid != null && encounterUuid.length() > 0) {
                isInserted = obsDAO.insertMotherDeceasedFlatObs(encounterUuid, sessionManager.getCreatorID(), isMotherDeceased ? "YES" : "NO");
                if (isMotherDeceased) {
                    isInserted = obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), motherDeceasedReason, UuidDictionary.MOTHER_DECEASED);
                }
            }

        } catch (DAOException e) {
            Log.e(TAG, "addMotherDeceasedObs: " + e.getMessage());
        }

        return isInserted;
    }

    public ObsDTO createObs(String encounterUuid, String conceptId, String value) {
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setValue(value);
        obsDTO.setConceptuuid(conceptId);
        return obsDTO;
    }
}
