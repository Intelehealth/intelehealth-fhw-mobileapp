package org.intelehealth.app.ayu.visit.reason;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import org.intelehealth.app.ayu.visit.common.adapter.SummaryViewAdapter;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonSummaryFragment extends Fragment {

    private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<List<VisitSummaryData>> mAllItemList = new ArrayList<>();
    private List<VisitSummaryData> mItemList = new ArrayList<VisitSummaryData>();
    private String mSummaryString;
    private LinearLayout mAssociateSymptomsLinearLayout, mComplainSummaryLinearLayout;
    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;

    public VisitReasonSummaryFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static VisitReasonSummaryFragment newInstance(Intent intent, String values) {
        VisitReasonSummaryFragment fragment = new VisitReasonSummaryFragment();
        fragment.mSummaryString = values;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visit_reason_summary, container, false);

        mComplainSummaryLinearLayout = view.findViewById(R.id.ll_complain_summary);
        mAssociateSymptomsLinearLayout = view.findViewById(R.id.ll_associated_sympt);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_PHYSICAL_EXAMINATION, null);
            }
        });
        view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT, null);
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
        String str = mSummaryString;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        str = str.replaceAll("<.*?>", "");
        System.out.println(str);
        String[] spt = str.split("►");
        List<String> list = new ArrayList<>();
        String associatedSymptomsString = "";
        for (String s : spt) {
            Log.e("node", s);
            if (s.trim().startsWith("Associated symptoms:")) {
                associatedSymptomsString = s;
            } else {
                list.add(s);
            }

        }
        mComplainSummaryLinearLayout.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            String complainName = "";
            List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
            String[] spt1 = list.get(i).split("•");
            for (String value : spt1) {

                if (value.contains(" - ")) {
                    String k = value.substring(0, value.indexOf(" - ")).trim();
                    String v = value.substring(value.indexOf(" - ") + 2).trim();
                    VisitSummaryData summaryData = new VisitSummaryData();
                    summaryData.setQuestion(k);
                    summaryData.setDisplayValue(v);
                    visitSummaryDataList.add(summaryData);
                } else if (value.contains(":")) {
                    complainName = value;
                    System.out.println(complainName);
                }

            }
            if (!complainName.isEmpty() && !visitSummaryDataList.isEmpty()) {
                View view = View.inflate(getActivity(), R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                complainLabelTextView.setText(complainName);
                view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_SUMMARY_RESUME_BACK_FOR_EDIT, null);
                    }
                });

                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, getActivity(), visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                mComplainSummaryLinearLayout.addView(view);
            }
        }
        String[] spt1 = associatedSymptomsString.trim().split("•");
        Log.e("node", associatedSymptomsString);
        Log.e("node", String.valueOf(spt1.length));
        mAssociateSymptomsLinearLayout.removeAllViews();
        for (String value : spt1) {
            Log.e("node", value);
            if (value.contains(" - ")) {
                String k = value.substring(0, value.indexOf(" - ")).trim();
                String v = value.substring(value.indexOf(" - ") + 2).trim();
                View view = View.inflate(getActivity(), R.layout.ui2_summary_qa_ass_sympt_row_item_view, null);
                TextView keyTextView = view.findViewById(R.id.tv_question_label);
                keyTextView.setText(k);
                TextView valueTextView = view.findViewById(R.id.tv_answer_value);
                valueTextView.setText(v);
                if (v.isEmpty()) {
                    view.findViewById(R.id.iv_blt).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.iv_blt).setVisibility(View.VISIBLE);
                }
                mAssociateSymptomsLinearLayout.addView(view);
            } else if (value.contains(":")) {
                System.out.println(value);
            }

        }
        for (int i = 0; i < mAnsweredRootNodeList.size(); i++) {
            List<VisitSummaryData> itemList = new ArrayList<VisitSummaryData>();
            for (int j = 0; j < mAnsweredRootNodeList.get(i).getOptionsList().size(); j++) {
                VisitSummaryData summaryData = new VisitSummaryData();
                summaryData.setDisplayValue(mAnsweredRootNodeList.get(i).getOptionsList().get(j).getText());
                itemList.add(summaryData);
            }
        }
    }
}