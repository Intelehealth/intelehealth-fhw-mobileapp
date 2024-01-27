package org.intelehealth.ezazi.ui.visit.adapter;

import static org.intelehealth.ezazi.utilities.UuidDictionary.DECISION_PENDING;
import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.builder.QueryBuilder;

/**
 * Created by Kaveri Zaware on 24-01-2024
 * email - kaveri@intelehealth.org
 **/
public class OutcomeCount extends QueryBuilder {


    public int outcomePendingPatientQuery(int offset, int limit, String providerId, SQLiteDatabase db) {
        if(!db.isOpen()){
             db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        }
        db.beginTransaction();
        String query = select("P.date_of_birth, V.enddate, V. startdate,V.uuid as visitId, P.openmrs_id, P.dateCreated, " +
                "CASE WHEN middle_name IS NULL THEN first_name || ' ' || last_name " +
                "ELSE first_name || ' ' || middle_name || ' ' || last_name END fullName, " + getCurrentStageCase() + "," +
                "(SELECT value FROM tbl_visit_attribute where visit_attribute_type_uuid = '" + DECISION_PENDING + "') as outcomePending")
                .from(" tbl_visit V ")
                .join(" LEFT JOIN tbl_patient P ON P.uuid = V.patientuuid ")
                .joinPlus(" LEFT JOIN tbl_visit_attribute VA ON VA.visit_uuid = V.uuid ")
                .where(" V.uuid NOT IN (Select visituuid FROM tbl_encounter WHERE  encounter_type_uuid ='" + ENCOUNTER_VISIT_COMPLETE + "' ) " +
                        "AND V.voided IN ('0', 'false', 'FALSE') AND VA.value = '" + providerId + "' /*AND outcomePending = 'true'*/ ")
                .groupBy(" V.uuid ")
                .orderBy(" P.dateCreated ")
                .orderIn("DESC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e("tag", "outcomePendingPatientQuery11 => " + query);

        Cursor cursor = db.rawQuery(query, null);
        if(db.isOpen()){
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }


        return cursor.getCount();
    }

    private String getCurrentStageCase() {
        return "(SELECT CASE " +
                "WHEN U.name LIKE '%Stage1%'  THEN 'Stage-1' " +
                "WHEN U.name LIKE '%Stage2%'  THEN 'Stage-2' ELSE U.name " +
                "END Stage FROM tbl_encounter E, tbl_uuid_dictionary U " +
                "WHERE E.visituuid =V.uuid  and E.voided = '0'  and U.uuid = E.encounter_type_uuid  ORDER BY U.name DESC LIMIT 1)  as stage ";
    }

}
