package org.intelehealth.app.activities.homeActivity;

import static org.intelehealth.app.database.dao.VisitsDAO.olderNotEndedVisits;
import static org.intelehealth.app.database.dao.VisitsDAO.recentNotEndedVisits;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.apache.commons.lang3.time.DateUtils;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientActivity_New;
import org.intelehealth.app.activities.visit.EndVisitActivity;
import org.intelehealth.app.activities.visit.VisitActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointmentNew.MyAppointmentActivity;
import org.intelehealth.app.appointmentNew.UpdateFragmentOnEvent;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment_New extends Fragment implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "HomeFragment_New";
    View view;
    SessionManager sessionManager;
    CardView followup_cardview, addpatient_cardview;
    TextView textlayout_find_patient;
    NetworkUtils networkUtils;
    ImageView ivInternet;
    private TextView mUpcomingAppointmentCountTextView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_ui2, container, false);
        networkUtils = new NetworkUtils(getActivity(), this);
        setLocale(getContext());
        return view;
    }

    private static SQLiteDatabase db;

    private int getCurrentMonthsVisits(boolean isForReceivedPrescription) {
        int count = 0;
        db.beginTransactionNonExclusive();

        Cursor cursor = null;
        if (isForReceivedPrescription)
            cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                            " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                            " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                            "  e.encounter_type_uuid = ? and" +
                            " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" +
                            " o.conceptuuid = ? and" +
                            " STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%Y',DATE('now')) AND " +
                            " STRFTIME('%m',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%m',DATE('now'))" +
                            " group by e.visituuid"
                    , new String[]{ENCOUNTER_VISIT_NOTE, "537bb20d-d09d-4f88-930b-cc45c7d662df"});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.
        else
            cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                            " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                            " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                            //" e.encounter_type_uuid = ?  and " +
                            " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" +
                            " " +
                            " STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%Y',DATE('now')) AND " +
                            " STRFTIME('%m',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%m',DATE('now'))" +
                            "  group by e.visituuid"
                    , new String[]{});
        db.setTransactionSuccessful();
        db.endTransaction();
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {

                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                //TODO: need more improvement in main query, this condition can be done by join query
                if(isForReceivedPrescription) {
                    if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                        count += 1;
                    }
                }else{
                    if (!isCompletedExitedSurvey && !isPrescriptionReceived) {
                        count += 1;
                    }
                }
            }
            while (cursor.moveToNext());
        }


        cursor.close();


        return count;
    }

    private int getThisMonthsNotEndedVisits() {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name, p.last_name, v.startdate " +
                "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                "v.voided = 0 AND " +
                "STRFTIME('%Y',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%m',DATE('now')) AND " +
                "v.enddate IS NULL", new String[]{});

        count = cursor.getCount();

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return count;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((HomeScreenActivity_New) getActivity()).initUpdateFragmentOnEvent(new UpdateFragmentOnEvent() {
            @Override
            public void onStart(int eventFlag) {
                Log.v(TAG, "onStart");
            }

            @Override
            public void onFinished(int eventFlag) {
                Log.v(TAG, "onFinished");
                initUI();
            }
        });
    }

    private void initUI() {
        sessionManager = new SessionManager(getActivity());
        View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        layoutToolbar.setVisibility(View.VISIBLE);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        }

        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        ImageView viewHamburger = requireActivity().findViewById(R.id.iv_hamburger);
        viewHamburger.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_hamburger));
        DrawerLayout mDrawerLayout = requireActivity().findViewById(R.id.drawer_layout);

        /*if (viewHamburger != null) {
            viewHamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                }
            });
        } else {
            Log.d(TAG, "clickListeners: iv_hamburger null");
        }*/
        mUpcomingAppointmentCountTextView = requireActivity().findViewById(R.id.textView5);
        TextView tvLocation = requireActivity().findViewById(R.id.tv_user_location_home);
        tvLocation.setText(StringUtils.translateLocation(sessionManager.getLocationName(), sessionManager.getAppLanguage()));
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ui2_ic_location_home, 0);
        TextView tvLastSyncApp = requireActivity().findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = requireActivity().findViewById(R.id.imageview_notifications_home);
        tvLastSyncApp.setVisibility(View.VISIBLE);
        ivNotification.setVisibility(View.VISIBLE);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.VISIBLE);
        ivInternet = requireActivity().findViewById(R.id.imageview_is_internet);


        CardView cardAppointment = view.findViewById(R.id.cardView4_appointment);
        CardView closedVisitsCardView = view.findViewById(R.id.closedVisitsCardView);
        CardView card_prescription = view.findViewById(R.id.card_prescription);

        cardAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyAppointmentActivity.class);
                startActivity(intent);

            }
        });

        closedVisitsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), VisitSummaryActivity_New.class);
                startActivity(intent);*/
                Intent intent = new Intent(getActivity(), EndVisitActivity.class);
                startActivity(intent);
            }
        });

        card_prescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VisitActivity.class);
                startActivity(intent);
            }
        });

        //
        TextView prescriptionCountTextView = view.findViewById(R.id.textview_received_no);
        int pendingCountTotalVisits = getCurrentMonthsVisits(false);
        int countReceivedPrescription = getCurrentMonthsVisits(true);

        int total = pendingCountTotalVisits + countReceivedPrescription;
        prescriptionCountTextView.setText(countReceivedPrescription + " " +
                getResources().getString(R.string.out_of) + " "  + total + " " +
                getResources().getString(R.string.received).toLowerCase());

      //  int countPendingCloseVisits = getThisMonthsNotEndedVisits();    // error: IDA: 1337 - fetching wrong data.
        TextView countPendingCloseVisitsTextView = view.findViewById(R.id.textview_close_visit_no);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int countPendingCloseVisits = recentNotEndedVisits().size() + olderNotEndedVisits().size();    // IDA: 1337 - fetching wrong data.
                getActivity().runOnUiThread(() -> countPendingCloseVisitsTextView.setText(countPendingCloseVisits + " " + getResources().getString(R.string.unclosed_visits)));
            }
        }).start();

        getUpcomingAppointments();

        int count = countPendingFollowupVisits();
        TextView countPendingFollowupVisitsTextView = view.findViewById(R.id.textView6);
        countPendingFollowupVisitsTextView.setText(count + " " + getResources().getString(R.string.pending));
    }

    @Override
    public void onResume() {
        super.onResume();
        setLocale(getContext());
        initUI();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        followup_cardview = view.findViewById(R.id.followup_cardview);
        addpatient_cardview = view.findViewById(R.id.addpatient_cardview);
        textlayout_find_patient = view.findViewById(R.id.textlayout_find_patient);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textlayout_find_patient.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchPatientActivity_New.class);
            startActivity(intent);
        });

        followup_cardview.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FollowUpPatientActivity_New.class);
            startActivity(intent);
        });

        addpatient_cardview.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PrivacyPolicyActivity_New.class);
            intent.putExtra("intentType", "navigateFurther");
            intent.putExtra("add_patient", "add_patient");
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }

    private void getUpcomingAppointments() {
        //recyclerview for upcoming appointments
        int totalUpcomingApps = 0;
        //SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat1.format(new Date());
        String endDate = dateFormat1.format(DateUtils.addYears(new Date(), 1));

        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointmentsWithFiltersV1(currentDate, endDate, "");
        List<AppointmentInfo> upcomingAppointmentsList = new ArrayList<>();

        try {
            if (appointmentInfoList.size() > 0) {
                for (int i = 0; i < appointmentInfoList.size(); i++) {
                    AppointmentInfo appointmentInfo = appointmentInfoList.get(i);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                    String currentDateTime = dateFormat.format(new Date());
                    String slottime = appointmentInfo.getSlotDate() + " " + appointmentInfo.getSlotTime();

                    long diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();

                    long second = diff / 1000;
                    long minutes = second / 60;
                    if (appointmentInfo.getStatus().equalsIgnoreCase("booked") && minutes >= 0) {
                        upcomingAppointmentsList.add(appointmentInfo);
                    }
                }
                totalUpcomingApps = upcomingAppointmentsList.size();


            } else {
                totalUpcomingApps = 0;
            }
            mUpcomingAppointmentCountTextView.setText(totalUpcomingApps + " " + getResources().getString(R.string.upcoming));

        } catch (
                Exception e) {
            e.printStackTrace();
        }


    }

    public int countPendingFollowupVisits() {
        int count = 0;

        // TODO: end date is removed later add it again. --> Added...
        String query = "SELECT a.uuid as visituuid, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate, " +
                "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info," +
                "b.patient_photo, a.enddate, b.uuid, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text, o.obsservermodifieddate " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE " +
                "a.uuid = c.visit_uuid AND   a.enddate is NOT NULL AND a.patientuuid = b.uuid AND " +
                "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND " +
                "STRFTIME('%Y',date(substr(o.value, 1, 10))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(o.value, 1, 10))) = STRFTIME('%m',DATE('now')) AND " +
                "o.value is NOT NULL GROUP BY a.patientuuid";

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    // Fetch encounters who have emergency set and udpate modelist.
                    String visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                    boolean isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitUuid);
                    if (isCompletedExitedSurvey) {
                        count += 1;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
//                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return count;
    }
}

