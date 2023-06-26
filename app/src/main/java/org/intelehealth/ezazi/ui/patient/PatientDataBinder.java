package org.intelehealth.ezazi.ui.patient;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.models.dto.PatientDTO;
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
                model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(model.getUuid())));
                model.setBedNo(searchCursor.getString(searchCursor.getColumnIndexOrThrow("bedNo")));
                patients.add(model);
            } while (searchCursor.moveToNext());
        }

        return patients;
    }

    private String phoneNumber(String patientuuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();

        return phone;
    }
}
