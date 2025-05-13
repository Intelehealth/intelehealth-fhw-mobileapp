package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;

import org.intelehealth.app.ui.prescriptionwithotp.PrescriptionData;
import org.intelehealth.app.ui.prescriptionwithotp.SharePrescriptionViewModel;
import org.intelehealth.app.ui.prescriptionwithotp.SharePrescriptionViewModelFactory;
import org.intelehealth.app.ui.prescriptionwithotp.ShowPrescriptionDataPdfShareDialog;
import org.intelehealth.app.utilities.AddPatientUtils;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.PrescriptionLoadingListeners;
import org.intelehealth.app.utilities.VisitCountInterface;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.room.entity.FeatureActiveStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Prajwal Waingankar on 3/11/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class VisitReceivedFragment extends Fragment implements VisitAdapter.OnItemClickListener {
    public static final String TAG = "VisitReceivedFragment";
    private RecyclerView recycler_recent, recycler_older /*, recycler_month*/;
    private CardView visit_received_card_header;
    private static SQLiteDatabase db;
    private TextView received_endvisit_no, allvisits_txt, priority_visits_txt;
    int totalCounts = 0, totalCounts_recent = 0, totalCounts_older = 0, totalCounts_month = 0;
    private ImageButton filter_icon, priority_cancel;
    private CardView filter_menu;
    private RelativeLayout filter_relative, no_patient_found_block, main_block;
    private List<PrescriptionModel> mRecentList = new ArrayList<>();
    private List<PrescriptionModel> mOlderList = new ArrayList<>();
    private VisitAdapter recent_adapter, older_adapter;
    TextView recent_nodata, older_nodata, month_nodata;
    private androidx.appcompat.widget.SearchView searchview_received;
    private ImageView closeButton;
    private ProgressBar progress;
    private VisitCountInterface mlistener;
    private int recentLimit = 40, olderLimit = 40;
    private int recentStart = 0, recentEnd = recentStart + recentLimit;
    private boolean isRecentFullyLoaded = false;
    private int olderStart = 0, olderEnd = olderStart + olderLimit;
    private boolean isolderFullyLoaded = false;
    NestedScrollView nestedscrollview;
    List<PrescriptionModel> mRecentPrescriptionModelList = new ArrayList<>();
    List<PrescriptionModel> mOlderPrescriptionModelList = new ArrayList<>();
    private boolean isOlderPageLoading = false;
    private boolean isRecentPageLoading = false;
    PrescriptionLoadingListeners prescriptionLoadingListeners;
    private String searchQuery = "";
    private AlertDialog commonLoadingDialog;
    private FeatureActiveStatus mFeatureActiveStatus;

    public VisitReceivedFragment(PrescriptionLoadingListeners prescriptionLoadingListeners) {
        this.prescriptionLoadingListeners = prescriptionLoadingListeners;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_received, container, false);
        initUI(view);
        //setLocale(getContext());
        mlistener = (VisitCountInterface) getActivity();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        mFeatureActiveStatus =  ((VisitActivity) requireActivity()).getFeatureActiveStatus();
        if (mFeatureActiveStatus != null && mFeatureActiveStatus.getActiveStatusPrescriptionWithOtp()){
            older_adapter.setFeatureActiveStatus(mFeatureActiveStatus.getActiveStatusPrescriptionWithOtp());
            Log.d(TAG, "onViewCreated: mFeatureActiveStatus : "+mFeatureActiveStatus.getActiveStatusPrescriptionWithOtp());
        }
        //Log.d(TAG, "onViewCreated: mFeatureActiveStatus.getActiveStatusPrescriptionWithOtp() : "+mFeatureActiveStatus.getActiveStatusPrescriptionWithOtp());
        defaultData();
        visitData();
    }

   /* public Context setLocale(Context context) {
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
    }*/

    private void initUI(View view) {
        progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);

        ((TextView) view.findViewById(R.id.search_pat_hint_txt)).setText(getString(R.string.empty_message_for_patinet_search_visit_screen));
        LinearLayout addPatientTV = view.findViewById(R.id.add_new_patientTV);
        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPatientUtils.navigate(requireActivity());
                getActivity().finish();
            }
        });

        no_patient_found_block = view.findViewById(R.id.no_patient_found_block);
        main_block = view.findViewById(R.id.main_block);
        visit_received_card_header = view.findViewById(R.id.visit_received_card_header);
        searchview_received = view.findViewById(R.id.searchview_received);
        closeButton = searchview_received.findViewById(androidx.appcompat.R.id.search_close_btn);
        recent_nodata = view.findViewById(R.id.recent_nodata);
        older_nodata = view.findViewById(R.id.older_nodata);
        month_nodata = view.findViewById(R.id.month_nodata);
        recycler_recent = view.findViewById(R.id.recycler_recent);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recycler_recent.setLayoutManager(reLayoutManager);
        recycler_older = view.findViewById(R.id.rv_older);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recycler_older.setLayoutManager(layoutManager);
        nestedscrollview = view.findViewById(R.id.rece_nestedscroll);

        nestedscrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                // Scroll Down
                if (scrollY > oldScrollY) {
                    // update recent data as it will not go at very bottom of list.
                   /* if (mRecentList != null && mRecentList.size() == 0) {
                        isRecentFullyLoaded = true;
                    }*/
                    //this function will call each everytime endlessly
                    //that's is the cause of lagging and ANR
                    //hence moved this to bottom logic

                   /* if (!isRecentFullyLoaded && !isRecentPageLoading) {
                        setRecentMoreDataIntoRecyclerView();
                    }*/

                    // Last Item Scroll Down.
                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                        if (!isRecentFullyLoaded && !isRecentPageLoading) {
                            setRecentMoreDataIntoRecyclerView();
                        }

                        // update older data as it will not go at very bottom of list.
                        if (mOlderList != null && mOlderList.size() == 0) {
                            isolderFullyLoaded = true;
                            return;
                        }
                        if (!isolderFullyLoaded && !isOlderPageLoading) {
                            if (mRecentPrescriptionModelList != null && mOlderPrescriptionModelList != null) {
                                if (mRecentPrescriptionModelList.size() > 0 || mOlderPrescriptionModelList.size() > 0) {

                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.loading_more), Toast.LENGTH_SHORT).show();
                                    setOlderMoreDataIntoRecyclerView();
                                }
                            }
                        }
                    }
                }
            }
        });
        received_endvisit_no = view.findViewById(R.id.received_endvisit_no);
        filter_icon = view.findViewById(R.id.filter_icon);
        filter_menu = view.findViewById(R.id.filter_menu);
        allvisits_txt = view.findViewById(R.id.allvisits_txt);
        priority_visits_txt = view.findViewById(R.id.priority_visits_txt);
        filter_relative = view.findViewById(R.id.filter_relative);
        priority_cancel = view.findViewById(R.id.priority_cancel);
    }

    public void executeInBackground(Runnable task) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
        System.out.println("BaseDao.execute");
    }

    private void defaultData() {
        executeInBackground(fetchRecentData());
        executeInBackground(fetchOlderData());

        int totalCounts = totalCounts_recent + totalCounts_older;
        CustomLog.d("rece", "defaultData: received" + totalCounts);

//        thisMonths_Visits();
        if (mlistener != null)
            mlistener.receivedCount(totalCounts);
        progress.setVisibility(View.GONE);
    }

    private Runnable fetchOlderData() {
        // Older vistis
        // pagination - start
        return () -> {
            //  mOlderList = olderVisits(olderLimit, olderStart);   // olderLimit is constant = 40 but olderStart will keep iterating by 40 on every cycle.

            new Handler(Looper.getMainLooper()).post(() -> {    // Show loading indicator on UI thread
                //Toast.makeText(getActivity(), getString(R.string.loading_more), Toast.LENGTH_LONG).show();
                recycler_older.setVisibility(View.GONE); // Hide RecyclerView
                older_nodata.setVisibility(View.GONE); // Hide 'No Data' message
            });

            mOlderList = olderVisits(10, mOlderList.size());

           /* do {
             //   Timber.tag(TAG).v("fetchOlderData: 1st call: olderlimit, olderstart: " + olderLimit + " - " + olderStart);
                mOlderList = olderVisits(olderLimit, olderStart);
                if (mOlderList.isEmpty()) {
                    olderEnd = olderEnd + 40;
                    olderStart = olderStart + 40; // First update to 40, then double
                    // /*Timber.tag(TAG).v("do while loop older visits - start, end, limit: " + olderStart + "-" + olderEnd+ "-" + olderLimit);
                    //Timber.tag(TAG).v("==========================");
                }
            } while (mOlderList.isEmpty());*/ // Keep looping until we get some results

            new Handler(Looper.getMainLooper()).post(() -> {

                recycler_older.setVisibility(View.VISIBLE); // Show RecyclerView
                older_adapter = new VisitAdapter(getActivity(), mOlderList,this);
                applyFeatureStatusToAdapters();
                recycler_older.setNestedScrollingEnabled(false);
                recycler_older.setAdapter(older_adapter);

                totalCounts_older = mOlderList.size();
                if (totalCounts_older == 0)
                    older_nodata.setVisibility(View.VISIBLE);
                else
                    older_nodata.setVisibility(View.GONE);
            });

            olderStart = olderEnd;
            olderEnd += olderLimit;
            prescriptionLoadingListeners.isReceivedOldLoaded(true);

            // pagination - end
        };
    }

    private Runnable fetchRecentData() {
        return () -> {
            mRecentList = recentVisits(20, mRecentList.size());
            // pagination - start
            new Handler(Looper.getMainLooper()).post(() -> {    // UI Thread.
                recent_adapter = new VisitAdapter(getActivity(), mRecentList, this);
                recycler_recent.setNestedScrollingEnabled(false);
                applyFeatureStatusToAdapters();
                recycler_recent.setAdapter(recent_adapter);

                totalCounts_recent = mRecentList.size();
                if (totalCounts_recent == 0 || totalCounts_recent < 0)
                    recent_nodata.setVisibility(View.VISIBLE);
                else
                    recent_nodata.setVisibility(View.GONE);
            });
            recentStart = recentEnd;
            recentEnd += recentLimit;

            prescriptionLoadingListeners.isReceivedRecentLoaded(true);
            // pagination - end
        };
    }

    private void visitData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int total = new VisitsDAO().getVisitCountsByStatus(false);//getPendingPrescCount();
                Activity activity = getActivity();
                if (activity != null && isAdded()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String htmlvalue = "<b>" + total + " " + IntelehealthApplication.getInstance().getResources().getString(R.string.patients) + " " + "</b>" + IntelehealthApplication.getInstance().getResources().getString(R.string.awaiting_their_prescription);
                            received_endvisit_no.setText(Html.fromHtml(htmlvalue));
                        }
                    });
                }
            }
        }).start();


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
                if(!query.isEmpty()){
                    searchOperation(query);
                }
                return false;   // setting to false will close the keyboard when clicked on search btn.
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equalsIgnoreCase("")) {
                    searchview_received.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.blue_border_bg));
                } else {
                    searchview_received.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_common_input_bg));
                }
                return false;
            }
        });


        closeButton.setOnClickListener(v -> {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
//            defaultData();
            searchQuery = "";
            resetData();
            searchview_received.setQuery("", false);

        });
        // Search - end
    }

    private void recent_older_visibility(List<PrescriptionModel> recent, List<PrescriptionModel> older) {
        if (recent.size() == 0 || recent.size() < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);

        if (older.size() == 0 || older.size() < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    private void initLimits() {
        recentLimit = 15;
        olderLimit = 15;
        recentStart = 0;
        recentEnd = recentStart + recentLimit;
        olderStart = 0;
        olderEnd = olderStart + olderLimit;
    }


    private void resetData() {
        if (commonLoadingDialog == null) {
            commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(getActivity(), getString(R.string.loading), "");
            commonLoadingDialog.setCancelable(false);
        }else {
            commonLoadingDialog.show();
        }
        initLimits();
        mRecentList.clear();
        mOlderList.clear();
        executeInBackground(() -> {
            mRecentList = recentVisits(20, mRecentList.size());
            mOlderList = olderVisits(20, mOlderList.size());

            getActivity().runOnUiThread(() -> {
                recentStart = recentEnd;
                recentEnd += recentLimit;
                olderStart = olderEnd;
                olderEnd += olderLimit;

                //
                recent_older_visibility(mRecentList, mOlderList);
                CustomLog.d("TAG", "resetData: " + mRecentList.size() + ", " + mOlderList.size());

                recent_adapter = new VisitAdapter(getActivity(), mRecentList,this);
                recycler_recent.setNestedScrollingEnabled(false);
                recycler_recent.setAdapter(recent_adapter);

                older_adapter = new VisitAdapter(getActivity(), mOlderList,this);
                recycler_older.setNestedScrollingEnabled(false);
                recycler_older.setAdapter(older_adapter);

                if (commonLoadingDialog.isShowing()) {
                    commonLoadingDialog.dismiss();
                }
            });
        });

    }

    /**
     * This function will perform the search operation.
     *
     * @param query
     */
    private void searchOperation(String query) {
        CustomLog.v("Search", "Search Word: " + query);
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");
        if (commonLoadingDialog == null) {
            commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(getActivity(), getString(R.string.loading), "");
            commonLoadingDialog.setCancelable(false);
        }else {
            commonLoadingDialog.show();
        }

        searchQuery = query;

//        List<PrescriptionModel> recent = new ArrayList<>();
//        List<PrescriptionModel> older = new ArrayList<>();

        String finalQuery = query;

        mRecentList.clear();
        mOlderList.clear();
        executeInBackground(()->{
            mRecentList = recentVisits(20, mRecentList.size());
            mOlderList = olderVisits(20, mOlderList.size());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(recent_adapter == null){
                        recent_adapter = new VisitAdapter(getActivity(), mRecentList);
                        recycler_recent.setNestedScrollingEnabled(false);
                        recycler_recent.setAdapter(recent_adapter);
                    }else {
                        recent_adapter.resetAndAddData(mRecentList);
                    }

                    if(older_adapter == null){
                        older_adapter = new VisitAdapter(getActivity(), mOlderList);
                        recycler_older.setNestedScrollingEnabled(false);
                        recycler_older.setAdapter(older_adapter);
                    }else {
                        older_adapter.resetAndAddData(mOlderList);
                    }

                    /**
                     * Checking here the query that is entered and it is not empty so check the size of all of these
                     * arraylists; if there size is 0 than show the no patient found view.
                     */
                    int allCount = mRecentList.size() + mOlderList.size();
                    allCountVisibility(allCount);
                    recent_older_visibility(mRecentList, mOlderList);
                    if (commonLoadingDialog.isShowing()) {
                        commonLoadingDialog.dismiss();
                    }
                }
            });
        });

    }

    private void allCountVisibility(int allCount) {
        if (allCount == 0 || allCount < 0) {
            no_patient_found_block.setVisibility(View.VISIBLE);
            main_block.setVisibility(View.GONE);
        } else {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function will display all the visit of Emergency who have been recived the presc.
     */
    private void showOnlyPriorityVisits() {
        // todays - start
        List<PrescriptionModel> prio_todays = new ArrayList<>();
        for (int i = 0; i < mRecentList.size(); i++) {
            if (mRecentList.get(i).isEmergency())
                prio_todays.add(mRecentList.get(i));
        }
        totalCounts_recent = prio_todays.size();
        if (totalCounts_recent == 0 || totalCounts_recent < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);
        recent_adapter = new VisitAdapter(getActivity(), prio_todays,this);
        recycler_recent.setNestedScrollingEnabled(false);
        recycler_recent.setAdapter(recent_adapter);
        // todays - end

        // weeks - start
        List<PrescriptionModel> prio_weeks = new ArrayList<>();
        for (int i = 0; i < mOlderList.size(); i++) {
            if (mOlderList.get(i).isEmergency())
                prio_weeks.add(mOlderList.get(i));
        }
        totalCounts_older = prio_weeks.size();
        if (totalCounts_older == 0 || totalCounts_older < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
        older_adapter = new VisitAdapter(getActivity(), prio_weeks,this);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(older_adapter);
        // weeks - end

        // months - start
        List<PrescriptionModel> prio_months = new ArrayList<>();
       /* for (int i = 0; i < mMonthsList.size(); i++) {
            if (mMonthsList.get(i).isEmergency())
                prio_months.add(mMonthsList.get(i));
        }*/
        totalCounts_month = prio_months.size();
        if (totalCounts_month == 0 || totalCounts_month < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);
        //months_adapter = new VisitAdapter(getActivity(), prio_months);
        // recycler_month.setNestedScrollingEnabled(false);
        //recycler_month.setAdapter(months_adapter);
        // months - end
    }

    // This method will be accessed every time the person scrolls the recyclerView further.
    private void setRecentMoreDataIntoRecyclerView() {
        isRecentPageLoading = true;
        if (mRecentPrescriptionModelList.size() > 0 || mOlderPrescriptionModelList.size() > 0) {    // on scroll, new data loads issue fix.

        } else {
            if (mRecentList != null && mRecentList.size() == 0) {
                isRecentFullyLoaded = true;
                return;
            }

            //  recentList = recentVisits(recentLimit, recentStart);
            executeInBackground(()->{
                List<PrescriptionModel> tempList = recentVisits(20, mRecentList.size());  // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
                getActivity().runOnUiThread(()->{
                    if (tempList.size() > 0) {
                        mRecentList.addAll(tempList);
                        CustomLog.d("TAG", "setRecentMoreDataIntoRecyclerView: " + mRecentList.size());
                        recent_adapter.list.addAll(tempList);
                        recent_adapter.notifyDataSetChanged();
                        recentStart = recentEnd;
                        recentEnd += recentLimit;
                    }
                });
            });
        }
        isRecentPageLoading = false;
    }

    private void setOlderMoreDataIntoRecyclerView() {
        isOlderPageLoading = true;
        if (mRecentPrescriptionModelList.size() > 0 || mOlderPrescriptionModelList.size() > 0) {

        } else {
            if (mOlderList != null && mOlderList.isEmpty()) {
                isolderFullyLoaded = true;
                return;
            }
            //  Timber.tag(TAG).v("older visits fetch on scroll - olderstart, olderlimit: " + olderStart + "-" + olderLimit);
            executeInBackground(() -> {
                List<PrescriptionModel> tempList = olderVisits(20, mOlderList.size()); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
                if (tempList.size() > 0) {
                    getActivity().runOnUiThread(() -> {
                        mOlderList.addAll(tempList);
                        CustomLog.d("TAG", "setOlderMoreDataIntoRecyclerView: " + mOlderList.size());
                        older_adapter.list.addAll(tempList);
                        older_adapter.notifyDataSetChanged();
                        olderStart = olderEnd;
                        olderEnd += olderLimit;
                    });
                }
            });
        }

        isOlderPageLoading = false;
    }

    private List<PrescriptionModel> recentVisits(int limit, int offset) {
        List<PrescriptionModel> recentList = new ArrayList<>();
        db.beginTransaction();
        String searchQ = "";
        String middleName = "CASE WHEN p.middle_name IS NOT NULL THEN ' ' || p.middle_name || ' ' ELSE ' ' END";
        if (!searchQuery.isEmpty()) {
            searchQ = "and (patient_name_new LIKE " + "'%" + searchQuery + "%') ";
        }

        // ie. visit is active and presc is given.
        Cursor cursor = db.rawQuery("select p.patient_photo, " +
                        "p.first_name, p.middle_name, p.last_name, " +
                        "p.first_name || " + middleName + " || p.last_name as patient_name_new," +
                        "p.openmrs_id, p.date_of_birth, " +
                        "p.phone_number, " +
                        "p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync, " +
                        "CASE " +
                        "WHEN EXISTS (" +
                        "SELECT 1 FROM tbl_encounter e1 " +
                        " WHERE e1.visituuid = v.uuid " +
                        "AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' " +
                        ") THEN 1 ELSE 0 " +
                        "END AS has_exit_survey," +
                        "CASE " +
                        "WHEN EXISTS (" +
                        "SELECT 1 FROM tbl_encounter e2 " +
                        "WHERE e2.visituuid = v.uuid " +
                        "AND e2.encounter_type_uuid = ?" +
                        ") THEN 1 ELSE 0 " +
                        "END AS has_visit_complete " +
                        "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                        " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? and" +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 " +//and" + " o.conceptuuid = ? and " +
                        " and v.startdate > DATETIME('now', '-4 day') " +
                        "AND has_exit_survey = 0 " +
                        "AND has_visit_complete = 1 " +
                        searchQ +
                        " group by p.openmrs_id ORDER BY v.startdate DESC limit ? offset ?",

                new String[]{ENCOUNTER_VISIT_COMPLETE, ENCOUNTER_VISIT_COMPLETE, String.valueOf(limit), String.valueOf(offset)});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                //if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                String emergencyUuid = "";
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    emergencyUuid = "";
                }

                if (!emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
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
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                recentList.add(model);

                //  }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        if (recentList.isEmpty()) {
            isRecentFullyLoaded = true;
        }
        return recentList;
    }

    private List<PrescriptionModel> recentVisits() {
        List<PrescriptionModel> recentList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.


        //added sub query to handle isCompletedExitedSurvey and isPrescriptionReceived logic

        String query = "select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync, " +
                "CASE " +
                "WHEN EXISTS (" +
                "SELECT 1 FROM tbl_encounter e1 " +
                " WHERE e1.visituuid = v.uuid " +
                "AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' " +
                ") THEN 1 ELSE 0 " +
                "END AS has_exit_survey," +
                "CASE " +
                "WHEN EXISTS (" +
                "SELECT 1 FROM tbl_encounter e2 " +
                "WHERE e2.visituuid = v.uuid " +
                "AND e2.encounter_type_uuid = ?" +
                ") THEN 1 ELSE 0 " +
                "END AS has_visit_complete " +
                "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid " +
                //" and v.enddate is null " +
                "and e.encounter_type_uuid = ? and" +
                " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 " +//and" + " o.conceptuuid = ? and "+
                " and v.startdate > DATETIME('now', '-4 day') " +
                "AND has_exit_survey = 0 " +
                "AND has_visit_complete = 1 " +
                " group by p.openmrs_id ORDER BY v.startdate DESC";

        Cursor cursor = db.rawQuery(query,

                new String[]{ENCOUNTER_VISIT_COMPLETE, ENCOUNTER_VISIT_COMPLETE});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor != null && cursor.moveToFirst()) {
            // Move cursor to first row
            do {
                PrescriptionModel model = new PrescriptionModel();
                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
               /* try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }*/
                //if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                String emergencyUuid = "";
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    emergencyUuid = "";
                }

                if (!emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
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
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                recentList.add(model);

                // }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return recentList;
    }


    private List<PrescriptionModel>  olderVisits(int limit, int offset) {
        List<PrescriptionModel> olderList = new ArrayList<>();
        if (!db.isOpen()) {
            db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        }
        db.beginTransaction();

        // ie. visit is active and presc is given.
        /*Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                        " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid " +
                        "and e.encounter_type_uuid = ? and" +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 " +//and" + " o.conceptuuid = ?  "+
                        " and v.startdate <= DATE('now', '-4 day') " +
                        "group by p.openmrs_id ORDER BY v.startdate DESC limit ? offset ?",

                new String[]{ENCOUNTER_VISIT_COMPLETE, String.valueOf(limit), String.valueOf(offset)});*/

        //added sub query to handle isCompletedExitedSurvey and isPrescriptionReceived logic

        String searchQ = "";
        String middleName = "CASE WHEN p.middle_name IS NOT NULL THEN ' ' || p.middle_name || ' ' ELSE ' ' END";
        if (!searchQuery.isEmpty()) {
            searchQ = "and (patient_name_new LIKE " + "'%" + searchQuery + "%') ";
        }

        String query = "SELECT p.patient_photo, p.first_name, p.middle_name, p.last_name, " +
                "p.first_name || " + middleName + " || p.last_name as patient_name_new," +
                "p.openmrs_id, " +
                "p.date_of_birth, p.phone_number, p.gender, v.startdate, " +
                "v.patientuuid, e.visituuid, " +
                "e.uuid AS euid, o.uuid AS ouid, o.obsservermodifieddate, o.sync AS osync, " +
                "CASE " +
                "WHEN EXISTS (" +
                "SELECT 1 FROM tbl_encounter e1 " +
                " WHERE e1.visituuid = v.uuid " +
                "AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' " +
                ") THEN 1 ELSE 0 " +
                "END AS has_exit_survey," +
                "CASE " +
                "WHEN EXISTS (" +
                "SELECT 1 FROM tbl_encounter e2 " +
                "WHERE e2.visituuid = v.uuid " +
                "AND e2.encounter_type_uuid = ?" +
                ") THEN 1 ELSE 0 " +
                "END AS has_visit_complete " +
                "FROM tbl_patient p " +
                "JOIN tbl_visit v ON p.uuid = v.patientuuid " +
                "JOIN tbl_encounter e ON v.uuid = e.visituuid " +
                "JOIN tbl_obs o ON e.uuid = o.encounteruuid " +
                "WHERE e.encounter_type_uuid = ? " +
                "AND (o.sync = 1 OR LOWER(o.sync) = 'true') " +
                "AND o.voided = 0 " +
                "AND v.startdate <= DATE('now', '-4 day') " +
                "AND has_exit_survey = 0 " +
                "AND has_visit_complete = 1 " +
                searchQ +
                "GROUP BY p.openmrs_id " +
                "ORDER BY v.startdate DESC " +
                "LIMIT ? OFFSET ?";

        Log.d("QQQQQ",query);

        Cursor cursor = db.rawQuery(query,
                new String[]{ENCOUNTER_VISIT_COMPLETE, ENCOUNTER_VISIT_COMPLETE, String.valueOf(limit), String.valueOf(offset)}
        );
        // not needed as diagnosis is not mandatoy. --> 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor != null && cursor.moveToFirst()) {
            // Move cursor to first row
            Log.v("VisitReceived", "oldervisits: " + cursor.getCount());
            do {
                PrescriptionModel model = new PrescriptionModel();

                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                //if(!isPrescriptionReceived) continue;
                // if (!isCompletedExitedSurvey && isPrescriptionReceived) {
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
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                olderList.add(model);
                // }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return olderList;
    }

    private List<PrescriptionModel> olderVisits() {
        List<PrescriptionModel> olderList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.

        //added sub query to handle isCompletedExitedSurvey and isPrescriptionReceived logic
        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync, " +
                        "CASE " +
                        "WHEN EXISTS (" +
                        "SELECT 1 FROM tbl_encounter e1 " +
                        " WHERE e1.visituuid = v.uuid " +
                        "AND e1.encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' " +
                        ") THEN 1 ELSE 0 " +
                        "END AS has_exit_survey," +
                        "CASE " +
                        "WHEN EXISTS (" +
                        "SELECT 1 FROM tbl_encounter e2 " +
                        "WHERE e2.visituuid = v.uuid " +
                        "AND e2.encounter_type_uuid = ?" +
                        ") THEN 1 ELSE 0 " +
                        "END AS has_visit_complete " +
                        " from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                        " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? and" +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0  " +//and" + " o.conceptuuid = ? and "+
                        " and v.startdate <= DATE('now', '-4 day') " +
                        "AND has_exit_survey = 0 " +
                        "AND has_visit_complete = 1 " +
                        "group by p.openmrs_id ORDER BY v.startdate DESC",

                new String[]{ENCOUNTER_VISIT_COMPLETE});  // not needed as diagnosis is not mandatoy. --> 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor != null && cursor.moveToFirst()) {
            // Move cursor to first row
            do {
                PrescriptionModel model = new PrescriptionModel();

                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
               /* try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }*/
                // if (!isCompletedExitedSurvey && isPrescriptionReceived) {
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
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                olderList.add(model);
                // }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return olderList;
    }
    private Boolean featureStatusWithOtp = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof VisitActivity) {
            ((VisitActivity) context).setFeatureStatusListener(new VisitActivity.OnFeatureStatusReadyListener() {
                @Override
                public void onFeatureStatusReady(FeatureActiveStatus status) {
                    if (status != null) {
                        featureStatusWithOtp = status.getActiveStatusPrescriptionWithOtp();
                        Log.d("VisitFragment", "Feature status received: " + featureStatusWithOtp);

                        // Apply status if adapters already initialized
                        applyFeatureStatusToAdapters();
                    }
                }
            });
        }
    }

    private void applyFeatureStatusToAdapters() {
        if (recent_adapter != null && featureStatusWithOtp != null) {
            recent_adapter.setFeatureActiveStatus(featureStatusWithOtp);
        }
        if (older_adapter != null && featureStatusWithOtp != null) {
            older_adapter.setFeatureActiveStatus(featureStatusWithOtp);
        }
    }

    @Override
    public void onItemClicked(PrescriptionModel data) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        SharePrescriptionViewModelFactory factory = new SharePrescriptionViewModelFactory(db);
        SharePrescriptionViewModel viewModel = new ViewModelProvider(this, factory).get(SharePrescriptionViewModel.class);
        viewModel.loadPrescriptionDataFromJava(data.getPatientUuid(), data.getVisitUuid(), dataPdf -> {
                    ShowPrescriptionDataPdfShareDialog helper =
                            new ShowPrescriptionDataPdfShareDialog(getActivity(), dataPdf, data.getOpenmrs_id(), data.getPatientUuid(), data.getVisitUuid(), data.isHasPrescription());
                    helper.sharePrescriptionInPdf();
                    return null;
                },
                throwable -> {
                    return null;
                }
        );

    }
}
