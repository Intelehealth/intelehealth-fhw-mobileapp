package org.intelehealth.ezazi.ui.visit.dialog;

import static org.intelehealth.ezazi.app.AppConstants.INPUT_MAX_LENGTH;

import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.VisitAttributeListDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.databinding.MotherDeceasedDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.ui.visit.model.VisitOutcome;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.Objects;
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

    public interface MotherDeathListener {
        void onMotherDeathObservationAdded();
    }

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
                new VisitAttributeListDAO().markVisitAsRead(visitId);
            }
            return encounterUuid;
        } catch (DAOException e) {
            return "";
        }
    }

    public boolean addMotherDeceasedObs(String encounterId, boolean isMotherDeceased, String motherDeceasedReason) {
        Log.e(TAG, "addMotherDeceasedObs: " + encounterId);
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            if (encounterId != null && encounterId.length() > 0) {
                isInserted = obsDAO.insertMotherDeceasedFlatObs(encounterId, sessionManager.getCreatorID(),
                        isMotherDeceased ? VisitOutcome.MotherDeceased.YES.name() : VisitOutcome.MotherDeceased.NO.name());
                if (isMotherDeceased) {
                    isInserted = obsDAO.insert_Obs(encounterId, sessionManager.getCreatorID(), motherDeceasedReason, UuidDictionary.MOTHER_DECEASED);
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
        obsDTO.setCreatorUuid(sessionManager.getCreatorID());
        return obsDTO;
    }

    public void showCustomViewDialog(@StringRes int title,
                                     @StringRes int positiveLbl,
                                     @StringRes int negLbl,
                                     View view,
                                     CustomViewDialogFragment.OnConfirmationActionListener listener) {
        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(context)
                .title(title)
                .positiveButtonLabel(positiveLbl)
                .negativeButtonLabel(negLbl)
                .view(view)
                .build();

        dialog.setListener(listener);

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    public void showMotherDeceasedDialog(MotherDeathListener motherDeathListener) {
        MotherDeceasedDialogBinding binding = MotherDeceasedDialogBinding.inflate(inflater, null, false);
        binding.etLayoutMotherDeceased.setMultilineInputEndIconGravity();
        binding.etMotherDeceasedReason.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter(), new InputFilter.LengthFilter(INPUT_MAX_LENGTH)});
        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(context).title(R.string.mother_deceased).positiveButtonLabel(R.string.yes).negativeButtonLabel(R.string.no).view(binding.getRoot()).build();

        dialog.requireValidationBeforeDismiss(true);
        dialog.setListener(() -> {
            if (Objects.requireNonNull(binding.etMotherDeceasedReason.getText()).length() > 0) {
                String value = binding.etMotherDeceasedReason.getText().toString();
                String encounterId = insertVisitCompleteEncounter();
                if (encounterId != null && encounterId.length() > 0) {
                    boolean isInserted = addMotherDeceasedObs(encounterId, true, value);
                    if (isInserted) motherDeathListener.onMotherDeathObservationAdded();
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(context, context.getString(R.string.please_enter_reason), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }
}
