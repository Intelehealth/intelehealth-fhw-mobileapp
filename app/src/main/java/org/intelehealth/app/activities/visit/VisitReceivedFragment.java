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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.BaseProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.VisitCountInterface;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

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
    private RelativeLayout filter_relative, no_patient_found_block, main_block;
    private List<PrescriptionModel> todayList, weeksList, monthsList;
    private VisitAdapter todays_adapter, weeks_adapter, months_adapter;
    TextView today_nodata, week_nodata, month_nodata;
    private androidx.appcompat.widget.SearchView searchview_received;
    private ImageView closeButton;
    private ProgressBar progress;
    private VisitCountInterface mlistener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_received, container, false);
        initUI(view);
        mlistener = (VisitCountInterface) getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        defaultData();
        visitData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initUI(View view) {
        progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);

        no_patient_found_block = view.findViewById(R.id.no_patient_found_block);
        main_block = view.findViewById(R.id.main_block);

        visit_received_card_header = view.findViewById(R.id.visit_received_card_header);
        searchview_received = view.findViewById(R.id.searchview_received);
        closeButton = searchview_received.findViewById(R.id.search_close_btn);

        today_nodata = view.findViewById(R.id.today_nodata);
        week_nodata = view.findViewById(R.id.week_nodata);
        month_nodata = view.findViewById(R.id.month_nodata);

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
    }

    private void defaultData() {
        todays_Visits();
        thisWeeks_Visits();
        thisMonths_Visits();

        totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;
        if (mlistener != null)
            mlistener.receivedCount(totalCounts);

    }

    private void visitData() {

        // Total no. of End visits.
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

        priority_cancel.setOnClickListener(v -> {
            filter_relative.setVisibility(View.GONE);   // on clicking on cancel for Priority remove the filter tag as well as reset the data as default one.
            defaultData();
        });
        // Filter - end

        // Search - start
        searchview_received.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchOperation(query);
                return false;   // setting to false will close the keyboard when clicked on search btn.
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equalsIgnoreCase("")) {
                    searchview_received.setBackground(getResources().getDrawable(R.drawable.blue_border_bg));
                }
                else {
                    searchview_received.setBackground(getResources().getDrawable(R.drawable.ui2_common_input_bg));
                }
                return false;
            }
        });


        closeButton.setOnClickListener(v -> {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
            defaultData();
            searchview_received.setQuery("", false);

        });
        // Search - end
    }

    /**
     * This function will perform the search operation.
     * @param query
     */
    private void searchOperation(String query) {
        Log.v("Search", "Search Word: " + query);

        List<PrescriptionModel> today = new ArrayList<>();
        List<PrescriptionModel> week = new ArrayList<>();
        List<PrescriptionModel> month = new ArrayList<>();

        today.addAll(todayList);
        week.addAll(weeksList);
        month.addAll(monthsList);

        if (!query.isEmpty()) {

            // todays - start
            today.clear();
            for (PrescriptionModel model : todayList) {
                if (model.getFirst_name().toLowerCase().contains(query) || model.getLast_name().toLowerCase().contains(query)) {
                    today.add(model);
                }
                else {
                    // dont add in list value.
                }

                totalCounts_today = today.size();
                if(totalCounts_today == 0 || totalCounts_today < 0)
                    today_nodata.setVisibility(View.VISIBLE);
                else
                    today_nodata.setVisibility(View.GONE);
                todays_adapter = new VisitAdapter(getActivity(), today);
                recycler_today.setNestedScrollingEnabled(false);
                recycler_today.setAdapter(todays_adapter);
            }
            // todays - end

            // weeks - start
            week.clear();
            for (PrescriptionModel model : weeksList) {
                if (model.getFirst_name().toLowerCase().contains(query) || model.getLast_name().toLowerCase().contains(query)) {
                    week.add(model);
                }
                else {
                    // do nothing
                }

                totalCounts_week = week.size();
                if(totalCounts_week == 0 || totalCounts_week < 0)
                    week_nodata.setVisibility(View.VISIBLE);
                else
                    week_nodata.setVisibility(View.GONE);
                weeks_adapter = new VisitAdapter(getActivity(), week);
                recycler_week.setNestedScrollingEnabled(false);
                recycler_week.setAdapter(weeks_adapter);
            }
            // weeks - end

            // months - start
            month.clear();
            for (PrescriptionModel model : monthsList) {
                if (model.getFirst_name().toLowerCase().contains(query) || model.getLast_name().toLowerCase().contains(query)) {
                    month.add(model);
                }
                else {
                    // do nothing
                }

                totalCounts_month = month.size();
                if(totalCounts_month == 0 || totalCounts_month < 0)
                    month_nodata.setVisibility(View.VISIBLE);
                else
                    month_nodata.setVisibility(View.GONE);
                months_adapter = new VisitAdapter(getActivity(), month);
                recycler_month.setNestedScrollingEnabled(false);
                recycler_month.setAdapter(months_adapter);
            }
            // months - end

            /**
             * Checking here the query that is entered and it is not empty so check the size of all of these
             * arraylists; if there size is 0 than show the no patient found view.
             */
            totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;
            if (totalCounts <= 0) {
                no_patient_found_block.setVisibility(View.VISIBLE);
                main_block.setVisibility(View.GONE);
            }
            else {
                no_patient_found_block.setVisibility(View.GONE);
                main_block.setVisibility(View.VISIBLE);
            }

        }
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
        totalCounts_today = prio_todays.size();
        if(totalCounts_today == 0 || totalCounts_today < 0)
            today_nodata.setVisibility(View.VISIBLE);
        else
            today_nodata.setVisibility(View.GONE);
        todays_adapter = new VisitAdapter(getActivity(), prio_todays);
        recycler_today.setNestedScrollingEnabled(false);
        recycler_today.setAdapter(todays_adapter);
        // todays - end

        // weeks - start
        List<PrescriptionModel> prio_weeks = new ArrayList<>();
        for (int i = 0; i < weeksList.size(); i++) {
            if (weeksList.get(i).isEmergency())
                prio_weeks.add(weeksList.get(i));
        }
        totalCounts_week = prio_weeks.size();
        if(totalCounts_week == 0 || totalCounts_week < 0)
            week_nodata.setVisibility(View.VISIBLE);
        else
            week_nodata.setVisibility(View.GONE);
        weeks_adapter = new VisitAdapter(getActivity(), prio_weeks);
        recycler_week.setNestedScrollingEnabled(false);
        recycler_week.setAdapter(weeks_adapter);
        // weeks - end

        // months - start
        List<PrescriptionModel> prio_months = new ArrayList<>();
        for (int i = 0; i < monthsList.size(); i++) {
            if (monthsList.get(i).isEmergency())
                prio_months.add(monthsList.get(i));
        }
        totalCounts_month = prio_months.size();
        if(totalCounts_month == 0 || totalCounts_month < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);
        months_adapter = new VisitAdapter(getActivity(), prio_months);
        recycler_month.setNestedScrollingEnabled(false);
        recycler_month.setAdapter(months_adapter);
        // months - end
    }

    private void todays_Visits() {
        // new 
        todayList = new ArrayList<>();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                " e.encounter_type_uuid = ? and" +
                " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" +
                " o.conceptuuid = ? and " +
                " (substr(o.obsservermodifieddate, 1, 4) ||'-'|| substr(o.obsservermodifieddate, 6,2) ||'-'|| substr(o.obsservermodifieddate, 9,2)) = DATE('now') group by p.openmrs_id "
                , new String[]{ENCOUNTER_VISIT_NOTE, "537bb20d-d09d-4f88-930b-cc45c7d662df"});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

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
                model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                model.setVisitUuid(visitID);
                model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                todayList.add(model);

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        totalCounts_today = todayList.size();
        if(totalCounts_today == 0 || totalCounts_today < 0)
            today_nodata.setVisibility(View.VISIBLE);
        else
            today_nodata.setVisibility(View.GONE);

        todays_adapter = new VisitAdapter(getActivity(), todayList);
        recycler_today.setNestedScrollingEnabled(false);
        recycler_today.setAdapter(todays_adapter);

      //  thisWeeks_Visits();
        //new
        
        
        
        
        
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());

