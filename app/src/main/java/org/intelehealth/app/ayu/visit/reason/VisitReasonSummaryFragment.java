package org.intelehealth.app.ayu.visit.reason;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.common.adapter.SummaryViewAdapter;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.knowledgeEngine.Node;

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

    public VisitReasonSummaryFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static VisitReasonSummaryFragment newInstance(Intent intent, List<Node> answeredRootNodeList) {
        VisitReasonSummaryFragment fragment = new VisitReasonSummaryFragment();
        fragment.mAnsweredRootNodeList = answeredRootNodeList;
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
        View view = inflater.inflate(R.layout.fragment_visit_reason_summary, container, false);

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