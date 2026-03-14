package org.intelehealth.abdm.database.dao;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.abdm.utils.AbdmManager;

public class PatientDao {

    public boolean isPatientExistWithAbhaAddress(String patientOpenMRSUuid, String abhaAddress) {
        boolean isInserted = false;
        SQLiteDatabase db = AbdmManager.getDbClient();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT COUNT(*) as count FROM tbl_patient WHERE openmrs_id = ? AND abha_address LIKE ? COLLATE NOCASE", new String[]{patientOpenMRSUuid, "%" + abhaAddress + "%"});
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                isInserted = count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }

        return isInserted;
    }
}
