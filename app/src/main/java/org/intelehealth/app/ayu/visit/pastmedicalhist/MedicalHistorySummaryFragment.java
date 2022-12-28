package org.intelehealth.app.ayu.visit.pastmedicalhist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.adapter.SummarySingleViewAdapter;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicalHistorySummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicalHistorySummaryFragment extends Fragment {

    private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<List<VisitSummaryData>> mAllItemList = new ArrayList<>();
    private List<VisitSummaryData> mItemList = new ArrayList<VisitSummaryData>();
    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private String mSummaryStringPastHistory, mSummaryStringFamilyHistory;
    private LinearLayout mSummaryLinearLayout;

    public MedicalHistorySummaryFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MedicalHistorySummaryFragment newInstance(Intent intent, String patientHistory, String familyHistory) {
        MedicalHistorySummaryFragment fragment = new MedicalHistorySummaryFragment();
        fragment.mSummaryStringPastHistory = patientHistory;
        fragment.mSummaryStringFamilyHistory = familyHistory;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medical_history_summary, container, false);
        mSummaryLinearLayout = view.findViewById(R.id.ll_summary);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_6_VISIT_SUMMARY, null);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT, null);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT, null);
            }
        });
        view.findViewById(R.id.imb_btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getActivity())) {
                    new SyncUtils().syncBackground();
                    Toast.makeText(getActivity(), getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
                }
            }
        });
        prepareSummary();

        return view;
    }

    private void prepareSummary() {
        mSummaryLinearLayout.removeAllViews();
        String str = mSummaryStringPastHistory;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        String str1 = mSummaryStringFamilyHistory;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        str = str.replaceAll("<.*?>", "");
        str1 = str1.replaceAll("<.*?>", "");
        System.out.println("mSummaryStringPastHistory - " + str);
        System.out.println("mSummaryStringFamilyHistory - " + str1);
        String[] spt = str.split("•");
        String[] spt1 = str1.split("•");
        List<String> list = new ArrayList<>();
        TreeMap<String, List<String>> mapData = new TreeMap<>();
        mapData.put("Patient history", new ArrayList<>());
        mapData.put("Family history", new ArrayList<>());
        for (String s : spt) {
            System.out.println(s);
            if (!s.trim().isEmpty())
                mapData.get("Patient history").add(s.trim());


        }
        for (String s : spt1) {
            System.out.println(s);
            if (!s.trim().isEmpty())
                mapData.get("Family history").add(s.trim());


        }

        System.out.println(mapData);
        for (Map.Entry<String, List<String>> entry : mapData.entrySet()) {
            String _complain = entry.getKey();
            List<String> _list = entry.getValue();

            if (!_complain.isEmpty() && !_list.isEmpty()) {
                View view = View.inflate(getActivity(), R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                complainLabelTextView.setText(_complain);
                view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT, null);
                    }
                });
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
                SummarySingleViewAdapter summaryViewAdapter = new SummarySingleViewAdapter(recyclerView, getActivity(), _list, new SummarySingleViewAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(String data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                mSummaryLinearLayout.addView(view);
            }
        }

    }
}