package org.intelehealth.app.ayu.visit.physicalexam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PhysicalExamSummaryFragment extends Fragment {
    private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<List<VisitSummaryData>> mAllItemList = new ArrayList<>();
    private List<VisitSummaryData> mItemList = new ArrayList<VisitSummaryData>();
    private LinearLayout mSummaryLinearLayout;
    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;

    public PhysicalExamSummaryFragment() {
        // Required empty public constructor
    }

    private String mSummaryString;

    public static PhysicalExamSummaryFragment newInstance(Intent intent, String values) {
        PhysicalExamSummaryFragment fragment = new PhysicalExamSummaryFragment();
        fragment.mSummaryString = values;
        //fragment.prepareSummary();
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
        View view = inflater.inflate(R.layout.fragment_physical_exam_summary, container, false);
        mSummaryLinearLayout = view.findViewById(R.id.ll_summary);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PAST_MEDICAL_HISTORY, false, null);
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, true, null);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, true, null);
            }
        });
        view.findViewById(R.id.imb_btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getActivity())) {
                    new SyncUtils().syncBackground();
                    // Toast.makeText(getActivity(), getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
                }
            }
        });
        prepareSummary();

        return view;
    }

    private void prepareSummary() {
        mSummaryLinearLayout.removeAllViews();
        String str = mSummaryString;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        str = str.replaceAll("<.*?>", "");
        System.out.println("prepareSummary - " + str);
        String[] spt = str.split("►");
        List<String> list = new ArrayList<>();
        LinkedHashMap<String, List<String>> mapData = new LinkedHashMap<String, List<String>>();

        for (String s : spt) {
            System.out.println(s);
            if (s.isEmpty()) continue;
            String[] spt1 = s.split("•");
            String complainName = "";
            for (String s1 : spt1) {
                if (s1.trim().endsWith(":")) {
                    complainName = s1;
                    list = new ArrayList<>();
                    mapData.put(s1, list);
                } else {
                    mapData.get(complainName).add(s1);
                }
            }

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
                        mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, true, null);
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