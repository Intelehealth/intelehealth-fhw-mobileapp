package org.intelehealth.app.activities.visit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientAdapter_New;

public class VisitPendingFragment extends Fragment {
    private RecyclerView recycler_today, recycler_week, recycler_month;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_pending, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        recycler_today = view.findViewById(R.id.recycler_today);
        recycler_week = view.findViewById(R.id.rv_thisweek);
        recycler_month = view.findViewById(R.id.rv_thismonth);

        visitData();
    }

    private void visitData() {
        todays_Visits();
        thisWeeks_Visits();
        thisMonths_Visits();
    }


    private void todays_Visits() {
        FollowUpPatientAdapter_New adapter_new = new FollowUpPatientAdapter_New(getActivity());
        recycler_today.setAdapter(adapter_new);

        /*try {
            Date cDate = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
            List<FollowUpModel> followUpModels = getAllPatientsFromDB_Today(offset, currentDate);
            followUpModels = getChiefComplaint(followUpModels);
            totalCounts_today = followUpModels.size();
            if(totalCounts_today == 0 || totalCounts_today < 0)
                today_nodata.setVisibility(View.VISIBLE);
            else
                today_nodata.setVisibility(View.GONE);
            adapter_new = new FollowUpPatientAdapter_New(followUpModels, this);
            rv_today.setAdapter(adapter_new);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("todays_followupvisits", "exception: ", e);
        }*/
    }


    private void thisWeeks_Visits() {
        FollowUpPatientAdapter_New adapter_new = new FollowUpPatientAdapter_New(getActivity());
        recycler_week.setAdapter(adapter_new);
    }

    private void thisMonths_Visits() {
        FollowUpPatientAdapter_New adapter_new = new FollowUpPatientAdapter_New(getActivity());
        recycler_month.setAdapter(adapter_new);
    }


}
