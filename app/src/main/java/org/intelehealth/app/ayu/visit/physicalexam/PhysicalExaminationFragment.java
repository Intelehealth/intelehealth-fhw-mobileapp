package org.intelehealth.app.ayu.visit.physicalexam;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.ayu.visit.pocdevice.ConnectPocDeviceFragment;
import org.intelehealth.app.ayu.visit.pocdevice.DigitalStethoscopeDialogFragment;
import org.intelehealth.app.ayu.visit.pocdevice.SoundFragment;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhysicalExaminationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhysicalExaminationFragment extends Fragment  implements SoundFragment.OnSoundSavedListener {

    //private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private int mCurrentChildComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private PhysicalExam physicalExam;
    private VisitCreationActionListener mActionListener;
    private boolean mIsEditMode = false;
    private boolean isSoundFlowCompleted = false;

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
            public void onImageReadyForDelete(int nodeIndex, int imageIndex, String imageName) {
                mQuestionsListingAdapter.removeImageInLastNode(nodeIndex, imageIndex, imageName);
            }
        });
    }

    public static PhysicalExaminationFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, PhysicalExam physicalExamMap) {
        PhysicalExaminationFragment fragment = new PhysicalExaminationFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.physicalExam = physicalExamMap;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PhysicalExamFragment", "OnCreate");



    }

    private final HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_physical_examination, container, false);
        if (physicalExam != null) {
            if (mIsEditMode) {
                view.findViewById(R.id.ll_footer).setVisibility(View.VISIBLE);
                if (!((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    view.findViewById(R.id.btn_cancel).setVisibility(View.INVISIBLE);
                }
                view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);
                    }
                });
                view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mIsEditMode && ((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                            requireActivity().setResult(Activity.RESULT_OK);
                            requireActivity().finish();
                        }
                    }
                });
            }
            getParentFragmentManager().setFragmentResultListener(
                    "sound_done",
                    this,
                    (key, bundle) -> {

                        // ✅ prevent re-trigger
                        if (isSoundFlowCompleted) return;

                        isSoundFlowCompleted = true;

                        advanceToNextExam(null);
                    }
            );
            RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            linearLayoutManager.setStackFromEnd(!mIsEditMode);
            linearLayoutManager.setReverseLayout(false);
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            //mCurrentRootOptionList = mCurrentNode.getOptionsList();
            ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
            complainBasicInfo.setComplainName("Physical Exam");

            complainBasicInfo.setOptionSize(physicalExam.getTotalNumberOfExams());
            complainBasicInfo.setPhysicalExam(true);
            mRootComplainBasicInfoHashMap.put(0, complainBasicInfo);

            mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, requireActivity(), false, true, physicalExam, 0, mRootComplainBasicInfoHashMap, mIsEditMode, new OnItemSelection() {
                @Override
                public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
                    if (isSkipped) {
                        mQuestionsListingAdapter.geItems().get(index).setSelected(false);
                        mQuestionsListingAdapter.geItems().get(index).setDataCaptured(false);
                        mQuestionsListingAdapter.notifyItemChanged(index);
                    }
                    CustomLog.v("onSelect", "node - " + node.getText());
                    if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {
                        //if (mCurrentChildComplainNodeOptionsIndex < physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOptionsList().size()) {
                        //if (mCurrentChildComplainNodeOptionsIndex == physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOptionsList().size() - 1) {
                        //    mCurrentChildComplainNodeOptionsIndex = 0;
                        mCurrentComplainNodeOptionsIndex++;
                        // } else {
                        //    mCurrentChildComplainNodeOptionsIndex++;

                        //}

                        isSoundFlowCompleted = false;
                        Node nextExamNode = physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex);
                        Node nextOption = (nextExamNode != null) ? nextExamNode.getOption(0) : null;
                        if (nextOption == null) nextOption = nextExamNode;
                        if (nextOption != null) {
                            mQuestionsListingAdapter.addItem(nextOption, physicalExam.getEngineVersion());
                        }
                   /* recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);*/
                        VisitUtils.scrollNow(recyclerView, 300, 0, 500, mIsEditMode, false);

                        VisitUtils.scrollNow(recyclerView, 1400, 0, 1400, mIsEditMode, false);

                        mActionListener.onProgress((int) (100.0 / physicalExam.getTotalNumberOfExams()));
                        // }
                    } else {
                        if (!mIsEditMode)
                            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);
                        else
                            Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
                    }
                    if (!mIsEditMode)
                        linearLayoutManager.setStackFromEnd(false);
                }

                @Override
                public void needTitleChange(String title) {
                    // mActionListener.onTitleChange(title);
                }

                @Override
                public void onAllAnswered(boolean isAllAnswered) {
                    if (!mIsEditMode)
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);
                    else
                        Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCameraRequest() {
                    mActionListener.onCameraOpenRequest();
                }

                @Override
                public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
                    mActionListener.onImageRemoved(nodeIndex, imageIndex, image);
                }

                @Override
                public void onAyuDeviceRequest(Node node) {
                    if (isSoundFlowCompleted) {
                        // 🚫 prevent reopening
                        return;
                    }
                    String examType = "heart";
                    String id = node.getId();
                    String text = node.getText();
                    if (id != null && id.toLowerCase().contains("lung")) {
                        examType = "lung";
                    } else if (text != null && text.toLowerCase().contains("lung")) {
                        examType = "lung";
                    }
                    ArrayList<String> sounds = extractSounds(examType);
                    VisitCreationActivity activity = (VisitCreationActivity) requireActivity();
                    ConnectPocDeviceFragment fragment =
                            ConnectPocDeviceFragment.newInstance(
                                    examType,
                                    sounds,
                                    activity.patientUuid,
                                    activity.visitUuid,
                                    activity.encounterVitals
                            );
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fl_steps_body, fragment)
                            .addToBackStack("poc_device")
                            .commit();
                }

                @Override
                public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {
                }
            });
            recyclerView.setAdapter(mQuestionsListingAdapter);
            Node firstExamNode = physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex);
            Node firstOption = (firstExamNode != null) ? firstExamNode.getOption(0) : null;
            if (firstOption == null) firstOption = firstExamNode;
            if (firstOption != null) {
                mQuestionsListingAdapter.addItem(firstOption, physicalExam.getEngineVersion());
            }
            showSanityDialog();
            if (mIsEditMode) {
                while (true) {
                    if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {
                        mCurrentComplainNodeOptionsIndex++;
                        Node editExamNode = physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex);
                        Node editOption = (editExamNode != null) ? editExamNode.getOption(0) : null;
                        if (editOption == null) editOption = editExamNode;
                        if (editOption != null) {
                            mQuestionsListingAdapter.addItem(editOption, physicalExam.getEngineVersion());
                        }


                    } else {
                        break;
                    }
                }
            /*recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                }
            }, 100);*/
            }
        } else {
            Toast.makeText(requireActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
        return view;
    }
    private ArrayList<String> extractSounds(String examType) {

        ArrayList<String> result = new ArrayList<>();

        if (physicalExam == null) return result;

        for (int i = 0; i < physicalExam.getTotalNumberOfExams(); i++) {

            Node examNode = physicalExam.getExamNode(i);
            if (examNode == null) continue;

            List<Node> options = examNode.getOptionsList();
            if (options == null) continue;

            for (Node node : options) {

                String text = node.getText();
                if (text == null) continue;

                String lower = text.toLowerCase();

                if (examType.equals("heart") && lower.contains("sound heart")) {
                    result.add(text.replace("Sound Heart:", "").trim());
                }

                if (examType.equals("lung") && lower.contains("sound lung")) {
                    result.add(text.replace("Sound Lung:", "").trim());
                }
            }
        }

        return result;
    }

    private void showSanityDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(requireActivity(), R.drawable.ui2_ic_warning_sanity, getResources().getString(R.string.sanity_alert_title), "", true, getResources().getString(R.string.okay), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }

    @Override
    public void onSoundSaved() {
        advanceToNextExam(null);
    }
    private void advanceToNextExam(@Nullable LinearLayoutManager layoutManager) {

        if (physicalExam == null) return;

        if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {

            mCurrentComplainNodeOptionsIndex++;

            Node nextExamNode = physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex);
            Node nextOption = (nextExamNode != null) ? nextExamNode.getOption(0) : null;

            if (nextOption == null) nextOption = nextExamNode;

            if (nextOption != null && mQuestionsListingAdapter != null) {
                mQuestionsListingAdapter.addItem(nextOption, physicalExam.getEngineVersion());
            }

            if (layoutManager != null) {
                layoutManager.setStackFromEnd(false);
            }

        } else {

            if (!mIsEditMode) {
                mActionListener.onFormSubmitted(
                        VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION,
                        mIsEditMode,
                        null
                );
            } else {
                Toast.makeText(requireActivity(),
                        getString(R.string.please_submit_to_proceed_next_step),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*private void advanceToNextExam(LinearLayoutManager layoutManager) {
        if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {
            mCurrentComplainNodeOptionsIndex++;
            Node nextExamNode = physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex);
            Node nextOption = (nextExamNode != null) ? nextExamNode.getOption(0) : null;
            if (nextOption == null) nextOption = nextExamNode;
            if (nextOption != null) {
                mQuestionsListingAdapter.addItem(nextOption, physicalExam.getEngineVersion());
            }
            if (layoutManager != null) layoutManager.setStackFromEnd(false);
        } else {
            if (!mIsEditMode)
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);
            else
                Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
        }
    }*/
}