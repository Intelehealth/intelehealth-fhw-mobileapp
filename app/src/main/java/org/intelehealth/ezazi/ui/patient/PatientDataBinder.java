package org.intelehealth.ezazi.ui.patient;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.ui.visit.model.CompletedVisitStatus;
import org.intelehealth.ezazi.ui.visit.model.VisitOutcome;
import org.intelehealth.ezazi.utilities.StringUtils;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 25-06-2023 - 11:25.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PatientDataBinder {

    public List<PatientDTO> retrieveDataFromCursor(Cursor searchCursor) throws DAOException {
        List<PatientDTO> patients = new ArrayList<>();
        if (searchCursor.moveToFirst()) {
            do {
                PatientDTO model = new PatientDTO();
                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                model.setPhonenumber(StringUtils.mobileNumberEmpty(searchCursor.getString(searchCursor.getColumnIndexOrThrow("phoneNumber"))));
                model.setBedNo(searchCursor.getString(searchCursor.getColumnIndexOrThrow("bedNo")));
                model.setAlternateNo(searchCursor.getString(searchCursor.getColumnIndexOrThrow("alertCount")));
                model.setCreatedAt(searchCursor.getString(searchCursor.getColumnIndexOrThrow("dateCreated")));
                String birthStatus = searchCursor.getString(searchCursor.getColumnIndexOrThrow("birthStatus"));
                String motherDeceased = searchCursor.getString(searchCursor.getColumnIndexOrThrow("motherDeceased"));
                String stage = searchCursor.getString(searchCursor.getColumnIndexOrThrow("stage"));
                if (motherDeceased != null && motherDeceased.equals(VisitOutcome.MotherDeceased.YES.name())) {
                    motherDeceased = CompletedVisitStatus.MotherDeceased.MOTHER_DECEASED_REASON.sortValue();
                    birthStatus = birthStatus != null ? birthStatus + "\n" + motherDeceased : motherDeceased;
                }

                if (birthStatus != null) model.setStage(birthStatus);
                else model.setStage(stage);

                patients.add(model);
            } while (searchCursor.moveToNext());
        }

        return patients;
    }

    public List<PatientDTO> upcomingPatients(Cursor cursor) {
        List<PatientDTO> patients = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                PatientDTO model = new PatientDTO();
                model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("fullName")));
                model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("dateCreated")));
                patients.add(model);
            } while (cursor.moveToNext());
        }

        return patients;
    }
}
