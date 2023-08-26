package org.intelehealth.ezazi.ui.visit.dialog;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.databinding.DialogReferHospitalEzaziBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 25-08-2023 - 22:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ReferTypeHelper extends VisitCompletionHelper {
    private static final String TAG = "ReferTypeHelper";
    protected TextView selectedView;

    private OnReferredListener listener;

    public interface OnReferredListener {
        void onReferred();
    }

    public ReferTypeHelper(Context context, String visitId) {
        super(context, visitId);
    }

    public void completeVisitWithReferType(String value, String conceptId, OnReferredListener onReferredListener) {
        listener = onReferredListener;
        Log.e(TAG, "completeVisitWithReferType: value =>" + value);
        Log.e(TAG, "completeVisitWithReferType: conceptId =>" + conceptId);
        if (value.equals(context.getString(R.string.refer_to_other_hospital))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_refer_other, () -> referOtherHospitalDialog(value, conceptId));
        } else if (value.equals(context.getString(R.string.self_discharge_medical_advice))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_self_discharge, () -> completeVisitWithOtherReason(value, conceptId));
        } else if (value.equals(context.getString(R.string.shift_to_c_section))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_shift_to_c_section, () -> completeVisitWithOtherReason(value, conceptId));
        } else if (value.equals(context.getString(R.string.refer_to_icu))) {
            showConfirmationDialog(R.string.are_you_sure_want_to_refer_to_icu, () -> completeVisitWithOtherReason(value, conceptId));
        }
    }

    public void showConfirmationDialog(@StringRes int content, ConfirmationDialogFragment.OnConfirmationActionListener listener) {
        showConfirmationDialog(context.getString(content), listener);
    }

    public void showConfirmationDialog(String content, ConfirmationDialogFragment.OnConfirmationActionListener listener) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(context)
                .content(content)
                .positiveButtonLabel(R.string.yes)
                .build();

        dialog.setListener(listener);

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    public void referOtherHospitalDialog(String value, String conceptId) {
        DialogReferHospitalEzaziBinding binding = DialogReferHospitalEzaziBinding.inflate(inflater, null, false);

        showCustomViewDialog(R.string.refer_section, R.string.yes, R.string.no, binding.getRoot(), () -> {
            boolean isInserted = false;
            String hospitalName = binding.referHospitalName.getText().toString();
            String doctorName = binding.referDoctorName.getText().toString();
            String note = binding.referNote.getText().toString();

            // call visitcompleteenc and add obs for refer type and referal values entered...
            try {
                isInserted = referToOtherHospital(hospitalName, doctorName, note, value, conceptId);
                if (isInserted) listener.onReferred();
            } catch (DAOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean referToOtherHospital(String hospitalName,
                                         String doctorName,
                                         String note, String value, String conceptId) throws DAOException {

        boolean isInserted = true;
        String encounterUuid = insertVisitCompleteEncounter();

        // Now get this encounteruuid and create refer obs table.
        if (!encounterUuid.isEmpty()) {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO;
            List<ObsDTO> obsDTOList = new ArrayList<>();

            // 1. Refer Type
            obsDTOList.add(createObs(encounterUuid, conceptId, value));

            // 2. Refer Hospital Name
            if (hospitalName != null && hospitalName.length() > 0) {
                obsDTOList.add(createObs(encounterUuid, UuidDictionary.REFER_HOSPITAL, hospitalName));
            }

            // 3. Refer Doctor Name
            if (doctorName != null && doctorName.length() > 0) {
                obsDTOList.add(createObs(encounterUuid, UuidDictionary.REFER_DR_NAME, doctorName));
            }

            // 4. Refer Note
            if (note != null && note.length() > 0) {
                obsDTOList.add(createObs(encounterUuid, UuidDictionary.REFER_NOTE, note));
            }

            isInserted = obsDAO.insertObsToDb(obsDTOList, TAG);
        }

        return isInserted;
    }

    public void completeVisitWithOtherReason(String value, String conceptId) {
        // Now get this encounteruuid and create BIRTH_OUTCOME in obs table.
        try {
            ObsDAO obsDAO = new ObsDAO();
            String encounterUuid = insertVisitCompleteEncounter();
            Log.e(TAG, "completeVisitWithOtherReason: encounterId =>" + encounterUuid);
            if (encounterUuid != null && encounterUuid.length() > 0) {
                boolean isInserted = obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), value, conceptId);
                Log.e(TAG, "completeVisitWithOtherReason: isInserted => " + isInserted);
                if (isInserted) {
                    listener.onReferred();
                }
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }
}