/*
        executor.execute(new Runnable() {
            @Override
            public void run() {
                todayList = new ArrayList<>();
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
                                Cursor cursor = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                                        todayList.add(model);
                                    }
                                    while (cursor.moveToNext());
                                }
                                cursor.close();
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
                        if(totalCounts_today == 0 || totalCounts_today < 0)
                            today_nodata.setVisibility(View.VISIBLE);
                        else
                            today_nodata.setVisibility(View.GONE);

                        todays_adapter = new VisitAdapter(getActivity(), todayList);
                        recycler_today.setNestedScrollingEnabled(false);
                        recycler_today.setAdapter(todays_adapter);

                        thisWeeks_Visits();
                    }
                });
            }
        });
*/

    }


    private void thisWeeks_Visits() {
        // new
        weeksList = new ArrayList<>();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                "  e.encounter_type_uuid = ? and" +
                " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" +
                " o.conceptuuid = ? and" +
                " STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 4)||'-'||substr(o.obsservermodifieddate, 6, 2)||'-'||substr(o.obsservermodifieddate, 9,2))) = STRFTIME('%Y',DATE('now'))" +
                " AND STRFTIME('%W',date(substr(o.obsservermodifieddate, 1, 4)||'-'||substr(o.obsservermodifieddate, 6, 2)||'-'||substr(o.obsservermodifieddate, 9,2))) = STRFTIME('%W',DATE('now'))" +
                " group by p.openmrs_id"
                , new String[]{ENCOUNTER_VISIT_NOTE, "537bb20d-d09d-4f88-930b-cc45c7d662df"});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

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
                model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                model.setVisitUuid(visitID);
                model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                weeksList.add(model);

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        totalCounts_week = weeksList.size();
        if(totalCounts_week == 0 || totalCounts_week < 0)
            week_nodata.setVisibility(View.VISIBLE);
        else
            week_nodata.setVisibility(View.GONE);

        weeks_adapter = new VisitAdapter(getActivity(), weeksList);
        recycler_week.setNestedScrollingEnabled(false);
        recycler_week.setAdapter(weeks_adapter);

        //  thisWeeks_Visits();
        //new

     /*   ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                weeksList = new ArrayList<>();
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
                                Cursor cursor1 = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                                        weeksList.add(model);
                                    }
                                    while (cursor.moveToNext());
                                }
                                cursor.close();
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
                        if(totalCounts_week == 0 || totalCounts_week < 0)
                            week_nodata.setVisibility(View.VISIBLE);
                        else
                            week_nodata.setVisibility(View.GONE);

                        weeks_adapter = new VisitAdapter(getActivity(), weeksList);
                        recycler_week.setNestedScrollingEnabled(false);
                        recycler_week.setAdapter(weeks_adapter);
                        thisMonths_Visits();
                    }
                });
            }
        });*/

    }

    private void thisMonths_Visits() {
        // new
        monthsList = new ArrayList<>();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                        " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                        "  e.encounter_type_uuid = ? and" +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" +
                        " o.conceptuuid = ? and" +
                        " STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 4)||'-'||substr(o.obsservermodifieddate, 6, 2)||'-'||substr(o.obsservermodifieddate, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                        " STRFTIME('%m',date(substr(o.obsservermodifieddate, 1, 4)||'-'||substr(o.obsservermodifieddate, 6, 2)||'-'||substr(o.obsservermodifieddate, 9,2))) = STRFTIME('%m',DATE('now'))" +
                        " group by p.openmrs_id"
                , new String[]{ENCOUNTER_VISIT_NOTE, "537bb20d-d09d-4f88-930b-cc45c7d662df"});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

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
                model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                model.setVisitUuid(visitID);
                model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                monthsList.add(model);

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        totalCounts_month = monthsList.size();
        if(totalCounts_month == 0 || totalCounts_month < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);

        months_adapter = new VisitAdapter(getActivity(), monthsList);
        recycler_month.setNestedScrollingEnabled(false);
        recycler_month.setAdapter(months_adapter);
        progress.setVisibility(View.GONE);

        //  thisWeeks_Visits();
        //new


      /*  ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                monthsList = new ArrayList<>();
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
                                Cursor cursor2 = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                                        monthsList.add(model);

                                    }
                                    while (cursor.moveToNext());
                                }
                                cursor.close();
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
                        if(totalCounts_month == 0 || totalCounts_month < 0)
                            month_nodata.setVisibility(View.VISIBLE);
                        else
                            month_nodata.setVisibility(View.GONE);

                        months_adapter = new VisitAdapter(getActivity(), monthsList);
                        recycler_month.setNestedScrollingEnabled(false);
                        recycler_month.setAdapter(months_adapter);
                        progress.setVisibility(View.GONE);
                    }
                });
            }
        });
*/
    }


    @Override
    public int getPrescCount() {
        totalCounts = totalCounts_today + totalCounts_week + totalCounts_month;
        return totalCounts;
    }
}
