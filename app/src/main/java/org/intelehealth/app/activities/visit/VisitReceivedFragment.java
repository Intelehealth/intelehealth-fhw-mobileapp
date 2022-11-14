package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.VisitsDAO.getTotalCounts_EndVisit;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Prajwal Waingankar on 3/11/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class VisitReceivedFragment extends Fragment implements EndVisitCountsInterface {
    private RecyclerView recycler_today, recycler_week, recycler_month;
    private CardView visit_received_card_header;
    private static SQLiteDatabase db;
    private TextView received_endvisit_no, allvisits_txt, priority_visits_txt;
    int totalCounts = 0, totalCounts_today = 0, totalCounts_week = 0, totalCounts_month = 0;
    private ImageButton filter_icon, priority_cancel;
    private CardView filter_menu;
    private RelativeLayout filter_relative;
    private List<PrescriptionModel> todayList, weeksList, monthsList;
    private VisitAdapter todays_adapter, weeks_adapter, months_adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_received, container, false);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        visit_received_card_header = view.findViewById(R.id.visit_received_card_header);
         recycler_today = view.findViewById(R.id.recycler_today);
         recycler_week = view.findViewById(R.id.rv_thisweek);
         recycler_month = view.findViewById(R.id.rv_thismonth);
        received_endvisit_no = view.findViewById(R.id.received_endvisit_no);

        filter_icon = view.findViewById(R.id.filter_icon);
        filter_menu = view.findViewById(R.id.filter_menu);
        allvisits_txt = view.findViewById(R.id.allvisits_txt);
        priority_visits_txt = view.findViewById(R.id.priority_visits_txt);
        filter_relative = view.findViewById(R.id.filter_relative);
        priority_cancel = view.findViewById(R.id.priority_cancel);

        visit_received_card_header.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EndVisitActivity.class);
            startActivity(intent);
        });

        visitData();
    }

    private void visitData() {
        todays_Visits();
        thisWeeks_Visits();
        thisMonths_Visits();
        totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;

        // Total of End visits.
        int total = getTotalCounts_EndVisit();
        String htmlvalue = "<b>" + total + " Patients </b> visits are waiting for closure, please end the visit.";
        received_endvisit_no.setText(Html.fromHtml(htmlvalue));

        // Filter - start
        filter_icon.setOnClickListener(v -> {
            if (filter_menu.getVisibility() == View.VISIBLE)
                filter_menu.setVisibility(View.GONE);
            else
                filter_menu.setVisibility(View.VISIBLE);
        });

            priority_visits_txt.setOnClickListener(v -> {
                filter_relative.setVisibility(View.VISIBLE);    // display filter that is set tag.
                filter_menu.setVisibility(View.GONE);   // hide filter menu

                showOnlyPriorityVisits();
            });
        // Filter - end
    }

    /**
     * This function will display all the visit of Emergency who have been recived the presc.
     */
    private void showOnlyPriorityVisits() {
        // todays - start
        List<PrescriptionModel> prio_todays = new ArrayList<>();
        for (int i = 0; i < todayList.size(); i++) {
            if (todayList.get(i).isEmergency())
                prio_todays.add(todayList.get(i));
        }

        todays_adapter = new VisitAdapter(getActivity(), prio_todays);
        recycler_today.setAdapter(todays_adapter);
        // todays - end

        // weeks - start
        List<PrescriptionModel> prio_weeks = new ArrayList<>();
        for (int i = 0; i < weeksList.size(); i++) {
            if (weeksList.get(i).isEmergency())
                prio_weeks.add(weeksList.get(i));
        }

        weeks_adapter = new VisitAdapter(getActivity(), prio_weeks);
        recycler_week.setAdapter(weeks_adapter);
        // weeks - end

        // months - start
        List<PrescriptionModel> prio_months = new ArrayList<>();
        for (int i = 0; i < monthsList.size(); i++) {
            if (monthsList.get(i).isEmergency())
                prio_months.add(monthsList.get(i));
        }

        months_adapter = new VisitAdapter(getActivity(), prio_months);
        recycler_month.setAdapter(months_adapter);
        // months - end
    }

    private void todays_Visits() {
        todayList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                db.beginTransaction();
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                        "voided = 0 AND (substr(modified_date, 1, 4) ||'-'|| substr(modified_date, 6,2) ||'-'|| substr(modified_date, 9,2)) = DATE('now') AND" +
                        " encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        PrescriptionModel model = new PrescriptionModel();
                        // emergency - start
                        String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                        String emergencyUuid = "";
                        EncounterDAO encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                            model.setEmergency(true);
                        else
                            model.setEmergency(false);
                        // emergency - end

                        model.setHasPrescription(true);
                        model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                        model.setVisitUuid(visitID);
                        model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                        // fetching patientuuid from visit table.
                        Cursor c = db.rawQuery("SELECT * FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                        if (c.getCount() > 0 && c.moveToFirst()) {
                            do {
                                model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));
                                model.setVisit_start_date(c.getString(c.getColumnIndexOrThrow("startdate")));

                                // fetching patient values from Patient table.
                                Cursor p_c = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (p_c.getCount() > 0 && p_c.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(p_c.getString(p_c.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(p_c.getString(p_c.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(p_c.getString(p_c.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(p_c.getString(p_c.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(p_c.getString(p_c.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(p_c.getString(p_c.getColumnIndexOrThrow("gender")));
                                        todayList.add(model);
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

                totalCounts_today = todayList.size();
                // end

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        todays_adapter = new VisitAdapter(getActivity(), todayList);
                        recycler_today.setAdapter(todays_adapter);
                    }
                });
            }
        });

    }


    private void thisWeeks_Visits() {
        weeksList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                db.beginTransaction();
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                        "voided = 0 AND " +
                        "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) " +
                        "AND STRFTIME('%W',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%W',DATE('now')) AND " +
                        "encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        PrescriptionModel model = new PrescriptionModel();

                        // emergency - start
                        String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                        String emergencyUuid = "";
                        EncounterDAO encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                            model.setEmergency(true);
                        else
                            model.setEmergency(false);
                        // emergency - end

                        model.setHasPrescription(true);
                        model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                        model.setVisitUuid(visitID);
                        model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                        // fetching patientuuid from visit table.
                        Cursor c = db.rawQuery("SELECT * FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                        if (c.getCount() > 0 && c.moveToFirst()) {
                            do {
                                model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));
                                model.setVisit_start_date(c.getString(c.getColumnIndexOrThrow("startdate")));

                                // fetching patient values from Patient table.
                                Cursor p_c = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (p_c.getCount() > 0 && p_c.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(p_c.getString(p_c.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(p_c.getString(p_c.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(p_c.getString(p_c.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(p_c.getString(p_c.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(p_c.getString(p_c.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(p_c.getString(p_c.getColumnIndexOrThrow("gender")));
                                        weeksList.add(model);
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

                totalCounts_week = weeksList.size();
                // end

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        weeks_adapter = new VisitAdapter(getActivity(), weeksList);
                        recycler_week.setAdapter(weeks_adapter);
                    }
                });
            }
        });

    }

    private void thisMonths_Visits() {
        monthsList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                db.beginTransaction();
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                        "voided = 0 AND " +
                        "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                        "STRFTIME('%m',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%m',DATE('now')) AND " +
                        "encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        PrescriptionModel model = new PrescriptionModel();

                        // emergency - start
                        // TODO: 8-11-2022 -> In app currently in sync even when the visit is priority still in sync of app the emergency enc
                        //  is not getting added in local db ie. from server end emergency encounter is not coming to us in pull.
                        String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                        String emergencyUuid = "";
                        EncounterDAO encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                            model.setEmergency(true);
                        else
                            model.setEmergency(false);
                        // emergency - end

                        model.setHasPrescription(true);
                        model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                        model.setVisitUuid(visitID);
                        model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                        // fetching patientuuid from visit table.
                        Cursor c = db.rawQuery("SELECT * FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                        if (c.getCount() > 0 && c.moveToFirst()) {
                            do {
                                model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));
                                model.setVisit_start_date(c.getString(c.getColumnIndexOrThrow("startdate")));

                                // fetching patient values from Patient table.
                                Cursor p_c = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (p_c.getCount() > 0 && p_c.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(p_c.getString(p_c.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(p_c.getString(p_c.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(p_c.getString(p_c.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(p_c.getString(p_c.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(p_c.getString(p_c.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(p_c.getString(p_c.getColumnIndexOrThrow("gender")));
                                        monthsList.add(model);
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

                totalCounts_month = monthsList.size();
                // ednd

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        months_adapter = new VisitAdapter(getActivity(), monthsList);
                        recycler_month.setAdapter(months_adapter);
                    }
                });
            }
        });

    }


    @Override
    public int getPrescCount() {
        totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;
        return totalCounts;
    }
}
