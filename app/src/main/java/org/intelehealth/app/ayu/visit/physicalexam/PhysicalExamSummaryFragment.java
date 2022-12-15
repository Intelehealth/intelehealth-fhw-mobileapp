package org.intelehealth.app.ayu.visit.physicalexam;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.common.adapter.SummaryViewAdapter;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.List;

public class PhysicalExamSummaryFragment extends Fragment {
    private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<List<VisitSummaryData>> mAllItemList = new ArrayList<>();
    private List<VisitSummaryData> mItemList = new ArrayList<VisitSummaryData>();


    public PhysicalExamSummaryFragment() {
        // Required empty public constructor
    }

    private String mSummaryString;
    public static PhysicalExamSummaryFragment newInstance(Intent intent, String values) {
        PhysicalExamSummaryFragment fragment = new PhysicalExamSummaryFragment();
        fragment.mSummaryString = values;
        fragment.prepareSummary();
        return fragment;
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
        RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, getActivity(), mItemList, new SummaryViewAdapter.OnItemSelection() {
            @Override
            public void onSelect(VisitSummaryData data) {

            }
        });
        recyclerView.setAdapter(summaryViewAdapter);
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
            //System.out.println(s);
            if (s.trim().startsWith("Associated symptoms:")) {
                associatedSymptomsString = s;
            } else {
                list.add(s);
            }

        }
        //mComplainSummaryLinearLayout.removeAllViews();
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

                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, getActivity(), visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                //mComplainSummaryLinearLayout.addView(view);
            }
        }
        String[] spt1 = associatedSymptomsString.split("•");
        //mAssociateSymptomsLinearLayout.removeAllViews();
        for (String value : spt1) {

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
                //mAssociateSymptomsLinearLayout.addView(view);
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