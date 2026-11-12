package org.intelehealth.app.ayu.visit.ncd;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The "NCD Eligibility" visit step. Shows the questions of assets ncd_eligibility.json one at a
 * time with the existing question cards, then the result pop-up. Answers stay in memory only.
 */
public class NcdEligibilityFragment extends Fragment {

    static final String NCD_ELIGIBILITY_FILE = "ncd_eligibility.json";
    // Question-card version the adapter uses by default
    private static final String ENGINE_VERSION = "3.0";
    // Options with this input type open the in-card photo box
    private static final String INPUT_TYPE_CAMERA = "camera";
    // Question and option IDs in ncd_eligibility.json
    private static final String PREGNANCY = "NCD_PREGNANCY";
    private static final String PREGNANCY_YES = "NCD_PREGNANCY_YES";
    private static final String MULTIMORBIDITY = "NCD_MULTIMORBIDITY";
    private static final String MULTIMORBIDITY_YES = "NCD_MULTIMORBIDITY_YES";
    static final String KNOWN_CASE = "NCD_KNOWN_CASE";
    private static final String URGENT_SYMPTOMS = "NCD_URGENT_SYMPTOMS";
    private static final String URGENT_NONE = "NCD_URGENT_NONE";

    private VisitCreationActionListener mActionListener;
    private String mPatientGender;
    private float mPatientAgeYearMonth;
    // Questions for this patient, in order (pregnancy left out when not applicable)
    private final List<Node> mQuestions = new ArrayList<>();
    private QuestionsListingAdapter mAdapter;
    private RecyclerView mRecyclerView;
    // Back + View Eligibility buttons, shown after the last question
    private View mFooter;
    // True once the answers and photos were passed to the activity
    private boolean mResultHandedOver = false;

    public NcdEligibilityFragment() {
        // Required empty public constructor
    }

