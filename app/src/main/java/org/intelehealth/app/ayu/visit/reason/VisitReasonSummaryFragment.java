package org.intelehealth.app.ayu.visit.reason;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
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

    public VisitReasonSummaryFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static VisitReasonSummaryFragment newInstance(Intent intent, List<Node> answeredRootNodeList) {
        VisitReasonSummaryFragment fragment = new VisitReasonSummaryFragment();
        fragment.mAnsweredRootNodeList = answeredRootNodeList;
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

        return view;
    }
}