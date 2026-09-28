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
import org.intelehealth.app.ayu.visit.model.HeartLungRecordModel;
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
public class PhysicalExaminationFragment extends Fragment  {

    //private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private int mCurrentChildComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private PhysicalExam physicalExam;
    private VisitCreationActionListener mActionListener;
    private boolean mIsEditMode = false;
    private boolean isSoundFlowCompleted = false;
    private RecyclerView mRecyclerView;

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

        // Rehydrate the completed-sound-type set from the local DB. This ensures
        // that even if the Activity was recreated (config change / process death)
        // mid-flow, we still know which sound exam types were already recorded
        // and won't re-open SoundFragment for them.
        rehydrateCompletedSoundTypesFromDb((VisitCreationActivity) context);
    }

    private void rehydrateCompletedSoundTypesFromDb(VisitCreationActivity vca) {
        if (vca == null || vca.visitUuid == null) return;
        try {
            InteleHealthDatabaseHelper db = new InteleHealthDatabaseHelper(vca);
            List<HeartLungRecordModel> records = db.getAllHeartLungRecords(vca.visitUuid);
            if (records == null) return;
            for (HeartLungRecordModel r : records) {
                if (r == null || r.type == null) continue;
                // getAllHeartLungRecords ignores the visitUuid filter internally, so
                // re-check here to scope to this visit.
                if (vca.visitUuid.equals(r.visitUuid)) {
                    vca.completedSoundTypes.add(r.type.toLowerCase());
                }
            }
            Log.d("SOUND_FLOW",
                    "Rehydrated completedSoundTypes=" + vca.completedSoundTypes);
        } catch (Exception e) {
            Log.e("SOUND_FLOW", "Failed to rehydrate sound types", e);
        }
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
                    "sound_done", this,
                    (key, bundle) -> {
                        if (isSoundFlowCompleted && !bundle.containsKey("type")) return;

                        String examType = bundle.getString("type", "heart");

                        // ✅ FIX 2: Immediately mark complete so no rebind can reopen it
                        VisitCreationActivity vca = (VisitCreationActivity) requireActivity();
                        vca.completedSoundTypes.add(examType);
                        isSoundFlowCompleted = false; // reset for next sound type

                        Log.d("SOUND_FLOW", "sound_done: type=" + examType
                                + " completedSoundTypes=" + vca.completedSoundTypes);

                        skipPastSoundNodesAndAdvance(examType);
                    }
            );
            getParentFragmentManager().setFragmentResultListener(
                    SoundFragment.RESULT_CANCELLED, this,
                    (key, bundle) -> {
                        String examType = bundle.getString("type", "heart");
                        VisitCreationActivity vca = (VisitCreationActivity) requireActivity();
                        isSoundFlowCompleted = false;
                        if (!hasRecordedSoundTypeInDb(vca, examType)) {
                            vca.completedSoundTypes.remove(examType);
                        }
                        Log.d("SOUND_FLOW", "sound_cancelled: type=" + examType
                                + " completedSoundTypes=" + vca.completedSoundTypes);
                    }
            );
            RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
            mRecyclerView = recyclerView;
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
                        finishPhysicalExamAndAdvance();
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
                    finishPhysicalExamAndAdvance();
                }

                @Override
                public void onCameraRequest() {
                    mActionListener.onCameraOpenRequest();
                }

                @Override
                public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
                    mActionListener.onImageRemoved(nodeIndex, imageIndex, image);
                }

               /* public void onAyuDeviceRequest(Node node) {
                    if (isSoundFlowCompleted) {
                        // prevent reopening within the same listener run
                        return;
                    }
                    // The level-2 node's text is just " Aortic" / " Anterior-1-Left-Top",
                    // which doesn't reveal heart vs lung. The level-1 PARENT node
                    // ("Sound Heart" / "Sound Lung") does. Use the parent name from
                    // PhysicalExam to determine which body region we're recording.
                    String examType = inferSoundExamType(mCurrentComplainNodeOptionsIndex);

                    // Suppress the adapter's auto-trigger when we're re-binding
                    // a sound node whose type was already recorded in this visit.
                    // Use both the activity-scoped set (fast) AND a DB check (durable
                    // across activity recreation) — either being positive blocks reopen.
                    VisitCreationActivity vca = (VisitCreationActivity) requireActivity();
                    if (vca.completedSoundTypes.contains(examType)
                            || hasRecordedSoundTypeInDb(vca, examType)) {
                        // Make sure the in-memory set is consistent with DB so the
                        // next bind is also fast.
                        vca.completedSoundTypes.add(examType);
                        Log.d("SOUND_FLOW",
                                "onAyuDeviceRequest: skipped — " + examType
                                        + " already recorded for visit "
                                        + "(completed=" + vca.completedSoundTypes + ")");

                        // Safety net: if we're being re-bound on a sound node that's
                        // already done, AND there are no more outstanding sound types
                        // in this protocol, the user is done with the physical exam —
                        // advance to the next major step regardless of whether the
                        // sound_done listener fires.
                        if (allRequiredSoundTypesCompleted(vca)) {
                            Log.d("SOUND_FLOW",
                                    "onAyuDeviceRequest: all sound types completed — "
                                            + "triggering finishPhysicalExamAndAdvance()");
                            finishPhysicalExamAndAdvance();
                        }
                        return;
                    }

                    ArrayList<String> sounds = extractSounds(examType);
                    if (sounds.isEmpty()) {
                        // Fallback defaults from Cough.json's perform-physical-exam string.
                        if ("heart".equals(examType)) {
                            sounds.add("Aortic");
                            sounds.add("Pulmonic");
                            sounds.add("Tricuspid");
                            sounds.add("Mitral");
                        } else {
                            sounds.add("Anterior-1-Left-Top");
                            sounds.add("Anterior-2-Right-Top");
                            sounds.add("Anterior-3-Left-Middle");
                            sounds.add("Anterior-4-Right-Middle");
                            sounds.add("Anterior-5-Left-Lower");
                            sounds.add("Anterior-6-Right-Lower");
                        }
                    }

                    VisitCreationActivity activity = (VisitCreationActivity) requireActivity();

                    Bundle args = new Bundle();
                    args.putString("type", examType);
                    args.putStringArrayList("sounds", sounds);
                    args.putString("patientUuid", activity.patientUuid);
                    args.putString("visitUuid", activity.visitUuid);
                    args.putString("encounterUuid", activity.encounterVitals);

                    SoundFragment fragment = SoundFragment.newInstance(args);

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fl_steps_body, fragment)
                            .addToBackStack("sound")
                            .commit();
                }*/
                @Override
                public void onAyuDeviceRequest(Node node) {
                    if (isSoundFlowCompleted) return;

                    String examType = inferSoundExamType(mCurrentComplainNodeOptionsIndex);
                    VisitCreationActivity vca = (VisitCreationActivity) requireActivity();

                    if (vca.completedSoundTypes.contains(examType)
                            || hasRecordedSoundTypeInDb(vca, examType)) {
                        vca.completedSoundTypes.add(examType);
                        Log.d("SOUND_FLOW", "onAyuDeviceRequest: skipped — " + examType);
                        if (allRequiredSoundTypesCompleted(vca)) {
                            finishPhysicalExamAndAdvance();
                        }
                        return;
                    }

                  /*  ArrayList<String> sounds = extractSounds(examType);
                    if (sounds.isEmpty()) {
                        if ("heart".equals(examType)) {
                            sounds.add("Aortic"); sounds.add("Pulmonic");
                            sounds.add("Tricuspid"); sounds.add("Mitral");
                        } else {
                            sounds.add("Anterior-1-Left-Top"); sounds.add("Anterior-2-Right-Top");
                            sounds.add("Anterior-3-Left-Middle"); sounds.add("Anterior-4-Right-Middle");
                            sounds.add("Anterior-5-Left-Lower"); sounds.add("Anterior-6-Right-Lower");
                        }
                    }

                    // ✅ FIX 1: Mark as completed BEFORE opening the fragment
                    // so any RecyclerView rebind during the transaction is already blocked
                    isSoundFlowCompleted = true;
                    vca.completedSoundTypes.add(examType);
                    Log.d("SOUND_FLOW", "Opening SoundFragment for type=" + examType);

                    Bundle args = new Bundle();
                    args.putString("type", examType);
                    args.putStringArrayList("sounds", sounds);
                    args.putString("patientUuid", vca.patientUuid);
                    args.putString("visitUuid", vca.visitUuid);
                    args.putString("encounterUuid", vca.encounterVitals);

                    SoundFragment fragment = SoundFragment.newInstance(args);
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fl_steps_body, fragment)
                            .addToBackStack("sound")
                            .commit();
                }*/
// AFTER
                    ArrayList<String> sounds = extractSounds(examType);
                    if (sounds.isEmpty()) {
                        if ("heart".equals(examType)) {
                            sounds.add("Aortic");
                            sounds.add("Pulmonic");
                            sounds.add("Tricuspid");
                            sounds.add("Mitral");
                        } else {
                            sounds.add("Anterior-1-Left-Top");
                            sounds.add("Anterior-2-Right-Top");
                            sounds.add("Anterior-3-Left-Middle");
                            sounds.add("Anterior-4-Right-Middle");
                            sounds.add("Anterior-5-Left-Lower");
                            sounds.add("Anterior-6-Right-Lower");
                        }
                    }

// Extract BOTH heart and lung sizes here so AyuConnectDialogFragment
// can show correct "0 / 4" and "0 / 6" on both cards from the start
                    ArrayList<String> heartSounds = extractSounds("heart");
                    ArrayList<String> lungSounds = extractSounds("lung");

// Fallback: if extractSounds returned empty, use the hardcoded defaults size
                    int heartSize = heartSounds.isEmpty() ? 4 : heartSounds.size();
                    int lungSize = lungSounds.isEmpty() ? 6 : lungSounds.size();

                    isSoundFlowCompleted = true;
                    vca.completedSoundTypes.add(examType);
                    Log.d("SOUND_FLOW", "Opening SoundFragment for type=" + examType
                            + " heartSize=" + heartSize + " lungSize=" + lungSize);

                    Bundle args = new Bundle();
                    args.putString("type", examType);
                    args.putStringArrayList("sounds", sounds);
                    args.putString("patientUuid", vca.patientUuid);
                    args.putString("visitUuid", vca.visitUuid);
                    args.putString("encounterUuid", vca.encounterVitals);
                    args.putInt("heartSoundsSize", heartSize);  // ADD — for AyuConnectDialogFragment
                    args.putInt("lungSoundsSize", lungSize);   // ADD — for AyuConnectDialogFragment

                    SoundFragment fragment = SoundFragment.newInstance(args);
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fl_steps_body, fragment)
                            .addToBackStack("sound")
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
    /**
     * Build the ordered list of sound positions for the requested exam type
     * by walking the protocol's exam nodes and matching by their LEVEL-1
     * parent name (e.g. "Sound Heart" / "Sound Lung").
     *
     * For Cough.json with 4 heart + 6 lung positions this returns exactly
     * those 4 (or 6) names, preserving the order from perform-physical-exam.
     */
    private ArrayList<String> extractSounds(String examType) {
        ArrayList<String> result = new ArrayList<>();
        if (physicalExam == null) return result;

        String parentMatch = "lung".equals(examType) ? "sound lung" : "sound heart";

        for (int i = 0; i < physicalExam.getTotalNumberOfExams(); i++) {
            String parent = physicalExam.getExamParentNodeName(i);
            if (parent == null) continue;
            if (!parent.toLowerCase().contains(parentMatch)) continue;

            Node examNode = physicalExam.getExamNode(i);
            if (examNode == null) continue;

            String text = examNode.getText();
            if (text == null) continue;

            // The mind-map adds a leading space (" Aortic"); trim it.
            result.add(text.trim());
        }

        return result;
    }

    /**
     * Check whether every sound exam type required by this protocol has been
     * recorded for the current visit. For Cough.json that means BOTH "heart"
     * AND "lung". For protocols with only one (or none), the check returns
     * accordingly.
     */
    private boolean allRequiredSoundTypesCompleted(VisitCreationActivity vca) {
        if (vca == null || physicalExam == null) return false;
        boolean needHeart = false, needLung = false;
        try {
            int total = physicalExam.getTotalNumberOfExams();
            for (int i = 0; i < total; i++) {
                // ✅ FIX 3: Use getExamParentNodeName() — not getTitle().split()
                // getTitle().split(" : ")[0] was returning wrong strings silently
                String parent = physicalExam.getExamParentNodeName(i);
                if (parent == null) continue;
                String p = parent.toLowerCase();
                if (p.contains("sound heart")) needHeart = true;
                else if (p.contains("sound lung")) needLung = true;
            }
        } catch (Exception e) {
            Log.e("SOUND_FLOW", "Failed to compute required sound types", e);
            return false;
        }
        if (!needHeart && !needLung) return false;
        if (needHeart && !vca.completedSoundTypes.contains("heart")
                && !hasRecordedSoundTypeInDb(vca, "heart")) return false;
        if (needLung && !vca.completedSoundTypes.contains("lung")
                && !hasRecordedSoundTypeInDb(vca, "lung")) return false;
        return true;
    }
    /**
     * Check the local DB for any saved heart/lung recording for the current visit
     * matching the given exam type. Used as a robust fallback when activity-scoped
     * state was reset (config change, process death).
     */
    private boolean hasRecordedSoundTypeInDb(VisitCreationActivity vca, String examType) {
        if (vca == null || vca.visitUuid == null || examType == null) return false;
        try {
            InteleHealthDatabaseHelper db = new InteleHealthDatabaseHelper(getContext());
            List<HeartLungRecordModel> records = db.getAllHeartLungRecords(vca.visitUuid);
            if (records == null) return false;
            for (HeartLungRecordModel r : records) {
                if (r == null || r.type == null || r.visitUuid == null) continue;
                if (vca.visitUuid.equals(r.visitUuid)
                        && examType.equalsIgnoreCase(r.type)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("SOUND_FLOW", "DB check failed", e);
        }
        return false;
    }

    /**
     * Infer "heart" vs "lung" from the LEVEL-1 parent name at the given index.
     * The level-2 node text alone (e.g. " Aortic") doesn't reveal which region.
     * Defaults to "heart" if the parent isn't a sound region.
     */
    private String inferSoundExamType(int examIndex) {
        if (physicalExam == null) return "heart";
        try {
            String parent = physicalExam.getExamParentNodeName(examIndex);
            if (parent != null && parent.toLowerCase().contains("lung")) {
                return "lung";
            }
        } catch (Exception ignored) {}
        return "heart";
    }

    private void showSanityDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(requireActivity(), R.drawable.ui2_ic_warning_sanity, getResources().getString(R.string.sanity_alert_title), "", true, getResources().getString(R.string.okay), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }

   /* @Override
    public void onSoundSaved() {
        advanceToNextExam(null);
    }*/

    /**
     * After a sound flow finishes (e.g. all 4 heart positions recorded),
     * advance the index past every remaining exam node of the same sound type
     * so we don't re-open SoundFragment on the sibling positions.
     *
     * Cough.json layout example:
     *   ... → Sound Heart: Aortic → Sound Heart: Pulmonic → Sound Heart: Tricuspid
     *       → Sound Heart: Mitral → Sound Lung: Anterior-1-Left-Top → ...
     *
     * If the user just finished "heart", we skip to the first node whose text
     * does NOT contain "sound heart", and add it as the next visible question.
     */
    private void skipPastSoundNodesAndAdvance(String examType) {
        if (physicalExam == null) return;

        String parentMatch = "lung".equals(examType) ? "sound lung" : "sound heart";
        int total = physicalExam.getTotalNumberOfExams();

        // Skip every consecutive exam node whose LEVEL-1 parent matches.
        // (e.g. just finished heart → skip Pulmonic, Tricuspid, Mitral so the
        // next visible card is the first lung position.)
        while (mCurrentComplainNodeOptionsIndex < total - 1) {
            int peekIdx = mCurrentComplainNodeOptionsIndex + 1;
            String peekParent;
            try {
                peekParent = physicalExam.getExamParentNodeName(peekIdx);
            } catch (Exception e) {
                break;
            }
            if (peekParent != null && peekParent.toLowerCase().contains(parentMatch)) {
                mCurrentComplainNodeOptionsIndex++;
            } else {
                break;
            }
        }

        // Now advance to the next non-matching node and add it to the UI.
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

            // Scroll the newly added question into view so the user sees it.
            if (mRecyclerView != null && mQuestionsListingAdapter != null) {
                final int last = mQuestionsListingAdapter.getItemCount() - 1;
                mRecyclerView.post(() -> mRecyclerView.smoothScrollToPosition(last));
            }

        } else {
            finishPhysicalExamAndAdvance();
        }
    }

    /**
     * Called when there are no more exam questions to ask in this protocol —
     * the LAST sound (or any other final question) has been answered.
     *
     * Sequence:
     *   1. STEP_4_PHYSICAL_SUMMARY_EXAMINATION — refreshes the side summary panel
     *      so the captured exam observations show up there.
     *   2. STEP_5_PAST_MEDICAL_HISTORY — auto-advances the body fragment to the
     *      next major step (Past Medical History), since otherwise the user is
     *      stuck on the completed exam screen with no Continue button.
     *
     * Edit mode keeps the original "click Submit to proceed" behavior.
     */
    private boolean mAdvancedToHistory = false;

    private void finishPhysicalExamAndAdvance() {
        if (mIsEditMode) {
            Toast.makeText(requireActivity(),
                    getString(R.string.please_submit_to_proceed_next_step),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // One-shot: avoid double-transition if both the sound_done listener
        // and the onAyuDeviceRequest safety-net both fire in the same flow.
        if (mAdvancedToHistory) {
            Log.d("SOUND_FLOW", "finishPhysicalExamAndAdvance: already advanced; ignoring");
            return;
        }
        mAdvancedToHistory = true;

        Log.d("SOUND_FLOW", "Physical exam complete — advancing to past history");

        // 1) Refresh the summary side panel.
    /*    mActionListener.onFormSubmitted(
                VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION,
                mIsEditMode,
                null
        );*/

        // 2) Move the main body to the next major step (Past Medical History).
        // Both transactions go through the FragmentManager queue and commit in
        // order; the summary panel and the body replacement target different
        // containers, so they don't conflict.
        mActionListener.onFormSubmitted(
                VisitCreationActivity.STEP_5_PAST_MEDICAL_HISTORY,
                mIsEditMode,
                null
        );
    }


}