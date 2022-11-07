package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.PrescriptionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 3/11/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class VisitPendingFragment extends Fragment {
    private RecyclerView recycler_today, recycler_week, recycler_month;
    private CardView visit_pending_card_header;
    private List<PrescriptionModel> model;
    private static SQLiteDatabase db;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_pending, container, false);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        visit_pending_card_header = view.findViewById(R.id.visit_pending_card_header);
        recycler_today = view.findViewById(R.id.recycler_today);
        recycler_week = view.findViewById(R.id.rv_thisweek);
        recycler_month = view.findViewById(R.id.rv_thismonth);

        visitData();
    }

    private void visitData() {
        visit_pending_card_header.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EndVisitActivity.class);
            startActivity(intent);
        });

        todays_Visits();
        thisWeeks_Visits();
        thisMonths_Visits();
    }


    private void todays_Visits() {
            List<PrescriptionModel> arrayList = new ArrayList<>();
            Date cDate = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

            db.beginTransaction();
        /**
         * Here we fetch all the visits for Today eg.10 except those visits for whom the presc is provided eg.2
         * So, it must return pending visits as eg. 10-2 = 8.
         */
        Cursor cursor = db.rawQuery("SELECT distinct(visituuid) FROM tbl_encounter where (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND voided = 0 AND (substr(modified_date, 1, 4) ||'-'|| substr(modified_date, 6,2) ||'-'|| substr(modified_date, 9,2)) = DATE('now') except " +
                    "SELECT distinct(visituuid) FROM tbl_encounter where (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND voided = 0 AND (substr(modified_date, 1, 4) ||'-'|| substr(modified_date, 6,2) ||'-'|| substr(modified_date, 9,2)) = DATE('now') " +
                    "and encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    PrescriptionModel model = new PrescriptionModel();

                  //  model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    model.setHasPrescription(false);
                    model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visituuid")));
                 //   model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                    // fetching patientuuid from visit table.
                    Cursor c = db.rawQuery("SELECT patientuuid FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                    if (c.getCount() > 0 && c.moveToFirst()) {
                        do {
                            model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));

                            // fetching patient values from Patient table.
                            Cursor p_c = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                            if (p_c.getCount() > 0 && p_c.moveToFirst()) {
                                do {
                                    model.setPatient_photo(p_c.getString(p_c.getColumnIndexOrThrow("patient_photo")));
                                    model.setFirst_name(p_c.getString(p_c.getColumnIndexOrThrow("first_name")));
                                    model.setLast_name(p_c.getString(p_c.getColumnIndexOrThrow("last_name")));
                                    arrayList.add(model);
                                }
                                while (p_c.moveToNext());
                            }
                            p_c.close();
                            // end

                        }
                        while (c.moveToNext());
                    }
                    c.close();
                    //end

                }
                while (cursor.moveToNext());
            }
            else {

            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();

          //  totalCounts_today = arrayList.size();
            VisitAdapter adapter_new = new VisitAdapter(getActivity(), arrayList);
            recycler_today.setAdapter(adapter_new);
        }


    private void thisWeeks_Visits() {
       // VisitAdapter adapter_new = new VisitAdapter(getActivity(), model);
      //  recycler_week.setAdapter(adapter_new);

        List<PrescriptionModel> arrayList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT distinct(visituuid) FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                "voided = 0 AND " +
                "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) " +
                "AND STRFTIME('%W',date(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2)) = STRFTIME('%W',DATE('now')) except " +

                "SELECT distinct(visituuid) FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                "voided = 0 AND " +
                "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) " +
                "AND STRFTIME('%W',date(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2)) = STRFTIME('%W',DATE('now'))" +
                "AND encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

              //  model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setHasPrescription(false);
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visituuid")));
              //  model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                // fetching patientuuid from visit table.
                Cursor c = db.rawQuery("SELECT patientuuid FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                if (c.getCount() > 0 && c.moveToFirst()) {
                    do {
                        model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));

                        // fetching patient values from Patient table.
                        Cursor p_c = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                        if (p_c.getCount() > 0 && p_c.moveToFirst()) {
                            do {
                                model.setPatient_photo(p_c.getString(p_c.getColumnIndexOrThrow("patient_photo")));
                                model.setFirst_name(p_c.getString(p_c.getColumnIndexOrThrow("first_name")));
                                model.setLast_name(p_c.getString(p_c.getColumnIndexOrThrow("last_name")));
                                arrayList.add(model);
                            }
                            while (p_c.moveToNext());
                        }
                        p_c.close();
                        // end

                    }
                    while (c.moveToNext());
                }
                c.close();
                //end

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

      //  totalCounts_week = arrayList.size();
        VisitAdapter adapter_new = new VisitAdapter(getActivity(), arrayList);
        recycler_week.setAdapter(adapter_new);
    }

    private void thisMonths_Visits() {
     //   VisitAdapter adapter_new = new VisitAdapter(getActivity(), model);
     //   recycler_month.setAdapter(adapter_new);

        List<PrescriptionModel> arrayList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT distinct(visituuid) FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                "voided = 0 AND " +
                "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%m',DATE('now')) except " +

                "SELECT distinct(visituuid) FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                "voided = 0 AND " +
                "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%m',DATE('now')) " +

                "AND " +
                "encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
              //  model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setHasPrescription(false);
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visituuid")));
              //  model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                // fetching patientuuid from visit table.
                Cursor c = db.rawQuery("SELECT patientuuid FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                if (c.getCount() > 0 && c.moveToFirst()) {
                    do {
                        model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));

                        // fetching patient values from Patient table.
                        Cursor p_c = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                        if (p_c.getCount() > 0 && p_c.moveToFirst()) {
                            do {
                                model.setPatient_photo(p_c.getString(p_c.getColumnIndexOrThrow("patient_photo")));
                                model.setFirst_name(p_c.getString(p_c.getColumnIndexOrThrow("first_name")));
                                model.setLast_name(p_c.getString(p_c.getColumnIndexOrThrow("last_name")));
                                arrayList.add(model);
                            }
                            while (p_c.moveToNext());
                        }
                        p_c.close();
                        // end

                    }
                    while (c.moveToNext());
                }
                c.close();
                //end

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

     //   totalCounts_month = arrayList.size();
        VisitAdapter adapter_new = new VisitAdapter(getActivity(), arrayList);
        recycler_month.setAdapter(adapter_new);
    }


}
