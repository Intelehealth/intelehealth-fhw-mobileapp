package org.intelehealth.unicef.ayu.visit.physicalexam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import org.intelehealth.unicef.knowledgeEngine.PhysicalExam;
import org.intelehealth.unicef.utilities.DialogUtils;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhysicalExaminationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhysicalExaminationFragment extends Fragment {

    //private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private int mCurrentChildComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private PhysicalExam physicalExam;
    private VisitCreationActionListener mActionListener;
    private boolean mIsEditMode = false;
    public PhysicalExaminationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        //sessionManager = new SessionManager(context);
        ((VisitCreationActivity) context).setImageUtilsListener(new VisitCreationActivity.ImageUtilsListener() {
            @Override
            public void onImageReady(Bundle bundle) {
                mQuestionsListingAdapter.addImageInLastNode(bundle.getString("image"));
            }

            @Override
            public void onImageReadyForDelete(int index, String image) {
                mQuestionsListingAdapter.removeImageInLastNode(index, image);
            }
        });
    }

    public static PhysicalExaminationFragment newInstance(Intent intent, boolean isEditMode, PhysicalExam physicalExamMap) {
        PhysicalExaminationFragment fragment = new PhysicalExaminationFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.physicalExam = physicalExamMap;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_physical_examination, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //mCurrentRootOptionList = mCurrentNode.getOptionsList();
        ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
        complainBasicInfo.setComplainName("Physical Exam");
        complainBasicInfo.setOptionSize(physicalExam.getTotalNumberOfExams());
        complainBasicInfo.setPhysicalExam(true);
        mRootComplainBasicInfoHashMap.put(0, complainBasicInfo);

        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), true, physicalExam, 0, mRootComplainBasicInfoHashMap, new OnItemSelection() {
            @Override
            public void onSelect(Node node, int index) {
                // avoid the scroll for old data change
                if (mCurrentComplainNodeOptionsIndex - index >= 1) {
                    return;
                }
                Log.v("onSelect", "node - " + node.getText());
                if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {
                    //if (mCurrentChildComplainNodeOptionsIndex < physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOptionsList().size()) {
                    //if (mCurrentChildComplainNodeOptionsIndex == physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOptionsList().size() - 1) {
                    //    mCurrentChildComplainNodeOptionsIndex = 0;
                    mCurrentComplainNodeOptionsIndex++;
                    // } else {
                    //    mCurrentChildComplainNodeOptionsIndex++;

                    //}


                    mQuestionsListingAdapter.addItem(physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOption(0));
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);

                    mActionListener.onProgress((int) 100 / physicalExam.getTotalNumberOfExams());
                    // }
                } else {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_PHYSICAL_SUMMARY_EXAMINATION,false, null);
                }

            }

            @Override
            public void needTitleChange(String title) {
                // mActionListener.onTitleChange(title);
            }

            @Override
            public void onAllAnswered(boolean isAllAnswered) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, false,null);
            }

            @Override
            public void onCameraRequest() {
                mActionListener.onCameraOpenRequest();
            }

            @Override
            public void onImageRemoved(int index, String image) {
                mActionListener.onImageRemoved(index, image);
            }
        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        mQuestionsListingAdapter.addItem(physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).

                getOption(0));
        showSanityDialog();
        return view;
    }

    private void showSanityDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(getActivity(), R.drawable.ui2_ic_warning_sanity, getResources().getString(R.string.sanity_alert_title), "", true, getResources().getString(R.string.okay), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }
}