package org.intelehealth.unicef.ayu.visit.pastmedicalhist;

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

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.ayu.visit.VisitCreationActionListener;
import org.intelehealth.unicef.ayu.visit.VisitCreationActivity;
import org.intelehealth.unicef.ayu.visit.common.OnItemSelection;
import org.intelehealth.unicef.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.unicef.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.unicef.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PastMedicalHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PastMedicalHistoryFragment extends Fragment {

    private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private Node mCurrentNode;
    private boolean mIsEditMode = false;
    private VisitCreationActionListener mActionListener;

    public PastMedicalHistoryFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static PastMedicalHistoryFragment newInstance(Intent intent, boolean isEditMode, Node node) {
        PastMedicalHistoryFragment fragment = new PastMedicalHistoryFragment();
        fragment.mCurrentNode = node;
        fragment.mIsEditMode = isEditMode;
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
        //sessionManager = new SessionManager(context);
    }
    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_past_medical_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        mCurrentRootOptionList = mCurrentNode.getOptionsList();

        ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
        complainBasicInfo.setComplainName("Patient History");
        complainBasicInfo.setOptionSize(mCurrentRootOptionList.size());
        complainBasicInfo.setPatientHistory(true);
        mRootComplainBasicInfoHashMap.put(0, complainBasicInfo);


        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), false, null, 0, mRootComplainBasicInfoHashMap, new OnItemSelection() {
            @Override
            public void onSelect(Node node, int index) {
                // avoid the scroll for old data change
                if (mCurrentComplainNodeOptionsIndex - index >= 1) {
                    return;
                }
                //Log.v("onSelect", "node - " + node.getText());
                if (mCurrentComplainNodeOptionsIndex < mCurrentRootOptionList.size() - 1) {
                    mCurrentComplainNodeOptionsIndex++;


                    mQuestionsListingAdapter.addItem(mCurrentRootOptionList.get(mCurrentComplainNodeOptionsIndex));
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);

                    mActionListener.onProgress((int) 100 / mCurrentRootOptionList.size());
                } else {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_5_FAMILY_HISTORY, false, null);
                }
            }

            @Override
            public void needTitleChange(String title) {
                mActionListener.onTitleChange(title);
            }

            @Override
            public void onAllAnswered(boolean isAllAnswered) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, false,null);
            }

            @Override
            public void onCameraRequest() {

            }

            @Override
            public void onImageRemoved(int index, String image) {

            }
        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        if (mIsEditMode) {
            mQuestionsListingAdapter.addItemAll(mCurrentRootOptionList);
            mCurrentComplainNodeOptionsIndex = mCurrentRootOptionList.size() - 1;
        } else {
            mQuestionsListingAdapter.addItem(mCurrentRootOptionList.get(mCurrentComplainNodeOptionsIndex));

        }
        return view;
    }
}