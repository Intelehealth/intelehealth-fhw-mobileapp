package org.intelehealth.app.ayu.visit.ncd;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The "CBAC" visit step after an Eligible NCD result. Shows the questions of assets
 * cbac_assessment.json one at a time with the existing question cards, then the score and
 * the result pop-up. Answers stay in memory only.
 */
public class CbacAssessmentFragment extends Fragment {

    private static final String CBAC_FILE = "cbac_assessment.json";
    // Question-card version the adapter uses by default
    private static final String ENGINE_VERSION = "3.0";
    // Age question id in the CBAC file; its answer comes from registration
    private static final String AGE_QUESTION = "0.13238707089220347";
    private static final String AGE_ANSWER = "CBAC_AGE_ANSWER";
    private static final String MARATHI = "mr";
    // Input type of the waist "Enter value" option
    private static final String DECIMAL = "decimal";
    // Card type for the age: shown, not editable, confirmed with a button
    private static final String READ_ONLY = "readonly";

    private VisitCreationActionListener mActionListener;
    private String mPatientGender;
    private float mPatientAgeYearMonth;
    // Questions for this patient, in order (only the waist question for their gender)
    private final List<Node> mQuestions = new ArrayList<>();
    // Waist question id → its cm bands (0, 1 and 2 points), taken off the card for scoring
    private final Map<String, List<Node>> mWaistBands = new HashMap<>();
    private QuestionsListingAdapter mAdapter;
    private RecyclerView mRecyclerView;
    // Score and View result, shown after the last question
    private View mFooter;
    private View mScoreBar;
    private TextView mScoreLabel;
    private TextView mScoreValue;

    public CbacAssessmentFragment() {
        // Required empty public constructor
    }

    // Gender picks the waist question; age is shown as the answer to the age question
    public static CbacAssessmentFragment newInstance(String patientGender, float patientAgeYearMonth) {
        CbacAssessmentFragment fragment = new CbacAssessmentFragment();
        fragment.mPatientGender = patientGender;
        fragment.mPatientAgeYearMonth = patientAgeYearMonth;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cbac_assessment, container, false);
        mFooter = view.findViewById(R.id.ll_footer);
        mScoreBar = view.findViewById(R.id.ll_score);
        mScoreLabel = view.findViewById(R.id.tv_score_label);
        mScoreValue = view.findViewById(R.id.tv_score_value);
        view.findViewById(R.id.btn_view_result).setOnClickListener(v -> showResult());

        Node cbacNode = loadCbacNode();
        if (cbacNode == null) {
            // Never block the visit on a broken question file; posted to stay out of this fragment transaction
            view.post(() -> handOver(null));
            return view;
        }
        mQuestions.clear();
        for (Node question : cbacNode.getOptionsList()) {
            if (!CbacAssessmentRules.isQuestionShown(question.getGender(), mPatientGender)) continue;
            if (AGE_QUESTION.equals(question.getId())) showAgeAsAnswer(question);
            mQuestions.add(question);
        }
        if (mQuestions.isEmpty()) {
            view.post(() -> handOver(null));
            return view;
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
        info.setComplainName(cbacNode.getText());
        info.setComplainNameByLocale(""); // counter reads "1 of 6 questions"
        info.setOptionSize(mQuestions.size());
        HashMap<Integer, ComplainBasicInfo> infoMap = new HashMap<>();
        infoMap.put(0, info);

        mAdapter = new QuestionsListingAdapter(mRecyclerView, getActivity(), false, false, null, 0, infoMap, false, new QuestionSelection());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRootNodeIndex(0);
        // Start with the first question; the next one appears after each answer (Confirm for the age)
        mAdapter.addItem(mQuestions.get(0), ENGINE_VERSION);
        return view;
    }