    // Gender and age decide whether the pregnancy question is shown
    public static NcdEligibilityFragment newInstance(String patientGender, float patientAgeYearMonth) {
        NcdEligibilityFragment fragment = new NcdEligibilityFragment();
        fragment.mPatientGender = patientGender;
        fragment.mPatientAgeYearMonth = patientAgeYearMonth;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        // Camera/gallery results come back through the activity into the photo card
        ((VisitCreationActivity) context).setImageUtilsListener(new VisitCreationActivity.ImageUtilsListener() {
            @Override
            public void onImageReady(Bundle bundle) {
                mAdapter.addImageInLastNode(bundle.getString("image"));
            }

            @Override
            public void onImageReadyForDelete(int nodeIndex, int imageIndex, String imageName) {
                mAdapter.removeImageInLastNode(nodeIndex, imageIndex, imageName);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ncd_eligibility, container, false);
        mFooter = view.findViewById(R.id.ll_footer);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        view.findViewById(R.id.btn_view_eligibility).setOnClickListener(v -> showEligibility());

        Node eligibilityNode = loadEligibilityNode();
        if (eligibilityNode == null) {
            // Never block the visit on a broken question file; posted to stay out of this fragment transaction
            view.post(() -> handOver(null));
            return view;
        }
        // Pregnancy is asked only to women aged 30-49
        mQuestions.clear();
        for (Node question : eligibilityNode.getOptionsList()) {
            if (PREGNANCY.equals(question.getId()) && !NcdEligibilityRules.isPregnancyQuestionShown(mPatientGender, mPatientAgeYearMonth))
                continue;
            mQuestions.add(question);
        }

        mRecyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        // First card sits at the bottom, as in Past medical history
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        // Redraw a card in place: the adapter fills cards after a delay, which races the change cross-fade
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        // Header info the adapter uses for the question counter
        ComplainBasicInfo info = new ComplainBasicInfo();
        info.setComplainName(eligibilityNode.getText());
        info.setComplainNameByLocale(""); // counter reads "1 of 4 questions"
        info.setOptionSize(mQuestions.size());
        HashMap<Integer, ComplainBasicInfo> infoMap = new HashMap<>();
        infoMap.put(0, info);

        mAdapter = new QuestionsListingAdapter(mRecyclerView, getActivity(), false, false, null, 0, infoMap, false, new QuestionSelection());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRootNodeIndex(0);
        // Start with the first question; the next one appears after each answer
        mAdapter.addItem(mQuestions.get(0), ENGINE_VERSION);
        return view;
    }

    private Node loadEligibilityNode() {
        // Next to physExam.json in assets; not engines/, which lists visit reasons
        try {
            JSONObject json = FileUtils.encodeJSON(requireContext(), NCD_ELIGIBILITY_FILE);
            return json == null ? null : new Node(json);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    // Adds the next question card, or shows the footer after the last one
    private void showNextQuestion() {
        int next = mAdapter.geItems().size();
        if (next < mQuestions.size()) {
            mAdapter.addItem(mQuestions.get(next), ENGINE_VERSION);
            VisitUtils.scrollNow(mRecyclerView, 300, 0, 500, false, false);
            // Again once the card is filled in (the adapter fills it after a delay), as on Visit reason
            VisitUtils.scrollNow(mRecyclerView, 1400, 0, 1400, false, false);
        } else {
            mFooter.setVisibility(View.VISIBLE);
            // The footer shortens the list, so bring the last card back into view
            VisitUtils.scrollNow(mRecyclerView, 300, 0, 500, false, false);
        }
    }

    // A photo is only kept while DM, HTN or Both is selected
    private void clearPhotosIfNotNeeded(Node question, int index) {
        List<Node> selected = selectedOptions(question);
        boolean needsPhoto = !selected.isEmpty() && INPUT_TYPE_CAMERA.equals(selected.get(0).getInputType());
        List<String> photos = question.getImagePathList();
        if (needsPhoto || photos == null || photos.isEmpty()) return;
        for (String photo : photos) deletePhoto(photo);
        question.removeImagesAllNestedNode();
        mRecyclerView.post(() -> mAdapter.notifyItemChanged(index));
    }

    // Collects the answers, stops on anything missing, then shows the result pop-up
    private void showEligibility() {
        NcdEligibilityState state = new NcdEligibilityState();
        List<String> symptomNames = new ArrayList<>();
        for (Node question : mQuestions) {
            List<Node> selected = selectedOptions(question);
            if (selected.isEmpty()) {
                Toast.makeText(getActivity(), R.string.ncd_answer_all_questions, Toast.LENGTH_SHORT).show();
                return;
            }
            String answerId = selected.get(0).getId();
            switch (question.getId()) {
                case PREGNANCY:
                    state.setPregnant(PREGNANCY_YES.equals(answerId));
                    break;
                case MULTIMORBIDITY:
                    state.setMultimorbidity(MULTIMORBIDITY_YES.equals(answerId));
                    break;
                case KNOWN_CASE:
                    List<String> photos = question.getImagePathList();
                    boolean hasPhoto = photos != null && !photos.isEmpty();
                    // DM, HTN or Both needs at least one medicine photo
                    if (INPUT_TYPE_CAMERA.equals(selected.get(0).getInputType()) && !hasPhoto) {
                        Toast.makeText(getActivity(), R.string.ncd_add_medicine_photo, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    state.setKnownCaseOptionId(answerId);
                    state.setKnownCaseText(selected.get(0).getText());
                    if (hasPhoto) state.getMedicinePhotoPaths().addAll(photos);
                    break;
                case URGENT_SYMPTOMS:
                    for (Node option : selected) {
                        // "None" is not a symptom
                        if (URGENT_NONE.equals(option.getId())) continue;
                        state.getUrgentSymptomIds().add(option.getId());
                        state.getUrgentSymptomTexts().add(option.getText());
                        symptomNames.add(option.findDisplay());
                    }
                    break;
            }
        }
        // Urgent beats Not eligible, which beats Eligible
        state.setResult(NcdEligibilityRules.evaluate(Boolean.TRUE.equals(state.getPregnant()), state.isMultimorbidity(),
                state.isKnownCaseBoth(), !state.getUrgentSymptomIds().isEmpty()));
        NcdEligibilityResultDialog.show(requireContext(), state, symptomNames, () -> handOver(state));
    }

    // Tells the activity NCD is finished; state is null when the question file failed to load
    private void handOver(NcdEligibilityState state) {
        mResultHandedOver = true;
        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_0_NCD_ELIGIBILITY_DONE, false, state);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Visit discarded or left without a result: the photos have no other owner, so remove them
        if (mResultHandedOver) return;
        for (Node question : mQuestions) {
            if (!KNOWN_CASE.equals(question.getId()) || question.getImagePathList() == null) continue;
            for (String photo : question.getImagePathList()) deletePhoto(photo);
        }
    }

    // The options picked on a question card
    private static List<Node> selectedOptions(Node question) {
        List<Node> selected = new ArrayList<>();
        if (question.getOptionsList() == null) return selected;
        for (Node option : question.getOptionsList()) {
            if (option.isSelected()) selected.add(option);
        }
        return selected;
    }

    // Photos are plain files on the phone, nothing in the database yet
    private static void deletePhoto(String path) {
        if (path != null) new File(path).delete();
    }

    // Callbacks from the question cards
    private class QuestionSelection implements OnItemSelection {

        @Override
        public void onSelect(Node node, int index, boolean isSkipped, Node selectedNode) {
            Node question = mAdapter.geItems().get(index);
            if (KNOWN_CASE.equals(question.getId())) clearPhotosIfNotNeeded(question, index);
            // Only the newest card moves the flow on; changing an earlier answer just updates it
            if (index == mAdapter.geItems().size() - 1) showNextQuestion();
            // After the first answer, cards list from the top
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).setStackFromEnd(false);
        }

        @Override
        public void needTitleChange(String title) {
            // Not used: the activity sets the NCD title
        }

        @Override
        public void onAllAnswered(boolean isAllAnswered) {
            // Not used: showNextQuestion shows the footer
        }

        // The activity opens the camera/gallery; the photo arrives in onImageReady
        @Override
        public void onCameraRequest() {
            mActionListener.onCameraOpenRequest();
        }

        // ✕ on a photo: delete the file and its thumbnail
        @Override
        public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
            deletePhoto(image);
            mAdapter.removeImageInLastNode(nodeIndex, imageIndex, image);
        }

        @Override
        public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {
            // Not used: NCD questions have no nested sub-questions
        }

        @Override
        public void onAyuDeviceRequest(Node node) {
            // Not used: no device readings in NCD
        }
    }
}
