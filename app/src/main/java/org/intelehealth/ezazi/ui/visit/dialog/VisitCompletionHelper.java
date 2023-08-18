package org.intelehealth.ezazi.ui.visit.dialog;

import android.content.Context;
import android.view.LayoutInflater;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 23:23.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class VisitCompletionHelper {
    protected SessionManager sessionManager;
    protected Context context;
    protected String visitId;
    protected final LayoutInflater inflater;

    public VisitCompletionHelper(Context context, String visitId) {
        sessionManager = new SessionManager(context);
        this.visitId = visitId;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    protected String insertVisitCompleteEncounter() {
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

    protected ObsDTO createObs(String encounterUuid, String conceptId, String value) {
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setValue(value);
        obsDTO.setConceptuuid(conceptId);
        return obsDTO;
    }
}
