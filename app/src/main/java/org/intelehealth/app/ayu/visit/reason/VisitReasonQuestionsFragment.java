package org.intelehealth.app.ayu.visit.reason;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
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
    private List<Node> mChiefComplainRootNodeList = new ArrayList<>();
    private int mCurrentComplainNodeIndex = 0;
    private Node mCurrentNode;
    private boolean mIsEditMode = false;

    public VisitReasonQuestionsFragment() {
        // Required empty public constructor
    }


    public static VisitReasonQuestionsFragment newInstance(Intent intent, boolean isEditMode, List<Node> nodeList) {
        VisitReasonQuestionsFragment fragment = new VisitReasonQuestionsFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.mChiefComplainRootNodeList = nodeList;
        fragment.mCurrentNode = fragment.mChiefComplainRootNodeList.get(fragment.mCurrentComplainNodeIndex);
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


    //private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_visit_reason_questions, container, false);

        if (mIsEditMode) {
            view.findViewById(R.id.ll_footer).setVisibility(View.VISIBLE);
            view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, mIsEditMode, null);

                }
            });
            view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            });
        }
        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(!mIsEditMode);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //mCurrentRootOptionList = mCurrentNode.getOptionsList();

        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            Log.v("VISIT_REASON", new Gson().toJson(mChiefComplainRootNodeList.get(i)));
            ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
            complainBasicInfo.setComplainName(mChiefComplainRootNodeList.get(i).getText());
            complainBasicInfo.setComplainNameByLocale(mChiefComplainRootNodeList.get(i).findDisplay());
            complainBasicInfo.setOptionSize(mChiefComplainRootNodeList.get(i).getOptionsList().size());
            if (complainBasicInfo.getComplainName().equalsIgnoreCase("Associated symptoms")) {
                complainBasicInfo.setComplainNameByLocale(getString(R.string.associated_symptoms_header_visit_creation));
                complainBasicInfo.setAssociateSymptom(true);
            }
            mRootComplainBasicInfoHashMap.put(i, complainBasicInfo);
        }
        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), false, false, null, mCurrentComplainNodeIndex, mRootComplainBasicInfoHashMap, mIsEditMode, new OnItemSelection() {
            @Override
            public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
                Log.v("onSelect QuestionsListingAdapter", "index - " + index + " \t mCurrentComplainNodeOptionsIndex - " + mCurrentComplainNodeOptionsIndex);
                Log.v("onSelect QuestionsListingAdapter", "node - " + node.getText());
                // avoid the scroll for old data change
                if (mCurrentComplainNodeOptionsIndex - index >= 1) {
                    Log.v("onSelect", "Scrolling index - " + index);
                    VisitUtils.scrollNow(recyclerView, 100, 0, 1000, mIsEditMode);
                    return;
                }
                if (isSkipped) {
                    boolean isRequiredToUnselectParent = mQuestionsListingAdapter.geItems().get(index).getOptionsList() == null || mQuestionsListingAdapter.geItems().get(index).size() <= 1;
                    if (isRequiredToUnselectParent) {
                        mQuestionsListingAdapter.geItems().get(index).setSelected(false);
                        mQuestionsListingAdapter.geItems().get(index).setDataCaptured(false);
                    }

                    if (mQuestionsListingAdapter.geItems().get(index).getOptionsList() != null && mQuestionsListingAdapter.geItems().get(index).getOptionsList().size() > 0)
                        for (int i = 0; i < mQuestionsListingAdapter.geItems().get(index).getOptionsList().size(); i++) {
                            mQuestionsListingAdapter.geItems().get(index).getOptionsList().get(i).setSelected(false);
                            mQuestionsListingAdapter.geItems().get(index).getOptionsList().get(i).setDataCaptured(false);
                        }
                    if (mQuestionsListingAdapter.geItems().get(index).isRequired()) {
                        mQuestionsListingAdapter.notifyItemChanged(index);
                        return;
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mQuestionsListingAdapter.notifyItemChanged(index);
                            }
                        }, 1000);
                    }
                }

                if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
                    mCurrentComplainNodeOptionsIndex++;
                else {
                    mCurrentComplainNodeOptionsIndex = 0;
                    mCurrentComplainNodeIndex += 1;
                    mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
                    mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
                }
                if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
                    //linearLayoutManager.setStackFromEnd(false);
                    if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
                        mQuestionsListingAdapter.addItem(mCurrentNode, mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
                    mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
                } else {
                    //linearLayoutManager.setStackFromEnd(false);
                    mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
                }

                VisitUtils.scrollNow(recyclerView, 300, 0, 500, mIsEditMode);

                VisitUtils.scrollNow(recyclerView, 1400, 0, 1400, mIsEditMode);


                mActionListener.onProgress((int) 60 / mCurrentNode.getOptionsList().size());
                if (!mIsEditMode) linearLayoutManager.setStackFromEnd(false);
            }

            @Override
            public void needTitleChange(String title) {
                mActionListener.onTitleChange(title);
            }

            @Override
            public void onAllAnswered(boolean isAllAnswered) {
                if (!mIsEditMode)
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, mIsEditMode, null);
                else
                    Toast.makeText(getActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCameraRequest() {

            }

            @Override
            public void onImageRemoved(int nodeIndex, int imageIndex, String image) {

            }
        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
        mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());

        if (mIsEditMode) {
            boolean pendingForAddAll = true;
            while (pendingForAddAll) {

                if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
                    mCurrentComplainNodeOptionsIndex++;
                else {
                    mCurrentComplainNodeOptionsIndex = 0;
                    mCurrentComplainNodeIndex += 1;
                    mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
                    mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
                }
                if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
                    //linearLayoutManager.setStackFromEnd(false);
                    if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
                        mQuestionsListingAdapter.addItem(mCurrentNode, mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
                    mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
                    pendingForAddAll = false;
                } else {
                    //linearLayoutManager.setStackFromEnd(true);
                    mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
                }
            }
        }
        return view;
    }

}