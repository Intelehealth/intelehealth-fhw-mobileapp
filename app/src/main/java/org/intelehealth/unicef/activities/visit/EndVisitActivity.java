package org.intelehealth.unicef.activities.visit;

import static org.intelehealth.unicef.database.dao.VisitsDAO.olderNotEndedVisits;
import static org.intelehealth.unicef.database.dao.VisitsDAO.recentNotEndedVisits;
import static org.intelehealth.unicef.syncModule.SyncUtils.syncNow;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.BaseActivity;
import org.intelehealth.unicef.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.models.PrescriptionModel;
import org.intelehealth.unicef.utilities.NetworkUtils;
import org.intelehealth.unicef.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class EndVisitActivity extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    RecyclerView recycler_recent, recycler_older, recycler_month;
    private static SQLiteDatabase db;
    private int total_counts = 0, todays_count = 0, weeks_count = 0, months_count = 0;
    private ImageButton backArrow, refresh;
    TextView recent_nodata, older_nodata, month_nodata;
    private NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;
    private SessionManager sessionManager;

    private int recentLimit = 15, olderLimit = 15;
    private int recentStart = 0, recentEnd = recentStart + recentLimit;
    private boolean isRecentFullyLoaded = false;

    private int olderStart = 0, olderEnd = olderStart + olderLimit;
    private boolean isolderFullyLoaded = false;

    private List<PrescriptionModel> recentCloseVisitsList, olderCloseVisitsList;
    private EndVisitAdapter recentVisitsAdapter, olderVisitsAdapter;
    private androidx.appcompat.widget.SearchView searchview_received;
    private ImageView closeButton;
    private Context context = EndVisitActivity.this;
    private RelativeLayout no_patient_found_block, main_block;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_visit);
        sessionManager = new SessionManager(this);
        setLocale(sessionManager.getAppLanguage());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        networkUtils = new NetworkUtils(this, this);
        initViews();
        endVisits_data();

        refresh.setOnClickListener(v -> {
            syncNow(EndVisitActivity.this, refresh, syncAnimator);
        });
    }

    private void initViews() {
        recycler_recent = findViewById(R.id.recycler_recent);
        recycler_older = findViewById(R.id.recycler_older);
        recycler_month = findViewById(R.id.recycler_month);
        recent_nodata = findViewById(R.id.recent_nodata);
        older_nodata = findViewById(R.id.older_nodata);
        month_nodata = findViewById(R.id.month_nodata);
        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);

        searchview_received = findViewById(R.id.searchview_received);
        closeButton = searchview_received.findViewById(R.id.search_close_btn);
        no_patient_found_block = findViewById(R.id.no_patient_found_block);
        main_block = findViewById(R.id.main_block);
        ((TextView) findViewById(R.id.search_pat_hint_txt)).setText(getString(R.string.empty_message_for_patinet_search_visit_screen));
        LinearLayout addPatientTV = findViewById(R.id.add_new_patientTV);
        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PrivacyPolicyActivity_New.class);
                intent.putExtra("intentType", "navigateFurther");
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                finish();
            }
        });

        backArrow.setOnClickListener(v -> {
            finish();
        });

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
                } else {
                    searchview_received.setBackground(getResources().getDrawable(R.drawable.ui2_common_input_bg));
                }
                return false;
            }
        });

        closeButton.setOnClickListener(v -> {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
            resetData();
            searchview_received.setQuery("", false);
        });
        // Search - end
    }

    private void endVisits_data() {
        recentCloseVisits();
        olderCloseVisits();
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
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

    private void searchOperation(String query) {
        Log.v("Search", "Search Word: " + query);
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");
        Log.d("TAG", "searchOperation: " + query);

        List<PrescriptionModel> recent = new ArrayList<>();
        List<PrescriptionModel> older = new ArrayList<>();

        String finalQuery = query;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  List<PrescriptionModel> allCloseList = allNotEndedVisits();
                List<PrescriptionModel> allRecentList = recentNotEndedVisits();
                List<PrescriptionModel> allOlderList = olderNotEndedVisits();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!finalQuery.isEmpty()) {
                            // recent- start
                            recent.clear();
                            older.clear();

                            if (allRecentList.size() > 0) {
                                for (PrescriptionModel model : allRecentList) {
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery) ||
                                                lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            recent.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            recent.add(model);
                                        } else {
                                            // dont add in list value.
                                        }
                                    }
                                }
                            }

                            if (allOlderList.size() > 0) {
                                for (PrescriptionModel model : allOlderList) {
                                    if (model.getMiddle_name() != null) {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String middleName = model.getMiddle_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullPartName = firstName + " " + lastName;
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        if (firstName.contains(finalQuery) || middleName.contains(finalQuery)
                                                || lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            older.add(model);
                                        } else {
                                            // do nothing
                                        }
                                    } else {
                                        String firstName = model.getFirst_name().toLowerCase();
                                        String lastName = model.getLast_name().toLowerCase();
                                        String fullName = firstName + " " + lastName;

                                        if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                            older.add(model);
                                        } else {
                                            // do nothing
                                        }
                                    }
                                }
                            }

                            recentVisitsAdapter = new EndVisitAdapter(context, recent);
                            recycler_recent.setNestedScrollingEnabled(false);
                            recycler_recent.setAdapter(recentVisitsAdapter);

                            olderVisitsAdapter = new EndVisitAdapter(context, older);
                            recycler_older.setNestedScrollingEnabled(false);
                            recycler_older.setAdapter(olderVisitsAdapter);

                            /**
                             * Checking here the query that is entered and it is not empty so check the size of all of these
                             * arraylists; if there size is 0 than show the no patient found view.
                             */
                            int allCount = recent.size() + older.size();
                            allCountVisibility(allCount);
                            recent_older_visibility(recent, older);
                        }
                    }
                });
            }
        }).start();

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

    private void recentCloseVisits() {
        recentCloseVisitsList = recentNotEndedVisits(recentLimit, recentStart);
        recentVisitsAdapter = new EndVisitAdapter(this, recentCloseVisitsList);
        recycler_recent.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        recycler_recent.setAdapter(recentVisitsAdapter);

        recentStart = recentEnd;
        recentEnd += recentLimit;

        todays_count = recentCloseVisitsList.size();
        if (todays_count == 0 || todays_count < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);
    }

    private void olderCloseVisits() {
        olderCloseVisitsList = olderNotEndedVisits(olderLimit, olderStart);
        olderVisitsAdapter = new EndVisitAdapter(this, olderCloseVisitsList);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(olderVisitsAdapter);

        olderStart = olderEnd;
        olderEnd += olderLimit;

        weeks_count = olderCloseVisitsList.size();
        if (weeks_count == 0 || weeks_count < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    private void resetData() {
        recent_older_visibility(recentCloseVisitsList, olderCloseVisitsList);
        Log.d("TAG", "resetData: " + recentCloseVisitsList.size() + ", " + olderCloseVisitsList.size());

        recentVisitsAdapter = new EndVisitAdapter(this, recentCloseVisitsList);
        recycler_recent.setNestedScrollingEnabled(false); // Note: use NestedScrollView in xml and in xml add nestedscrolling to false as well as in java for Recyclerview in case you are recyclerview and scrollview together.
        recycler_recent.setAdapter(recentVisitsAdapter);

        olderVisitsAdapter = new EndVisitAdapter(this, olderCloseVisitsList);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(olderVisitsAdapter);
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

  /*  @Override
    public int getTotalCounts() {
        total_counts = todays_count + weeks_count + months_count;
        return total_counts;
    }*/

}