package org.intelehealth.app.ayu.visit.reason;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.reason.adapter.QuestionsListingAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonQuestionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonQuestionsFragment extends Fragment {

    private List<String> mSelectedComplains = new ArrayList<>();
    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;


    public VisitReasonQuestionsFragment() {
        // Required empty public constructor
    }


    public static VisitReasonQuestionsFragment newInstance(Intent intent, List<String> selectedComplains) {
        VisitReasonQuestionsFragment fragment = new VisitReasonQuestionsFragment();
        fragment.mSelectedComplains = selectedComplains;
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
    }

    private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<Node> mCurrentRootNodeList = new ArrayList<>();
    private int mCurrentComplainNodeIndex = 0;
    private int mCurrentComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_visit_reason_questions, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        String fileLocation = "engines/" + mSelectedComplains.get(0) + ".json";
        JSONObject currentFile = FileUtils.encodeJSON(getActivity(), fileLocation);

        mCurrentRootNodeList.add(new Node(currentFile));

        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), mCurrentRootNodeList.get(mCurrentComplainNodeIndex).getOptionsList().size(), new QuestionsListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(Node node) {
                //Log.v("onSelect", "node - " + node.getText());
                if (mCurrentComplainNodeOptionsIndex < mCurrentRootNodeList.get(mCurrentComplainNodeIndex).getOptionsList().size() - 1)
                    mCurrentComplainNodeOptionsIndex++;
                else {
                    mCurrentComplainNodeOptionsIndex = 0;
                    mCurrentComplainNodeIndex++;
                }
                mQuestionsListingAdapter.addItem(mCurrentRootNodeList.get(mCurrentComplainNodeIndex).getOptionsList().get(mCurrentComplainNodeOptionsIndex));
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    }
                }, 100);

                mActionListener.onProgress((int)60/mCurrentRootNodeList.get(mCurrentComplainNodeIndex).getOptionsList().size());
            }
        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        mQuestionsListingAdapter.addItem(mCurrentRootNodeList.get(mCurrentComplainNodeIndex).getOptionsList().get(mCurrentComplainNodeOptionsIndex));
        return view;
    }
}