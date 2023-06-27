package org.intelehealth.ezazi.ui.visit;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.builder.PatientQueryBuilder;
import org.intelehealth.ezazi.models.ActivePatientModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 28-06-2023 - 01:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class VisitQueryResultBinder {
    private List<ActivePatientModel> fetchActiveVisits(Cursor cursor) {
        List<ActivePatientModel> activeVisits = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    ActivePatientModel model = new ActivePatientModel(
                            cursor.getString(cursor.getColumnIndexOrThrow("visitUuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phoneNumber")),
                            cursor.getString(cursor.getColumnIndexOrThrow("sync")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setBedNo(cursor.getString(cursor.getColumnIndexOrThrow("bedNo")));
                    model.setStageName(cursor.getString(cursor.getColumnIndexOrThrow("stage")));
                    model.setLatestEncounterId(cursor.getString(cursor.getColumnIndexOrThrow("latestEncounterId")));
                    activeVisits.add(model);
                } while (cursor.moveToNext());
            }
        }
        assert cursor != null;
        cursor.close();
        return activeVisits;
    }

    public List<ActivePatientModel> executeActiveVisitsQuery(int offset, int limit) {
        String query = new PatientQueryBuilder().activeVisitsQuery(offset, limit);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery(query, null);
        return fetchActiveVisits(cursor);
    }
}