    private Node loadCbacNode() {
        // Next to physExam.json in assets; not engines/, which lists visit reasons
        try {
            JSONObject json = FileUtils.encodeJSON(requireContext(), CBAC_FILE);
            if (json == null) return null;
            takeWaistBands(json);
            // The engine shows only English or Hindi help text, so put the Marathi one in its place
            if (MARATHI.equals(new SessionManager(requireContext()).getCurrentLang())) useMarathiPopups(json);
            return new Node(json);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    // The engine shows a number field only for an option without children, so the bands under
    // "Enter value" are kept here for scoring
    private void takeWaistBands(JSONObject cbac) throws Exception {
        JSONArray questions = cbac.getJSONArray("options");
        for (int i = 0; i < questions.length(); i++) {
            JSONObject question = questions.getJSONObject(i);
            JSONArray options = question.optJSONArray("options");
            if (options == null || options.length() != 1) continue;
            JSONObject enterValue = options.getJSONObject(0);
            if (!DECIMAL.equals(enterValue.optString("input-type"))) continue;

            List<Node> bands = new ArrayList<>();
            JSONArray ranges = enterValue.optJSONArray("options");
            JSONArray bandJson = ranges == null || ranges.length() == 0 ? null : ranges.getJSONObject(0).optJSONArray("options");
            if (bandJson != null) {
                for (int b = 0; b < bandJson.length(); b++) bands.add(new Node(bandJson.getJSONObject(b)));
            }
            mWaistBands.put(question.getString("id"), bands);
            enterValue.remove("options");
        }
    }

    private static void useMarathiPopups(JSONObject node) throws Exception {
        String marathi = node.optString("pop-up-mr");
        if (!marathi.isEmpty()) node.put("pop-up", marathi);
        JSONArray options = node.optJSONArray("options");
        if (options == null) return;
        for (int i = 0; i < options.length(); i++) useMarathiPopups(options.getJSONObject(i));
    }

    // Replaces the age bands with a locked field, e.g. "32 Years", and a Confirm button
    private void showAgeAsAnswer(Node ageQuestion) {
        try {
            String age = getString(R.string.cbac_age_years, ageYears());
            JSONObject answer = new JSONObject();
            answer.put("id", AGE_ANSWER);
            answer.put("text", age);
            answer.put("language", age);
            answer.put("input-type", READ_ONLY);
            ageQuestion.setOptionsList(new ArrayList<>(Collections.singletonList(new Node(answer))));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private int ageYears() {
        return NcdEligibilityRules.completedYears(mPatientAgeYearMonth);
    }

    // Adds the next question card, or shows the score after the last one
    private void showNextQuestion() {
        int next = mAdapter.geItems().size();
        if (next < mQuestions.size()) {
            mAdapter.addItem(mQuestions.get(next), ENGINE_VERSION);
            VisitUtils.scrollNow(mRecyclerView, 300, 0, 500, false, false);
            // Again once the card is filled in (the adapter fills it after a delay), as on Visit reason
            VisitUtils.scrollNow(mRecyclerView, 1400, 0, 1400, false, false);
        } else {
            mFooter.setVisibility(View.VISIBLE);
            updateScore();
            // The footer shortens the list, so bring the last card back into view
            VisitUtils.scrollNow(mRecyclerView, 300, 0, 500, false, false);
        }
    }

    // Total of the age points and each answer's points; null while any answer is missing
    private Integer calculateScore() {
        int total = CbacAssessmentRules.agePoints(ageYears());
        for (Node question : mQuestions) {
            if (AGE_QUESTION.equals(question.getId())) continue;
            Node answer = scoredOption(question);
            if (answer == null) return null;
            Integer points = CbacAssessmentRules.pointsFromText(answer.getText());
            if (points == null) {
                FirebaseCrashlytics.getInstance().log("CBAC option without points: " + answer.getId());
                return null;
            }
            total += points;
        }
        return total;
    }

    // The option whose "(n)" counts: the tapped one, or for waist the band of the typed value
    private Node scoredOption(Node question) {
        if (mWaistBands.containsKey(question.getId())) {
            Double waist = typedWaist(question);
            if (waist == null) return null;
            int band = CbacAssessmentRules.waistBand(mPatientGender, CbacAssessmentRules.roundWaistToHalf(waist));
            List<Node> bands = mWaistBands.get(question.getId());
            return band >= 0 && band < bands.size() ? bands.get(band) : null;
        }
        List<Node> selected = selectedOptions(question);
        return selected.isEmpty() ? null : selected.get(0);
    }

    // Null until a valid value is submitted on the waist card
    private static Double typedWaist(Node question) {
        Node enterValue = question.getOptionsList().get(0);
        return enterValue.isDataCaptured() ? CbacAssessmentRules.parseWaistCm(enterValue.getLanguage()) : null;
    }

    // Green for 4 and below, orange above 4, as in Figma
    private void updateScore() {
        Integer score = calculateScore();
        boolean highRisk = score != null && CbacAssessmentRules.isHighRisk(score);
        int textColor = ContextCompat.getColor(requireContext(), highRisk ? R.color.cbac_orange : R.color.badgeGreenText);
        mScoreBar.setBackgroundResource(highRisk ? R.drawable.bg_cbac_score_high : R.drawable.bg_cbac_score_low);
        mScoreLabel.setTextColor(textColor);
        mScoreValue.setTextColor(textColor);
        mScoreValue.setText(score == null ? "–" : String.valueOf(score));
    }

    // Collects the answers, stops on anything missing, then shows the result pop-up
    private void showResult() {
        Integer score = calculateScore();
        if (score == null) {
            Toast.makeText(getActivity(), R.string.ncd_answer_all_questions, Toast.LENGTH_SHORT).show();
            return;
        }
        CbacAssessmentState state = new CbacAssessmentState();
        state.setAgeYears(ageYears());
        for (Node question : mQuestions) {
            if (AGE_QUESTION.equals(question.getId())) continue;
            Node answer = scoredOption(question);
            state.getAnswers().put(question.getId(), answer.getId());
            state.getAnswerTexts().put(question.getId(), answer.getText());
            if (mWaistBands.containsKey(question.getId())) {
                state.setWaistCm(CbacAssessmentRules.roundWaistToHalf(typedWaist(question)));
            }
        }
        state.setTotalScore(score);
        CbacResultDialog.show(requireContext(), state.isHighRisk(), () -> handOver(state));
    }

    // Tells the activity CBAC is finished; state is null when the question file failed to load
    private void handOver(CbacAssessmentState state) {
        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_0_CBAC_ASSESSMENT_DONE, false, state);
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

    // Callbacks from the question cards
    private class QuestionSelection implements OnItemSelection {

        @Override
        public void onSelect(Node node, int index, boolean isSkipped, Node selectedNode) {
            // A waist like "." or "0" stays on the card to be corrected
            Node question = mQuestions.get(index);
            if (mWaistBands.containsKey(question.getId()) && typedWaist(question) == null) {
                Toast.makeText(getActivity(), R.string.cbac_enter_valid_waist, Toast.LENGTH_SHORT).show();
                if (mFooter.getVisibility() == View.VISIBLE) updateScore();
                return;
            }
            // Only the newest card moves the flow on; changing an earlier answer just updates it
            if (index == mAdapter.geItems().size() - 1) showNextQuestion();
            else if (mFooter.getVisibility() == View.VISIBLE) updateScore();
            // After the first answer, cards list from the top
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).setStackFromEnd(false);
        }

        @Override
        public void needTitleChange(String title) {
            // Not used: the activity sets the CBAC title
        }

        @Override
        public void onAllAnswered(boolean isAllAnswered) {
            // Not used: showNextQuestion shows the score
        }

        @Override
        public void onCameraRequest() {
            // Not used: no photos in CBAC
        }

        @Override
        public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
            // Not used: no photos in CBAC
        }

        @Override
        public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {
            // Not used: CBAC questions have no nested sub-questions
        }

        @Override
        public void onAyuDeviceRequest(Node node) {
            // Not used: no device readings in CBAC
        }
    }
}
