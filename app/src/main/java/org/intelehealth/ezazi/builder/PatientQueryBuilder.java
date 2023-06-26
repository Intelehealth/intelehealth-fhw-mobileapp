package org.intelehealth.ezazi.builder;

import android.database.Cursor;
import android.util.Log;

import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;

/**
 * Created by Vaghela Mithun R. on 25-06-2023 - 10:58.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PatientQueryBuilder extends QueryBuilder {
    public static final String TAG = "PatientQueryBuilder";

    public String searchQuery(String keyword) {
        String query = select("P.uuid, P.openmrs_id, P.first_name, P.last_name, P.middle_name, P.date_of_birth, PA.value as bedNo")
                .from("tbl_patient P")
                .join("LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid " +
                        "AND PA.person_attribute_type_uuid = (SELECT uuid FROM tbl_patient_attribute_master " +
                        "WHERE name = '" + PatientAttributesDTO.Columns.BED_NUMBER.value + "')")
                .where(" P.first_name LIKE '%" + keyword + "%' " +
                        "OR P.middle_name LIKE '%" + keyword + "%' " +
                        "OR P.last_name LIKE '%" + keyword + "%' " +
                        "OR (P.first_name || P.middle_name) LIKE '%" + keyword + "%' " +
                        "OR (P.middle_name || P.last_name) LIKE '%" + keyword + "%' " +
                        "OR (P.first_name || P.last_name) LIKE '%" + keyword + "%' " +
                        "OR P.openmrs_id LIKE '%" + keyword + "%' ")
                .groupBy("P.uuid")
                .orderBy("P.first_name")
                .inOrderOf("ASC")
                .build();
        Log.e(TAG, "searchQuery => " + query);
        return query;
    }

    public String pagingQuery(int offset, int limit) {
        String query = select("P.uuid, P.openmrs_id, P.first_name, P.last_name, P.middle_name, P.date_of_birth, PA.value as bedNo")
                .from("tbl_patient P")
                .join("LEFT OUTER JOIN tbl_patient_attribute PA ON PA.patientuuid = P.uuid " +
                        "AND PA.person_attribute_type_uuid = (SELECT uuid FROM tbl_patient_attribute_master " +
                        "WHERE name = '" + PatientAttributesDTO.Columns.BED_NUMBER.value + "')")
                .groupBy("P.uuid")
                .orderBy("P.first_name")
                .inOrderOf("ASC")
                .limit(limit)
                .offset(offset)
                .build();
        Log.e(TAG, "pagingQuery => " + query);
        return query;
    }
}
