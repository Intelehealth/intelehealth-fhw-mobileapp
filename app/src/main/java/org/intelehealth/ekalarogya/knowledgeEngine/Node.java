package org.intelehealth.ekalarogya.knowledgeEngine;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ekalarogya.models.AnswerResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.utilities.InputFilterMinMax;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import org.intelehealth.ekalarogya.activities.cameraActivity.CameraActivity;
import org.intelehealth.ekalarogya.activities.complaintNodeActivity.CustomArrayAdapter;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node implements Serializable {

    private String validation = ""; // MAX_TODAY , MIN_TODAY
    private String id;
    private String text;
    private String display;
    private String display_oriya;
    private String display_cebuno;
    private String display_hindi, display_gujarati, display_assamese, display_bengali, display_kannada;
    private String language;
    private String choiceType;
    private String inputType;
    private String physicalExams;
    private List<Node> optionsList;
    private String associatedComplaint;
    private String jobAidFile;
    private String jobAidType;
    private String pop_up;
    private String gender;
    private String min_age;
    private String max_age;
    private boolean isMultiChoice = false;
    private boolean isExcludedFromMultiChoice = false; //exclude-from-multi-choice

    //for Associated Complaints and medical history only
    private String positiveCondition;
    private String negativeCondition;

    //These are specific for physical exams
    private boolean rootNode;

    private boolean complaint;
    private boolean required = false;
    private boolean terminal;
    private boolean hasAssociations;
    private boolean aidAvailable;
    private boolean selected;
    private boolean subSelected;
    private boolean hasPhysicalExams;
    private boolean hasPopUp;
    private boolean subPopUp;
    private int associated_symptoms = 0;

    private boolean isNoSelected;

    private List<String> imagePathList;

    public static String bullet = "\u2022";
    public static String big_bullet = "\u25CF";
    //    public static String bullet_hollow = "\u25CB";
    public static String bullet_hollow = "\u2022";
    public static String bullet_arrow = "\u25BA";
    public static String next_line = "<br/>";
    String space = "\t";


    //• = \u2022, ● = \u25CF, ○ = \u25CB, ▪ = \u25AA, ■ = \u25A0, □ = \u25A1, ► = \u25BA

    private String imagePath;

    /**
     * Nodes refer to the structure that is used for a decision tree or mindmap.
     * The knowledgeEngine object is stored in the same structure where the there is a root knowledgeEngine which contains all the sub-nodes.
     * The nodes are also tagged based on the attributes each JSON object shows.
     * <p>
     * Most nodes are single choice questions. Therefore, they can just be clicked and selected.
     * Some nodes may be multi-choice, in which case there must be an attribute within the JSON to dictate that.
     * <p>
     * text - the text that is displayed on the app to user
     * language - the text that is displayed after answering a question
     * differs from the text attribute in that this is the response form of a question
     * inputType - dictates if the knowledgeEngine is something other that choice-based
     * types include: text, number, date, duration, area, range, frequency
     * physicalExams - any physical exams that should be triggered in the application if the knowledgeEngine is selected
     * optionsList - container of sub-nodes of the current knowledgeEngine
     * associatedComplaint - just like the name says
     * jobAidFile - the filename of the job aid
     * should be stored in the physicalExamAssets folder within the app when compiling
     * jobAidType - options are audio, video, or image
     *
     * @param jsonNode A JSON Object of a mindmap should be used here. The object that is generated will hold objects within it.
     */
    public Node(JSONObject jsonNode) {
        try {
            this.id = jsonNode.getString("id");

            this.isMultiChoice = jsonNode.optBoolean("multi-choice");

            this.isExcludedFromMultiChoice = jsonNode.optBoolean("exclude-from-multi-choice");
            this.validation = jsonNode.optString("validation");

            this.text = jsonNode.getString("text");

            this.gender = jsonNode.optString("gender");

            this.min_age = jsonNode.optString("age_min");

            this.max_age = jsonNode.optString("age_max");

            JSONArray optionsArray = jsonNode.optJSONArray("options");
            if (optionsArray == null) {
                this.terminal = true;
            } else {
                this.terminal = false;
                this.optionsList = createOptions(optionsArray);
            }

            this.display = jsonNode.optString("display");
            if (this.display.isEmpty()) {
                this.display = jsonNode.optString("display");
            }
            if (this.display.isEmpty()) {
                this.display = this.text;
            }

            this.display_oriya = jsonNode.optString("display-or");
            if (this.display_oriya.isEmpty()) {
                this.display_oriya = jsonNode.optString("display-or");
            }
            if (this.display_oriya.isEmpty()) {
                this.display_oriya = this.display;
            }
            this.display_cebuno = jsonNode.optString("display-cb");
            if (this.display_cebuno.isEmpty()) {
                this.display_cebuno = jsonNode.optString("display-cb");
            }
            if (this.display_cebuno.isEmpty()) {
                this.display_cebuno = this.display;
            }

            this.display_hindi = jsonNode.optString("display-hi");
            if (this.display_hindi.isEmpty()) {
                this.display_hindi = jsonNode.optString("display-hi");
            }
            if (this.display_hindi.isEmpty()) {
                this.display_hindi = this.display;
            }

            this.display_bengali = jsonNode.optString("display-bn");
            if (this.display_bengali.isEmpty()) {
                this.display_bengali = jsonNode.optString("display-bn");
            }
            if (this.display_bengali.isEmpty()) {
                this.display_bengali = this.display;
            }

            this.display_kannada = jsonNode.optString("display-kn");
            if (this.display_kannada.isEmpty()) {
                this.display_kannada = jsonNode.optString("display-kn");
            }
            if (this.display_kannada.isEmpty()) {
                this.display_kannada = this.display;
            }

            this.display_gujarati = jsonNode.optString("display-gu");
            if (this.display_gujarati.isEmpty()) {
                this.display_gujarati = jsonNode.optString("display-gu");
            }
            if (this.display_gujarati.isEmpty()) {
                this.display_gujarati = this.display;
            }

            this.display_assamese = jsonNode.optString("display-as");
            if (this.display_assamese.isEmpty()) {
                this.display_assamese = jsonNode.optString("display-as");
            }
            if (this.display_assamese.isEmpty()) {
                this.display_assamese = this.display;
            }

            this.language = jsonNode.optString("language");
            if (this.language.isEmpty()) {
                this.language = this.text;
            }

            //Only for physical exams
            if (!this.language.isEmpty()) {
                if (this.language.contains(":")) {
                    this.rootNode = true;
                }
            }

            this.inputType = jsonNode.optString("input-type");

            this.physicalExams = jsonNode.optString("perform-physical-exam");
            this.hasPhysicalExams = !(this.physicalExams == null);

            this.jobAidFile = jsonNode.optString("job-aid-file");
            if (!jobAidFile.isEmpty()) {
                this.jobAidType = jsonNode.optString("job-aid-type");
                this.aidAvailable = true;
            } else {
                this.aidAvailable = false;
            }

            this.associatedComplaint = jsonNode.optString("associated-complaint");
            this.hasAssociations = !associatedComplaint.isEmpty();

            this.selected = false;
            this.isNoSelected = false;

            this.choiceType = jsonNode.optString("choice-type");

//            this.required = false;

            this.required = jsonNode.optBoolean("isRequired");
            this.positiveCondition = jsonNode.optString("pos-condition");
            this.negativeCondition = jsonNode.optString("neg-condition");

            this.pop_up = jsonNode.optString("pop-up");
            this.hasPopUp = !pop_up.isEmpty();

        } catch (JSONException | NullPointerException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /**
     * Makes a copy of the knowledgeEngine, so that the original reference knowledgeEngine is not modified.
     *
     * @param source source knowledgeEngine to copy into a new knowledgeEngine. Will always default as unselected.
     */
    public Node(Node source) {
        this.id = source.id;
        this.isMultiChoice = source.isMultiChoice;
        this.isExcludedFromMultiChoice = source.isExcludedFromMultiChoice;
        this.text = source.text;
        this.display = source.display;
        this.display_hindi = source.display_hindi;
        this.display_bengali = source.display_bengali;
        this.display_kannada = source.display_kannada;
        this.display_oriya = source.display_oriya;
        this.display_cebuno = source.display_cebuno;
        this.display_gujarati = source.display_gujarati;
        this.display_assamese = source.display_assamese;
        this.optionsList = source.optionsList;
        this.terminal = source.terminal;
        this.language = source.language;
        this.gender = source.gender;
        this.min_age = source.min_age;
        this.max_age = source.max_age;
        this.inputType = source.inputType;
        this.physicalExams = source.physicalExams;
        this.complaint = source.complaint;
        this.pop_up = source.pop_up;
        this.jobAidFile = source.jobAidFile;
        this.jobAidType = source.jobAidType;
        this.aidAvailable = source.aidAvailable;
        this.associatedComplaint = source.associatedComplaint;
        this.hasAssociations = source.hasAssociations;
        this.hasPhysicalExams = source.hasPhysicalExams;
        this.hasPopUp = source.hasPopUp;
        this.selected = false;
        this.isNoSelected = false;
        this.associated_symptoms = 0;
        this.required = source.required;
        this.positiveCondition = source.positiveCondition;
        this.negativeCondition = source.negativeCondition;
    }

    public static void subLevelQuestion(final Node node, final Activity context, final QuestionsAdapter callingAdapter,
                                        final String imagePath, final String imageName) {

        node.setSelected(true);
        List<Node> mNodes = node.getOptionsList();
        final CustomArrayAdapter adapter = new CustomArrayAdapter(context, R.layout.list_item_subquestion, mNodes);
        final MaterialAlertDialogBuilder subQuestion = new MaterialAlertDialogBuilder(context);

        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_subquestion, null);
        ImageView imageView = convertView.findViewById(R.id.dialog_subquestion_image_view);
        if (node.isAidAvailable()) {
            if (node.getJobAidType().equals("image")) {
                String drawableName = node.getJobAidFile();
                int resID = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                imageView.setImageResource(resID);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }

        subQuestion.setTitle(node.findDisplay());
        ListView listView = convertView.findViewById(R.id.dialog_subquestion_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setClickable(true);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                node.getOption(position).toggleSelected();
                adapter.notifyDataSetChanged();
                Node currentNode = node.getOption(position);
                if (node.optionsList != null && !node.optionsList.isEmpty() && !node.isMultiChoice) {
                    for (int i = 0; i < node.optionsList.size(); i++) {
                        Node innerNode = node.optionsList.get(i);
                        innerNode.setUnselected();
                    }
                    currentNode.setSelected(true);
                }

                if (node.optionsList != null && !node.optionsList.isEmpty() && node.isMultiChoice) {
                    if (currentNode.isExcludedFromMultiChoice) {

                        if (currentNode.isSelected()) {
                            for (int i = 0; i < node.optionsList.size(); i++) {
                                Node innerNode = node.optionsList.get(i);
                                innerNode.setUnselected();
                            }
                            currentNode.setSelected(true);
                        } else
                            currentNode.setUnselected();

                    } else {
                        for (int i = 0; i < node.optionsList.size(); i++) {
                            Node innerNode = node.optionsList.get(i);
                            if (innerNode.isExcludedFromMultiChoice)
                                innerNode.setUnselected();
                        }
                    }

                }
                if (node.getOption(position).getInputType() != null) {
                    subHandleQuestion(node.getOption(position), context, adapter, imagePath, imageName);
                }

                if (!node.getOption(position).isTerminal()) {
                    subLevelQuestion(node.getOption(position), context, callingAdapter, imagePath, imageName);
                }
            }
        });

        subQuestion.setView(listView);
        subQuestion.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setText(node.generateLanguage());
                callingAdapter.notifyDataSetChanged();
                dialog.dismiss();
                if (node.anySubSelected() && node.anySubPopUp()) {
                    node.generatePopUp(context);
                }
            }
        });
        subQuestion.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.toggleSelected();
                callingAdapter.refreshChildAdapter();
                callingAdapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });

        subQuestion.setView(convertView);
        AlertDialog dialog = subQuestion.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    // check the answer status for the Associated_symptoms
    public int getAssociated_symptoms() {
        return associated_symptoms;
    }

    public void setAssociated_symptoms(int associated_symptoms) {
        this.associated_symptoms = associated_symptoms;
    }

    //Terminal nodes are important to identify to know so that the app does not keep looking for sub-nodes.
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    //Only complaints should be presented to the user at Complaint Select.
    public boolean isComplaint() {
        return complaint;
    }

    //In certain instances, the input is added to the starter language given to the user.
    public void addLanguage(String newText) {
        //Log.d("Node", language);
        if (language.contains("_")) {
            language = language.replace("_", newText);
            //Log.d("Node", language);
        } else {
            language = newText;
            //Log.d("Node", language);
        }
    }

    public String findDisplay() {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

//        String locale = Locale.getDefault().getLanguage();
        String locale = sessionManager.getCurrentLang();

        switch (locale) {
            case "en": {
                //Log.i(TAG, "findDisplay: eng");
                if (display != null && display.isEmpty()) {
                    //Log.i(TAG, "findDisplay: eng txt");
                    return text;
                } else {
                    //Log.i(TAG, "findDisplay: eng dis");
                    return display;
                }
            }
            case "or": {
                //Log.i(TAG, "findDisplay: ori");
                if (display_oriya != null && !display_oriya.isEmpty()) {
                    //Log.i(TAG, "findDisplay: ori dis");
                    return display_oriya;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }

            case "gu": {
                //Log.i(TAG, "findDisplay: ori");
                if (display_gujarati != null && !display_gujarati.isEmpty()) {
                    //Log.i(TAG, "findDisplay: ori dis");
                    return display_gujarati;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }

            }
//            case "cb": {
//                //Log.i(TAG, "findDisplay: cb");
//                if (display_cebuno != null && !display_cebuno.isEmpty()) {
//                    //Log.i(TAG, "findDisplay: cb ");
//                    return display_cebuno;
//                } else {
//                    if (display == null || display.isEmpty()) {
//                        //Log.i(TAG, "findDisplay: eng/o txt");
//                        return text;
//                    } else {
//                        //Log.i(TAG, "findDisplay: eng/o dis");
//                        return display;
//                    }
//                }
//            }
            case "hi": {
                //Log.i(TAG, "findDisplay: cb");
                if (display_hindi != null && !display_hindi.isEmpty()) {
                    //Log.i(TAG, "findDisplay: cb ");
                    return display_hindi;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "bn": {
                if (display_bengali != null && !display_bengali.isEmpty()) {
                    return display_bengali;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }

            case "kn": {
                if (display_kannada != null && !display_kannada.isEmpty()) {
                    return display_kannada;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }

            case "as": {
                //Log.i(TAG, "findDisplay: cb");
                if (display_assamese != null && !display_assamese.isEmpty()) {
                    //Log.i(TAG, "findDisplay: cb ");
                    return display_assamese;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            default: {
                {
                    if (display != null && display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }
        }
    }

    public String getPositiveCondition() {
        return positiveCondition;
    }

    public void setPositiveCondition(String positiveCondition) {
        this.positiveCondition = positiveCondition;
    }

    public String getNegativeCondition() {
        return negativeCondition;
    }

    public void setNegativeCondition(String negativeCondition) {
        this.negativeCondition = negativeCondition;
    }

    public int size() {
        return optionsList.size();
    }

    public boolean hasAssociations() {
        return hasAssociations;
    }

    public boolean isHasPhysicalExams() {
        return hasPhysicalExams;
    }

    public String getAssociatedComplaint() {
        return associatedComplaint;
    }

    public boolean isAidAvailable() {
        return aidAvailable;
    }

    public List<Node> getOptionsList() {
        return optionsList;
    }

    public Node getOptionByName(String name) {
        Node foundNode = null;
        if (optionsList != null) {
            for (Node node : optionsList) {
                if (node.getText().equals(name)) {
                    foundNode = node;
                }
            }
        }
        if (foundNode == null) Log.i(TAG, "getOptionByName [Not Found]: " + name);
        return foundNode;
    }

    public Node getOption(int i) {
        return optionsList.get(i);
    }

    public void addOptions(Node node) {
        optionsList.add(node);
    }

    public String getJobAidFile() {
        return jobAidFile;
    }

    public String getJobAidType() {
        return jobAidType;
    }

    public String getPop_up() {
        return pop_up;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isNoSelected() {
        return isNoSelected;
    }

    public void setNoSelected(boolean noSelected) {
        isNoSelected = noSelected;
    }

    public void setUnselected() {
        selected = false;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public void setSelected(boolean isSelectedStatus) {
        selected = isSelectedStatus;
    }

    public boolean anySubSelected() {
        if (!terminal) {
            for (int i = 0; i < optionsList.size(); i++) {
                if (optionsList.get(i).isSelected()) {
                    subSelected = true;
                    break;
                } else {
                    subSelected = false;
                }
            }
            return subSelected;
        } else {
            return false;
        }
    }

    public boolean anySubPopUp() {
        if (!terminal) {
            for (int i = 0; i < optionsList.size(); i++) {
                if (optionsList.get(i).isSelected()) {
                    subPopUp = true;
                    break;
                } else {
                    subPopUp = false;
                }
            }
            return subPopUp;
        } else {
            return false;
        }
    }

    public static void askText(final Node node, Activity context, final QuestionsAdapter adapter) {
        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!dialogEditText.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                    } else {
                        node.addLanguage(dialogEditText.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    node.setSelected(false);
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                    } else {
                        node.addLanguage("Question not answered");
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }

                node.setSelected(true);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!dialogEditText.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                    } else {
                        node.addLanguage(dialogEditText.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                } else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                    } else {
                        node.addLanguage("Question not answered");
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }
//                node.setSelected(false);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void askBP(final Node node, Activity context, final QuestionsAdapter adapter) {
        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        InputFilter inputFilter = (charSequence, i, i1, spanned, i2, i3) -> {
            if (!charSequence.toString().matches("[0-9/]"))
                return charSequence.toString().replace(charSequence.toString(), "");
            else
                return charSequence;
        };

        dialogEditText.setFilters(new InputFilter[]{inputFilter});

        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!dialogEditText.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                    } else {
                        node.addLanguage(dialogEditText.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    node.setSelected(false);
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                    } else {
                        node.addLanguage("Question not answered");
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }

                node.setSelected(true);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!dialogEditText.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                    } else {
                        node.addLanguage(dialogEditText.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                } else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                    } else {
                        node.addLanguage("Question not answered");
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }
//                node.setSelected(false);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public String generateLanguage() {

        String raw = "";
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected()) {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms") || associatedTest.trim().equals("जुड़े लक्षण") ||
                            (associatedTest.trim().equals("H/o specific illness")) || associatedTest.trim().equals("સંકળાયેલ લક્ષણો") ||
                            (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("সংশ্লিষ্ট লক্ষণ")))) {

                        if ((associatedTest.trim().equals("Associated symptoms")) || associatedTest.trim().equals("जुड़े लक्षण") ||
                                associatedTest.trim().equals("સંકળાયેલ લક્ષણો") || (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))
                                || (associatedTest.trim().equals("সংশ্লিষ্ট লক্ষণ"))) {
                            if (!generateAssociatedSymptomsOrHistory(node_opt).isEmpty()) {
                                raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                                raw = raw.substring(6);
                                Log.e("FinalText= ", raw);
                            } else {
                                Log.e("FinalText= ", raw);

                            }
                        } else {
                            raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                        }

                    } else {
                        if (!node_opt.getLanguage().isEmpty()) {
                            if (node_opt.getLanguage().equals("%")) {
                                raw = raw + bullet + " " + node_opt.formLanguage() + next_line;
                            } else if (node_opt.getLanguage().substring(0, 1).equals("%")) {
                                raw = raw + (bullet + " " + node_opt.getLanguage().substring(1) + " - " + node_opt.formLanguage()) + next_line;
                            } else {
                                raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + node_opt.formLanguage()) + next_line;
                            }
                        }
                    }
                    //raw = raw + ("\n"+"\n" + bullet +" "+ node_opt.formLanguage());
                } else {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms") || (associatedTest.trim().equals("সংশ্লিষ্ট লক্ষণ"))
                            || associatedTest.trim().equals("जुड़े लक्षण") || associatedTest.trim().equals("સંકળાયેલ લક્ષણો") || (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")))) {
                        if (!generateAssociatedSymptomsOrHistory(node_opt).isEmpty()) {
                            raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                            raw = raw.substring(6);
                            Log.e("FinalText= ", raw);
                        } else {
                            Log.e("FinalText= ", raw);
                        }
                    }
                }
            }
        }

        String formatted;
        if (!raw.isEmpty()) {
            if (Character.toString(raw.charAt(0)).equals(",")) {
                formatted = raw.substring(2);
            } else {
                formatted = raw;
            }
            formatted = formatted.replaceAll("\\. -", ".");
            formatted = formatted.replaceAll("\\.,", ", ");
            Log.i(TAG, "generateLanguage: " + formatted);
            return formatted;
        }
        return null;
    }

    public String generateLanguage(String language) {

        String raw = "";
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected()) {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms") || associatedTest.trim().equals("जुड़े लक्षण") ||
                            (associatedTest.trim().equals("H/o specific illness")) ||
                            (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("ಸಂಯೋಜಿತ ಲಕ್ಷಣಗಳು")) ||
                            (associatedTest.trim().equals("સંકળાયેલ લક્ષણો")) ||
                            (associatedTest.trim().equals("এইচ/অ নিৰ্দিষ্ট ৰোগ")) ||
                            (associatedTest.trim().equals("संबंधित लक्षणे"))
                            || (associatedTest.trim().equals("সংশ্লিষ্ট লক্ষণ")))) {

                        if ((associatedTest.trim().equals("Associated symptoms")) || associatedTest.trim().equals("जुड़े लक्षण") || (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("ಸಂಯೋಜಿತ ಲಕ್ಷಣಗಳು")) ||
                                (associatedTest.trim().equals("સંકળાયેલ લક્ષણો")) ||
                                (associatedTest.trim().equals("संबंधित लक्षणे"))
                                || (associatedTest.trim().equals("সংশ্লিষ্ট লক্ষণ"))) {
                            if (!generateAssociatedSymptomsOrHistory(node_opt).isEmpty()) {
                                raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                                raw = raw.substring(6);
                                Log.e("FinalText= ", raw);
                            } else {
                                Log.e("FinalText= ", raw);

                            }
                        } else {
                            raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                        }

                    } else {
                        if (!node_opt.getLanguage().isEmpty()) {
                            if (node_opt.getLanguage().equals("%")) {
                                raw = raw + bullet + " " + node_opt.formLanguage(language) + next_line;
                            } else if (node_opt.getLanguage().substring(0, 1).equals("%")) {
                                raw = raw + (bullet + " " + node_opt.getLanguage().substring(1) + " - " +
                                        node_opt.formLanguage(language)) + next_line;
                            } else {
                                if (language.equalsIgnoreCase("hi")) {
                                    raw = raw + (bullet + " " + node_opt.getDisplay_hindi() + " - " + node_opt.formLanguage(language)) + next_line;
                                } else if (language.equalsIgnoreCase("or")) {
                                    raw = raw + (bullet + " " + node_opt.getDisplay_oriya() + " - " + node_opt.formLanguage(language)) + next_line;
                                } else if (language.equalsIgnoreCase("gu")) {
                                    raw = raw + (bullet + " " + node_opt.getDisplay_gujarati() + " - " + node_opt.formLanguage(language)) + next_line;
                                } else if (language.equalsIgnoreCase("as")) {
                                    raw = raw + (bullet + " " + node_opt.getDisplay_assamese() + " - " + node_opt.formLanguage(language)) + next_line;
                                } else if (language.equalsIgnoreCase("bn")) {
                                    raw = raw + (bullet + " " + node_opt.getDisplay_bengali() + " - " + node_opt.formLanguage(language)) + next_line;
                                } else if (language.equalsIgnoreCase("kn")) {
                                    raw = raw + (bullet + " " + node_opt.getDisplay_kannada() + " - " + node_opt.formLanguage(language)) + next_line;
                                } else {
                                    raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + node_opt.formLanguage(language)) + next_line;
                                }
                            }
                            Log.e("FinalText= ", raw);

                        }
                    }
                    //raw = raw + ("\n"+"\n" + bullet +" "+ node_opt.formLanguage());
                } else {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms")
                            || associatedTest.trim().equals("जुड़े लक्षण") || (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("ಸಂಯೋಜಿತ ಲಕ್ಷಣಗಳು")) ||
                            (associatedTest.trim().equals("સંકળાયેલ લક્ષણો")) ||
                            (associatedTest.trim().equals("संबंधित लक्षणे")) ||
                            (associatedTest.trim().equals("সংশ্লিষ্ট লক্ষণ")))) {
                        if (!generateAssociatedSymptomsOrHistory(node_opt).isEmpty()) {
                            raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                            raw = raw.substring(6);
                            Log.e("FinalText= ", raw);
                        } else {
                            Log.e("FinalText= ", raw);
                        }
                    }
                }
            }
        }

        String formatted;
        if (!raw.isEmpty()) {
            if (Character.toString(raw.charAt(0)).equals(",")) {
                formatted = raw.substring(2);
            } else {
                formatted = raw;
            }
            formatted = formatted.replaceAll("\\. -", ".");
            formatted = formatted.replaceAll("\\.,", ", ");
            Log.i(TAG, "generateLanguage: " + formatted);
            return formatted;
        }
        return null;
    }

    public String formLanguage(String language) {
        List<String> stringsList = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean isTerminal = false;
        if (mOptions != null && !mOptions.isEmpty()) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).isSelected()) {
                    String test = mOptions.get(i).getLanguage();

                    if (language.equalsIgnoreCase("hi")) {
                        test = mOptions.get(i).getDisplay_hindi();
                    } else if (language.equalsIgnoreCase("or")) {
                        test = mOptions.get(i).getDisplay_oriya();
                    } else if (language.equalsIgnoreCase("gu")) {
                        test = mOptions.get(i).getDisplay_gujarati();
                    } else if (language.equalsIgnoreCase("as")) {
                        test = mOptions.get(i).getDisplay_assamese();
                    }else if (language.equalsIgnoreCase("bn")) {
                        test = mOptions.get(i).getDisplay_bengali();
                    }else if (language.equalsIgnoreCase("kn")) {
                        test = mOptions.get(i).getDisplay_kannada();
                    }

                    if (!test.isEmpty()) {
                        if (test.equals("%")) {
                        } else if (test.substring(0, 1).equals("%")) {
                            stringsList.add(test.substring(1));
                        } else {
                            // stringsList.add(test);
                            if (mOptions.get(i).getText() != null && mOptions.get(i).getText().replaceAll("\\s", "")
                                    .equalsIgnoreCase(mOptions.get(i).getLanguage().replaceAll("\\s", ""))) {

                                if (mOptions.get(i).getInputType().equalsIgnoreCase("")) {
                                    //This means chip is selected as answer...
                                    if (language.equalsIgnoreCase("hi")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("or")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("gu")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("as")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("bn")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("kn")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else {
                                        stringsList.add(mOptions.get(i).findDisplay());
                                    }
                                } else {
                                    stringsList.add(mOptions.get(i).getLanguage());
                                    //input's other than Text as for text input: text and language both are same.
                                }
                            } else {
                                if (mOptions.get(i).getInputType() != null && mOptions.get(i).getInputType().equalsIgnoreCase("text")) {
                                    if (language.equalsIgnoreCase("hi") && !mOptions.get(i).getDisplay_hindi().startsWith("[")) {
                                        stringsList.add(mOptions.get(i).getDisplay_hindi());
                                    } else if (language.equalsIgnoreCase("or") && !mOptions.get(i).getDisplay_oriya().startsWith("[")) {
                                        stringsList.add(mOptions.get(i).getDisplay_oriya());
                                    } else if (language.equalsIgnoreCase("gu") && !mOptions.get(i).getDisplay_gujarati().startsWith("[")) {
                                        stringsList.add(mOptions.get(i).getDisplay_gujarati());
                                    } else if (language.equalsIgnoreCase("as") && !mOptions.get(i).getDisplay_assamese().startsWith("[")) {
                                        stringsList.add(mOptions.get(i).getDisplay_assamese());
                                    } else if (language.equalsIgnoreCase("bn") && !mOptions.get(i).getDisplay_bengali().startsWith("[")) {
                                        stringsList.add(mOptions.get(i).getDisplay_bengali());
                                    } else if (language.equalsIgnoreCase("kn") && !mOptions.get(i).getDisplay_kannada().startsWith("[")) {
                                        stringsList.add(mOptions.get(i).getDisplay_kannada());
                                    } else {
                                        stringsList.add(mOptions.get(i).getLanguage());
                                    }
                                } else {
                                    stringsList.add(mOptions.get(i).findDisplay(language));
                                }

                            }

                        }
                    }

                    if (!mOptions.get(i).isTerminal()) {
                        stringsList.add(mOptions.get(i).formLanguageHindi(language));
                        isTerminal = false;
                    } else {
                        isTerminal = true;
                    }
                }
            }
        }

        String languageSeparator;
        if (isTerminal) {
            languageSeparator = ", ";
        } else {
            languageSeparator = " - ";
        }
        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {
            if (i == 0) {

                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(stringsList.get(i));
                    }
                }
            } else {
                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
                    }
                }
            }
        }

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "सवाल का जवाब नहीं दिया");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "ପ୍ରଶ୍ନର ଉତ୍ତର ନାହିଁ |");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "પ્રશ્નનો જવાબ મળ્યો નથી");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "প্ৰশ্নৰ উত্তৰ নাই");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "প্রশ্নের উত্তর দেওয়া হয়নি");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "ಪ್ರಶ್ನೆಗೆ ಉತ್ತರವಿಲ್ಲ");
        }
        return mLanguage;
    }

    public String formLanguageHindi(String language) {
        List<String> stringsList = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean isTerminal = false;
        if (mOptions != null && !mOptions.isEmpty()) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).isSelected()) {
                    String test = mOptions.get(i).getLanguage();
                    if (!test.isEmpty()) {
                        if (test.equals("%")) {
                        } else if (test.substring(0, 1).equals("%")) {
                            stringsList.add(test.substring(1));
                        } else {
                            // stringsList.add(test);
                            if (mOptions.get(i).getText() != null && mOptions.get(i).getText().replaceAll("\\s", "")
                                    .equalsIgnoreCase(mOptions.get(i).getLanguage().replaceAll("\\s", ""))) {
                                if (mOptions.get(i).getInputType().equalsIgnoreCase("")) {
                                    //This means chip is selected as answer...
                                    // stringsList.add(mOptions.get(i).findDisplay()); //Chip UI
                                    if (language.equalsIgnoreCase("hi")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("or")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("gu")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("as")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("bn")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("kn")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else {
                                        stringsList.add(mOptions.get(i).findDisplay());
                                    }
                                } else {
                                    stringsList.add(mOptions.get(i).getLanguage());
                                    //input's other than Text as for text input: text and language both are same.
                                }
                            } else {
                                if (mOptions.get(i).getInputType() != null && mOptions.get(i).getInputType().equalsIgnoreCase("text")) {
                                    stringsList.add(mOptions.get(i).getLanguage());
                                } else {
                                    // stringsList.add(mOptions.get(i).findDisplay()); //here be hindi case handled....
                                    if (language.equalsIgnoreCase("hi")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("or")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("gu")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("as")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("bn")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else if (language.equalsIgnoreCase("kn")) {
                                        stringsList.add(mOptions.get(i).findDisplay(language)); //Chip UI
                                    } else {
                                        stringsList.add(mOptions.get(i).findDisplay());
                                    }
                                }

                            }

                        }
                    }

                    if (!mOptions.get(i).isTerminal()) {
                        stringsList.add(mOptions.get(i).formLanguageHindi());
                        isTerminal = false;
                    } else {
                        isTerminal = true;
                    }
                }
            }
        }

        String languageSeparator;
        if (isTerminal) {
            languageSeparator = ", ";
        } else {
            languageSeparator = " - ";
        }
        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {
            if (i == 0) {

                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(stringsList.get(i));
                    }
                }
            } else {
                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
                    }
                }
            }
        }
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "सवाल का जवाब नहीं दिया");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "ପ୍ରଶ୍ନର ଉତ୍ତର ନାହିଁ |");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "પ્રશ્નનો જવાબ મળ્યો નથી");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "প্ৰশ্নৰ উত্তৰ নাই");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "প্রশ্নের উত্তর দেওয়া হয়নি");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "ಪ್ರಶ್ನೆಗೆ ಉತ್ತರವಿಲ್ಲ");
        }
        return mLanguage;
    }

    public String findDisplay(String language) {
        switch (language) {
            case "en": {

                if (display != null && display.isEmpty()) {
                    return text;
                } else {
                    return display;
                }

            }

            case "or": {
                //Log.i(TAG, "findDisplay: ori");
                if (display_oriya != null && !display_oriya.isEmpty()) {
                    //Log.i(TAG, "findDisplay: ori dis");
                    return display_oriya;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "gu": {
                //Log.i(TAG, "findDisplay: ori");
                if (display_gujarati != null && !display_gujarati.isEmpty()) {
                    //Log.i(TAG, "findDisplay: ori dis");
                    return display_gujarati;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }

            }

            case "hi": {
                //Log.i(TAG, "findDisplay: cb");
                if (display_hindi != null && !display_hindi.isEmpty()) {
                    //Log.i(TAG, "findDisplay: cb ");
                    return display_hindi;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "bn": {
                if (display_bengali != null && !display_bengali.isEmpty()) {
                    return display_bengali;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }

            case "kn": {
                if (display_kannada != null && !display_kannada.isEmpty()) {
                    return display_kannada;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }
            case "as": {
                //Log.i(TAG, "findDisplay: cb");
                if (display_assamese != null && !display_assamese.isEmpty()) {
                    //Log.i(TAG, "findDisplay: cb ");
                    return display_assamese;
                } else {
                    if (display == null || display.isEmpty()) {
                        //Log.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //Log.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }

            default: {
                {
                    if (display != null && display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }
        }
    }

    public String formLanguageHindi() {
        List<String> stringsList = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean isTerminal = false;
        if (mOptions != null && !mOptions.isEmpty()) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).isSelected()) {
                    String test = mOptions.get(i).getLanguage();
                    if (!test.isEmpty()) {
                        if (test.equals("%")) {
                        } else if (test.substring(0, 1).equals("%")) {
                            stringsList.add(test.substring(1));
                        } else {
                            // stringsList.add(test);
                            if (mOptions.get(i).getText() != null && mOptions.get(i).getText().replaceAll("\\s", "")
                                    .equalsIgnoreCase(mOptions.get(i).getLanguage().replaceAll("\\s", ""))) {
                                if (mOptions.get(i).getInputType().equalsIgnoreCase("")) {
                                    //This means chip is selected as answer...
                                    stringsList.add(mOptions.get(i).findDisplay()); //Chip UI
                                } else {
                                    stringsList.add(mOptions.get(i).getLanguage());
                                    //input's other than Text as for text input: text and language both are same.
                                }
                            } else {
                                if (mOptions.get(i).getInputType() != null && mOptions.get(i).getInputType().equalsIgnoreCase("text")) {
                                    stringsList.add(mOptions.get(i).getLanguage());
                                } else {
                                    stringsList.add(mOptions.get(i).findDisplay()); //here be hindi case handled....
                                }

                            }

                        }
                    }

                    if (!mOptions.get(i).isTerminal()) {
                        stringsList.add(mOptions.get(i).formLanguageHindi());
                        isTerminal = false;
                    } else {
                        isTerminal = true;
                    }
                }
            }
        }

        String languageSeparator;
        if (isTerminal) {
            languageSeparator = ", ";
        } else {
            languageSeparator = " - ";
        }
        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {
            if (i == 0) {

                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(stringsList.get(i));
                    }
                }
            } else {
                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
                    }
                }
            }
        }
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "सवाल का जवाब नहीं दिया");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "ପ୍ରଶ୍ନର ଉତ୍ତର ନାହିଁ |");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "પ્રશ્નનો જવાબ મળ્યો નથી");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "প্ৰশ্নৰ উত্তৰ নাই");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "প্রশ্নের উত্তর দেওয়া হয়নি");
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            mLanguage = mLanguage.replaceAll("Question not answered", "ಪ್ರಶ್ನೆಗೆ ಉತ್ತರವಿಲ್ಲ");
        }
        return mLanguage;
    }

    //TODO: Check this, as associated complaints are not being triggered.
    public ArrayList<String> getSelectedAssociations() {
        ArrayList<String> selectedAssociations = new ArrayList<>();
        List<Node> mOptions = optionsList;
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected() & mOptions.get(i).hasAssociations()) {
                selectedAssociations.add(mOptions.get(i).getAssociatedComplaint());
                if (!mOptions.get(i).isTerminal()) {
                    selectedAssociations.addAll(mOptions.get(i).getSelectedAssociations());
                }
            }
        }
        return selectedAssociations;
    }

    public ArrayList<String> getPhysicalExamList() {
        ArrayList<String> selectedExams = new ArrayList<>();
        List<Node> mOptions = optionsList;
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected() & mOptions.get(i).isHasPhysicalExams()) {
                String rawExams = mOptions.get(i).getPhysicalExams();
                if (!rawExams.trim().isEmpty()) {
                    String[] splitExams = rawExams.split(";");
                    selectedExams.addAll(Arrays.asList(splitExams));
                }
                if (!mOptions.get(i).isTerminal()) {
                    selectedExams.addAll(mOptions.get(i).getPhysicalExamList());
                }
            }
        }
        return selectedExams;
    }

    public static final int TAKE_IMAGE_FOR_NODE = 507;
    public static final String TAG = Node.class.getSimpleName();

    public static void askDate(final Node node, final Activity context, final QuestionsAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                        String dateString = simpleDateFormat.format(date);
                        if (!dateString.equalsIgnoreCase("")) {
                            if (node.getLanguage().contains("_")) {
                                node.setLanguage(node.getLanguage().replace("_", dateString));
                            } else {
                                node.addLanguage(dateString);
                                //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                            }
                            node.setSelected(true);
                        } else {
                            if (node.isRequired()) {
                                node.setSelected(false);
                            } else {
                                node.setSelected(true);
                                if (node.getLanguage().contains("_")) {
                                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                                } else {
                                    node.addLanguage("Question not answered");
                                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                } else {
                    node.addLanguage("Question not answered");
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }

//                node.setSelected(false);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
            }
        });
        datePickerDialog.setTitle(R.string.question_date_picker);
        //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
        /*if (node.validation.equals("MAX_TODAY")) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        }*/
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datePickerDialog.setCanceledOnTouchOutside(false);
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    public void generatePopUp(final Activity context) {

        HashSet<String> messages = new HashSet<String>();
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected() && node_opt.hasPopUp) {
                    messages.add(node_opt.pop_up);
                }
            }
        }

        String finalMessage = "";
        Iterator<String> i = messages.iterator();
        while (i.hasNext()) {
            finalMessage = i.next() + "\n";
        }

        if (!finalMessage.isEmpty()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
            alertDialogBuilder.setMessage(finalMessage);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);
        }
    }

    public static void handleQuestion(Node questionNode, final Activity context, final QuestionsAdapter adapter,
                                      final String imagePath, final String imageName) {
        String type = questionNode.getInputType();
        switch (type) {
            case "text":
                askText(questionNode, context, adapter);
                break;
            case "bloodpressure":
                askBP(questionNode, context, adapter);
                break;
            case "date":
                askDate(questionNode, context, adapter);
                break;
            case "location":
                askLocation(questionNode, context, adapter);
                break;
            case "number":
                askNumber(questionNode, context, adapter);
                break;
            case "area":
                askArea(questionNode, context, adapter);
                break;
            case "duration":
                askDuration(questionNode, context, adapter);
                break;
            case "timeduration":
                askTimeDuration(questionNode, context, adapter);
                break;
            case "range":
                askRange(questionNode, context, adapter);
                break;
            case "frequency":
                askFrequency(questionNode, context, adapter);
                break;
            case "camera":
                openCamera(context, imagePath, imageName);
                break;
        }
    }

    public static void openCamera(Activity activity, String imagePath, String imageName) {
        Log.d(TAG, "open Camera!");
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
            }
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        activity.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

    public static void askNumber(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder numberDialog = new MaterialAlertDialogBuilder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
       /* final NumberPicker numberPicker = convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(1000);*/
        EditText et_enter_value = convertView.findViewById(R.id.et_enter_value);
        et_enter_value.setFilters(new InputFilter[]{new InputFilterMinMax("1", "1000")});
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               /* numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());*/
                String value = et_enter_value.getText().toString();

                if (!et_enter_value.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", et_enter_value.getText().toString()));
                    } else {
                        node.addLanguage(et_enter_value.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    node.setSelected(false);
                    //} else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                    } else {
                        node.addLanguage("Question not answered");
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }
                // node.setSelected(true);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!et_enter_value.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", et_enter_value.getText().toString()));
                    } else {
                        node.addLanguage(et_enter_value.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                } else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                    } else {
                        node.addLanguage("Question not answered");
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }
                node.setSelected(false);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = numberDialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askArea(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder areaDialog = new MaterialAlertDialogBuilder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText("X");

        widthPicker.setMinValue(0);
        widthPicker.setMaxValue(100);
        lengthPicker.setMinValue(0);
        lengthPicker.setMaxValue(100);

        areaDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                widthPicker.setValue(widthPicker.getValue());
                lengthPicker.setValue(lengthPicker.getValue());
                String durationString = widthPicker.getValue() + " X " + lengthPicker.getValue();


                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
        areaDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = areaDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askRange(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder rangeDialog = new MaterialAlertDialogBuilder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText(" - ");

        startPicker.setMinValue(0);
        startPicker.setMaxValue(100);
        endPicker.setMinValue(0);
        endPicker.setMaxValue(100);
        rangeDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPicker.setValue(startPicker.getValue());
                endPicker.setValue(endPicker.getValue());
                //String durationString = startPicker.getValue() + " to " + endPicker.getValue();
                String durationString = startPicker.getValue() + " - " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        rangeDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = rangeDialog.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void askLocation(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder locationDialog = new MaterialAlertDialogBuilder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub


    }

    public static void askFrequency(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder frequencyDialog = new MaterialAlertDialogBuilder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        // final String[] units = new String[]{"per Hour", "per Day", "Per Week", "per Month", "per Year"};
        final String[] units = new String[]{context.getString(R.string.per_Hour),
                context.getString(R.string.per_Day), context.getString(R.string.per_Week),
                context.getString(R.string.per_Month), context.getString(R.string.per_Year)};

        final String[] doctorUnits = new String[]{"times per hour", "time per day", "times per week", "times per month", "times per year"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = quantityPicker.getValue() + " " + doctorUnits[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        frequencyDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = frequencyDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askDuration(final Node node, Activity context, final QuestionsAdapter adapter) {
        final MaterialAlertDialogBuilder durationDialog = new MaterialAlertDialogBuilder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{
                context.getString(R.string.Hours), context.getString(R.string.Days),
                context.getString(R.string.Weeks), context.getString(R.string.Months),
                context.getString(R.string.Years)}; //supports Hindi Translations as well...

        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);

        EditText input = findInput(quantityPicker);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().length() != 0) {
                    Integer value = Integer.parseInt(editable.toString());
                    if (value >= quantityPicker.getMinValue())
                        quantityPicker.setValue(value);
                }
            }
        };

        input.addTextChangedListener(textWatcher);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());

                //translate back to English from Hindi if present...
                String unit_text = "";
                unit_text = hi_en(units[unitPicker.getValue()]);//for Hindi
                unit_text = or_en(units[unitPicker.getValue()]);//for Odiya
                unit_text = gu_en(units[unitPicker.getValue()]);//for Gujrati
                unit_text = as_en(units[unitPicker.getValue()]);//for Assamese

                String durationString = quantityPicker.getValue() + " " + unit_text;

                if (quantityPicker.getValue() != '0' || !durationString.equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    if (node.isRequired()) {
                        node.setSelected(false);
                    } else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    }
                }
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);

                /*if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                } else {
                    node.addLanguage("Question not answered");
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }*/
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void askTimeDuration(final Node node, Activity context, final QuestionsAdapter adapter) {
        final MaterialAlertDialogBuilder durationDialog = new MaterialAlertDialogBuilder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{
                context.getString(R.string.Minute)}; //supports Hindi Translations as well...

        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(60);
        unitPicker.setMinValue(0);
        //unitPicker.setMaxValue(4);

        EditText input = findInput(quantityPicker);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().length() != 0) {
                    Integer value = Integer.parseInt(editable.toString());
                    if (value >= quantityPicker.getMinValue())
                        quantityPicker.setValue(value);
                }
            }
        };

        input.addTextChangedListener(textWatcher);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());

                //translate back to English from Hindi if present...
                String unit_text = "";
                unit_text = hi_en(units[unitPicker.getValue()]);//for Hindi
                unit_text = or_en(units[unitPicker.getValue()]);//for Odiya
                unit_text = gu_en(units[unitPicker.getValue()]);//for Gujrati
                unit_text = as_en(units[unitPicker.getValue()]);//for Assamese

                String durationString = quantityPicker.getValue() + " " + unit_text;

                if (quantityPicker.getValue() != '0' || !durationString.equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    if (node.isRequired()) {
                        node.setSelected(false);
                    } else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    }
                }
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);

                /*if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                } else {
                    node.addLanguage("Question not answered");
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }*/
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private static EditText findInput(ViewGroup np) {
        int count = np.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = np.getChildAt(i);
            if (child instanceof ViewGroup) {
                findInput((ViewGroup) child);
            } else if (child instanceof EditText) {
                return (EditText) child;
            }
        }
        return null;
    }

    private static String hi_en(String unit) {

        switch (unit) {
            case "मिनट":
                unit = "Minutes";
                break;

            case "घंटे":
                unit = "Hours";
                break;

            case "दिन":
                unit = "Days";
                break;

            case "हफ्तों":
                unit = "Weeks";
                break;

            case "महीने":
                unit = "Months";
                break;

            case "वर्ष":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String or_en(String unit) {
        switch (unit) {
            case "ମିନିଟ୍ \\|":
                unit = "Minutes";
                break;

            case "ଘଣ୍ଟା":
                unit = "Hours";
                break;

            case "ଦିନଗୁଡିକ":
                unit = "Days";
                break;

            case "ସପ୍ତାହଗୁଡିକ":
                unit = "Weeks";
                break;

            case "ମାସଗୁଡିକ":
                unit = "Months";
                break;

            case "ବର୍ଷଗୁଡିକ":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String gu_en(String unit) {
        switch (unit) {

            case "મિનિટ":
                unit = "Minutes";
                break;

            case "કલાકો":
                unit = "Hours";
                break;

            case "દિવસ":
                unit = "Days";
                break;

            case "અઠવાડિયા":
                unit = "Weeks";
                break;

            case "મહિનાઓ":
                unit = "Months";
                break;

            case "વર્ષ":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String as_en(String unit) {
        switch (unit) {

            case "মিনিটবোৰ":
                unit = "Minutes";
                break;

            case "ঘণ্টা":
                unit = "Hours";
                break;

            case "দিনবোৰ":
                unit = "Days";
                break;

            case "সপ্তাহ":
                unit = "Weeks";
                break;

            case "মাহবোৰ":
                unit = "Months";
                break;

            case "বছৰবোৰ":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    public static void subAskText(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialogEditText.getText().toString().trim().isEmpty()) {
                    node.setSelected(false);
                } else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                    } else {
                        node.addLanguage(dialogEditText.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                }

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);
                adapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void subAskDate(final Node node, final Activity context, final CustomArrayAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                        String dateString = simpleDateFormat.format(date);
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", dateString));
                        } else {
                            node.addLanguage(" " + dateString);
                            node.setText(node.getLanguage());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                node.setSelected(false);

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                } else {
                    node.addLanguage("Question not answered");
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                adapter.notifyDataSetChanged();

            }
        });
        datePickerDialog.setTitle(R.string.question_date_picker);
        /*if (node.validation.equals("MAX_TODAY")) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        }*/
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public static void subAskNumber(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder numberDialog = new MaterialAlertDialogBuilder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
      /*  final NumberPicker numberPicker = convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(1000);*/
        EditText et_enter_value = convertView.findViewById(R.id.et_enter_value);
        et_enter_value.setFilters(new InputFilter[]{new InputFilterMinMax("1", "1000")});
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //numberPicker.setValue(numberPicker.getValue());
                // String value = String.valueOf(numberPicker.getValue());
                String value = et_enter_value.getText().toString();
                if (value.trim().isEmpty()) {
                    node.setSelected(false);
                } else {

                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", value));
                    } else {
                        node.addLanguage(" " + value);
                        node.setText(value);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                }

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = numberDialog.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }


    public static void subHandleQuestion(Node questionNode, final Activity context, final CustomArrayAdapter adapter, final String imagePath, final String imageName) {
        String type = questionNode.getInputType();
        Log.d(TAG, "subQ " + type);
        switch (type) {
            case "text":
                subAskText(questionNode, context, adapter);
                break;
            case "date":
                subAskDate(questionNode, context, adapter);
                break;
            case "location":
                subAskLocation(questionNode, context, adapter);
                break;
            case "number":
                subAskNumber(questionNode, context, adapter);
                break;
            case "area":
                subAskArea(questionNode, context, adapter);
                break;
            case "duration":
                subAskDuration(questionNode, context, adapter);
                break;
            case "timeduration":
                subAskTimeDuration(questionNode, context, adapter);
                break;
            case "range":
                subAskRange(questionNode, context, adapter);
                break;
            case "frequency":
                subAskFrequency(questionNode, context, adapter);
                break;
            case "camera":
                openCamera(context, imagePath, imageName);
                break;
        }
    }

    public static void subAskArea(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder areaDialog = new MaterialAlertDialogBuilder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText("X");

        widthPicker.setMinValue(0);
        widthPicker.setMaxValue(100);
        lengthPicker.setMinValue(0);
        lengthPicker.setMaxValue(100);

        areaDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                widthPicker.setValue(widthPicker.getValue());
                lengthPicker.setValue(lengthPicker.getValue());
                String durationString = widthPicker.getValue() + " X " + lengthPicker.getValue();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        areaDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = areaDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void subAskRange(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder rangeDialog = new MaterialAlertDialogBuilder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText(" - ");

        startPicker.setMinValue(0);
        startPicker.setMaxValue(100);
        endPicker.setMinValue(0);
        endPicker.setMaxValue(100);
        rangeDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPicker.setValue(startPicker.getValue());
                endPicker.setValue(endPicker.getValue());
                //String durationString = startPicker.getValue() + " to " + endPicker.getValue();
                String durationString = startPicker.getValue() + " - " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        rangeDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = rangeDialog.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void subAskLocation(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder locationDialog = new MaterialAlertDialogBuilder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub
    }

    public static void subAskFrequency(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder frequencyDialog = new MaterialAlertDialogBuilder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        //  final String[] units = context.getResources().getStringArray(R.array.units);
        final String[] units = new String[]{context.getString(R.string.per_Hour),
                context.getString(R.string.per_Day), context.getString(R.string.per_Week),
                context.getString(R.string.per_Month), context.getString(R.string.per_Year)};

        final String[] doctorUnits = context.getResources().getStringArray(R.array.doctor_units);
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = quantityPicker.getValue() + " " + doctorUnits[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        frequencyDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = frequencyDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void subAskDuration(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final MaterialAlertDialogBuilder durationDialog = new MaterialAlertDialogBuilder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        // final String[] units = context.getResources().getStringArray(R.array.duration_units);
        final String[] units = new String[]{
                context.getString(R.string.Hours), context.getString(R.string.Days),
                context.getString(R.string.Weeks), context.getString(R.string.Months),
                context.getString(R.string.Years)}; //supports Hindi Translations as well...

        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);

        EditText input = findInput(quantityPicker);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().length() != 0) {
                    Integer value = Integer.parseInt(editable.toString());
                    if (value >= quantityPicker.getMinValue())
                        quantityPicker.setValue(value);
                }
            }
        };

        input.addTextChangedListener(textWatcher);

        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                //  String durationString = quantityPicker.getValue() + " " + units[unitPicker.getValue()];
                //translate back to English from Hindi if present...
                String unit_text = "";
                unit_text = hi_en(units[unitPicker.getValue()]); //for Hindi...
                unit_text = or_en(units[unitPicker.getValue()]);//for Odiya
                unit_text = gu_en(units[unitPicker.getValue()]);//for Gujrati

                String durationString = quantityPicker.getValue() + " " + unit_text;

                if (quantityPicker.getValue() != '0' || !durationString.equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    if (node.isRequired()) {
                        node.setSelected(false);
                    } else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    }
                }
                //adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);

               /* if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                } else {
                    node.addLanguage("Question not answered");
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }*/
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void subAskTimeDuration(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final MaterialAlertDialogBuilder durationDialog = new MaterialAlertDialogBuilder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        // final String[] units = context.getResources().getStringArray(R.array.duration_units);
        final String[] units = new String[]{
                context.getString(R.string.Minute)}; //supports Hindi Translations as well...

        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(60);
        unitPicker.setMinValue(0);
        //unitPicker.setMaxValue(4);

        EditText input = findInput(quantityPicker);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().length() != 0) {
                    Integer value = Integer.parseInt(editable.toString());
                    if (value >= quantityPicker.getMinValue())
                        quantityPicker.setValue(value);
                }
            }
        };

        input.addTextChangedListener(textWatcher);

        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                //  String durationString = quantityPicker.getValue() + " " + units[unitPicker.getValue()];
                //translate back to English from Hindi if present...
                String unit_text = "";
                unit_text = hi_en(units[unitPicker.getValue()]); //for Hindi...
                unit_text = or_en(units[unitPicker.getValue()]);//for Odiya
                unit_text = gu_en(units[unitPicker.getValue()]);//for Gujrati
                unit_text = as_en(units[unitPicker.getValue()]);//for Assamese

                String durationString = quantityPicker.getValue() + " " + unit_text;

                if (quantityPicker.getValue() != '0' || !durationString.equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    if (node.isRequired()) {
                        node.setSelected(false);
                    } else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    }
                }
                //adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setSelected(false);

               /* if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                } else {
                    node.addLanguage("Question not answered");
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }*/
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    /**
     * Takes a JSON Array from a knowledgeEngine and creates the sub-nodes to store within it.
     * This is how we handle recursive construction.
     * Nodes are stores within each other. This method is maintains good organizational structure, but makes it difficult to loop back to higher level nodes.
     * This is will be modified as the knowledge curating method is updated.
     * <p>
     * The current structure of the knowledge, and the way it is stored here, is as follows"
     * Node 1 {
     * Node 1.1 {
     * Node 1.1.1
     * Node 1.1.2
     * Node 1.1.3
     * }
     * }
     *
     * @param jsonArray JSON Array of JSON Objects, which are nodes in the knowledge
     * @return List of nodes generated based on input JSON Array
     */
    private List<Node> createOptions(JSONArray jsonArray) {
        List<Node> createdOptions = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject current = jsonArray.getJSONObject(i);
                createdOptions.add(i, new Node(current));
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //write your code here for removing an option item...

        return createdOptions;
    }

    /*
        Language needs to be built recursively for each first level question of a complaint.
        In this context, all the language must be built by searching a knowledgeEngine, and then looking at sub-nodes to determine which are selected.
        Once a terminal knowledgeEngine is found, then the "sentence" of the primary starting knowledgeEngine is complete.
        So for Question 1 of Complaint X, all of the nodes of Q1 are examined to see which are selected, and the selected branch's language attributes are merged.
        Once the Q1 sentence is saved, Q2 is now formed.
     */
    public String formLanguage() {
        List<String> stringsList = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean isTerminal = false;
        if (mOptions != null && !mOptions.isEmpty()) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).isSelected()) {
                    String test = mOptions.get(i).getLanguage();
                    if (!test.isEmpty()) {
                        if (test.equals("%")) {
                        } else if (test.substring(0, 1).equals("%")) {
                            stringsList.add(test.substring(1));
                        } else {
                            stringsList.add(test);
                        }
                    }

                    if (!mOptions.get(i).isTerminal()) {
                        stringsList.add(mOptions.get(i).formLanguage());
                        isTerminal = false;
                    } else {
                        isTerminal = true;
                    }
                }
            }
        }

        String languageSeparator;
        if (isTerminal) {
            languageSeparator = ", ";
        } else {
            languageSeparator = " - ";
        }
        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {
            if (i == 0) {

                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(stringsList.get(i));
                    }
                }
            } else {
                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
                    }
                }
            }
        }
        return mLanguage;
    }

    public AlertDialog displayImage(final Activity context, final String path, final String name) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addImageToList();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File temp = new File(imagePath);
                if (temp.exists()) temp.delete();
                imagePath = null;
                openCamera(context, path, name);
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            dialog.supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        } else {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        dialog.setView(dialogLayout);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screen_height = displayMetrics.heightPixels;
                int screen_width = displayMetrics.widthPixels;

                ImageView imageView = dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                Glide.with(context)
                        .load(new File(imagePath))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .listener(new RequestListener<File, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, File file, Target<GlideDrawable> target, boolean b) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable glideDrawable, File file, Target<GlideDrawable> target, boolean b, boolean b1) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .override(screen_width, screen_height)
                        .into(imageView);
            }
        });

        dialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
        return dialog;

    }



   /* public ArrayList<String> getPhysicalExams() {
        ArrayList<String> selectedAssociations = new ArrayList<>();
        List<Node> mOptions = optionsList;
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected() & mOptions.get(i).hasAssociations()) {
                selectedAssociations.add(mOptions.get(i).getAssociatedComplaint());
                if (!mOptions.get(i).isTerminal()) {
                    selectedAssociations.addAll(mOptions.get(i).getSelectedAssociations());
                }
            }
        }
        return selectedAssociations;
    }
    */

    public String getPhysicalExams() {
        return physicalExams;
    }

    public void removeOptionsList() {
        this.optionsList = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getDisplay_oriya() {
        return display_oriya;
    }

    public void setDisplay_oriya(String display_oriya) {
        this.display_oriya = display_oriya;
    }

    public String getDisplay_cebuno() {
        return display_cebuno;
    }

    public void setDisplay_cebuno(String display_cebuno) {
        this.display_cebuno = display_cebuno;
    }

    public String getDisplay_hindi() {
        return display_hindi;
    }
    public String getDisplay_bengali() {
        return display_bengali;
    }

    public String getDisplay_kannada() {
        return display_kannada;
    }

    public void setDisplay_hindi(String display_hindi) {
        this.display_hindi = display_hindi;
    }

    public void setDisplay_bengali(String display_bengali) {
        this.display_bengali = display_bengali;
    }

    public void setDisplay_kannada(String display_kannada) {
        this.display_kannada = display_kannada;
    }

    public String getDisplay_gujarati() {
        return display_gujarati;
    }

    public void setDisplay_gujarati(String display_gujarati) {
        this.display_gujarati = display_gujarati;
    }

    public String getDisplay_assamese() {
        return display_assamese;
    }

    public void setDisplay_assamese(String display_assamese) {
        this.display_assamese = display_assamese;
    }

    public void setOptionsList(List<Node> optionsList) {
        this.optionsList = optionsList;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMin_age() {
        return min_age;
    }

    public void setMin_age(String min_age) {
        this.min_age = min_age;
    }

    public String getMax_age() {
        return max_age;
    }

    public void setMax_age(String max_age) {
        this.max_age = max_age;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getInputType() {
        return inputType;
    }

    public void setComplaint(boolean complaint) {
        this.complaint = complaint;
    }

    public String getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(String choiceType) {
        this.choiceType = choiceType;
    }

    public boolean isRootNode() {
        return rootNode;
    }

    public void setRootNode(boolean rootNode) {
        this.rootNode = rootNode;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getImagePathList() {
        return imagePathList;
    }

    public void setImagePathList(List<String> imagePathList) {
        this.imagePathList = imagePathList;
    }

    public void addImageToList() {
        if (imagePathList == null) {
            imagePathList = new ArrayList<>();
        }
        if (imagePath != null && !imagePath.isEmpty()) {
            imagePathList.add(imagePath);

        }
    }

    public boolean isMultiChoice() {
        return isMultiChoice;
    }

    public void setMultiChoice(boolean multiChoice) {
        isMultiChoice = multiChoice;
    }

    public boolean isExcludedFromMultiChoice() {
        return isExcludedFromMultiChoice;
    }

    public void setExcludedFromMultiChoice(boolean excludedFromMultiChoice) {
        isExcludedFromMultiChoice = excludedFromMultiChoice;
    }

    private String generateAssociatedSymptomsOrHistory(Node associatedSymptomNode) {

        List<String> positiveAssociations = new ArrayList<>();
        List<String> negativeAssociations = new ArrayList<>();
        List<String> finalTexts = new ArrayList<>();
        List<Node> mOptions = associatedSymptomNode.getOptionsList();
        boolean flagPositive = false;
        boolean flagNegative = false;
//        String mLanguagePositive = associatedSymptomNode.positiveCondition;
        String mLanguagePositive = "Patient reports -" + next_line;
//        String mLanguageNegative = associatedSymptomNode.negativeCondition;
        String mLanguageNegative = "Patient denies -" + next_line;

        Log.i(TAG, "generateAssociatedSymptomsOrHistory: " + mLanguagePositive);
        Log.i(TAG, "generateAssociatedSymptomsOrHistory: " + mLanguageNegative);


        for (int i = 0; i < mOptions.size(); i++) {


            if (mOptions.get(i).isSelected()) {
                if (!mOptions.get(i).getLanguage().isEmpty()) {
                    if (mOptions.get(i).getLanguage().equals("%")) {
                    } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
                        positiveAssociations.add(mOptions.get(i).getLanguage().substring(1));
                    } else if (mOptions.get(i).getLanguage().isEmpty()) {
                        positiveAssociations.add(mOptions.get(i).getText());
                    } else {
                        positiveAssociations.add(mOptions.get(i).getLanguage());
                    }
                }
                if (!mOptions.get(i).isTerminal()) {
                    if (positiveAssociations.size() > 0) {
                        String tempString = positiveAssociations.get(positiveAssociations.size() - 1) + " - " +
                                mOptions.get(i).formLanguage();

                        positiveAssociations.set(positiveAssociations.size() - 1, tempString);
                    }
                }

            } else if (mOptions.get(i).isNoSelected()) {
                if (!mOptions.get(i).getLanguage().isEmpty()) {
                    if (mOptions.get(i).getLanguage().equals("%")) {
                    } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
                        negativeAssociations.add(mOptions.get(i).getLanguage().substring(1));
                    } else if (mOptions.get(i).getLanguage().isEmpty()) {
                        negativeAssociations.add(mOptions.get(i).getText());
                    } else {
                        negativeAssociations.add(mOptions.get(i).getLanguage());
                    }
                }
            }


//            else {
//                if (mOptions.get(i).getLanguage().equals("%")) {
//                } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
//                    negativeAssociations.add(mOptions.get(i).getLanguage().substring(1));
//                } else if (mOptions.get(i).getLanguage().isEmpty()) {
//                    negativeAssociations.add(mOptions.get(i).getText());
//                } else {
//                    negativeAssociations.add(mOptions.get(i).getLanguage());
//                }
//            }
        }

        if (positiveAssociations != null && !positiveAssociations.isEmpty()) {
            finalTexts.add(bullet_hollow);
            for (String string : positiveAssociations) {
                Log.i(TAG, "generateAssociatedSymptomsOrHistory:  " + mLanguagePositive);
                if (!flagPositive) {
                    flagPositive = true;
                    finalTexts.add(mLanguagePositive + " " + string);
                } else {
                    finalTexts.add(" " + string);
                }
            }
        }


        if (negativeAssociations != null && !negativeAssociations.isEmpty()) {
            finalTexts.add(bullet_hollow);
            for (String string : negativeAssociations) {
                Log.i(TAG, "generateAssociatedSymptomsOrHistory:  " + mLanguageNegative);
                if (!flagNegative) {
                    flagNegative = true;
                    finalTexts.add(mLanguageNegative + " " + string);
                } else {
                    finalTexts.add(" " + string);
                }
            }
        }

        String final_language = "";

        if (!finalTexts.isEmpty()) {
            for (int l = 0; l < finalTexts.size(); l++) {
                final_language = final_language + ", " + finalTexts.get(l);
            }
        }

        final_language = final_language.replaceAll("- ,", "- ");
        final_language = final_language.replaceAll("of,", "of");
        final_language = final_language.replaceAll("\\, \\[", " [");
        final_language = final_language.replaceAll(", " + bullet_hollow + ", ", " " + next_line + bullet_hollow + " ");
        Log.i(TAG, "generateAssociatedSymptomsOrHistory: " + final_language);

        return final_language;

    }

    public String formQuestionAnswer(int level) {
        List<String> stringsList = new ArrayList<>();
        List<String> stringsListNoSelected = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean flag = false;
        boolean isAssociatedSymEmpty = false;

        for (int i = 0; i < mOptions.size(); i++) {
            //isSelected set from  thisNode.setUnselected(); method
            if (mOptions.get(i).isSelected()) {
                String question;
                if (level == 0) {
                    question = big_bullet + " " + mOptions.get(i).findDisplay();
                    if ((mOptions.get(i).getText().equalsIgnoreCase("Associated symptoms"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો")) ||
                            (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("সংশ্লিষ্ট লক্ষণ"))) {
                        question = question + next_line + "Patient reports -";
                    }
                } else {
                    question = bullet + " " + mOptions.get(i).findDisplay();
                }
                String answer = mOptions.get(i).getLanguage();
                if (mOptions.get(i).isTerminal()) {
                    if (mOptions.get(i).getInputType() != null && !mOptions.get(i).getInputType().trim().isEmpty()) {
                        Log.i(TAG, "ipt: " + mOptions.get(i).getInputType());
                        if (mOptions.get(i).getInputType().equals("camera")) {
                        } else {
                            if (!answer.isEmpty()) {
                                if (answer.equals("%")) {
                                } else if (mOptions.get(i).getText().equals(mOptions.get(i).getLanguage())) {
                                    stringsList.add(bullet_hollow + answer + next_line);
                                } else if (answer.substring(0, 1).equals("%")) {
                                    stringsList.add(bullet_hollow + answer.substring(1) + next_line);
                                } else {
                                    stringsList.add(bullet_hollow + answer + next_line);
                                }
                            }
                        }
                    } else {
                        stringsList.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                    }
                } else {
                    stringsList.add(question + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1));
                }
            } else if (mOptions.get(i).getText() != null &&
                    ((mOptions.get(i).getText().equalsIgnoreCase("Associated symptoms"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("সংশ্লিষ্ট লক্ষণ")))) {

                if (!mOptions.get(i).isTerminal()) {
                    stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1));
                }

                //                if (mOptions.get(i).getOptionsList().size() > 0) {
                //
                //                    for (int j = 0; j < mOptions.get(i).getOptionsList().size(); j++) {
                //
                //                        if (mOptions.get(i).getOptionsList().get(j).isSelected()
                //                                || mOptions.get(i).getOptionsList().get(j).isNoSelected()) {
                //
                //                            if (!mOptions.get(i).isTerminal()) {
                //                                stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                //                                stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1));
                //                            }
                //                        }
                //                    }
                //                }
            } else {
                //in case of weird null exception...
            }

            // to add Patient denies entry
            if (mOptions.get(i).isNoSelected()) {
                if (!flag) {
                    flag = true;
                    stringsListNoSelected.add("Patient denies -" + next_line);
                }
                stringsListNoSelected.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                Log.e("List", "" + stringsListNoSelected);
            }
        }

        if (stringsListNoSelected.size() > 0) {
            stringsList.addAll(stringsListNoSelected);
        }

        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {

            if (!stringsList.get(i).isEmpty()) {
                mLanguage = mLanguage.concat(stringsList.get(i));
            }

        }
        Log.i(TAG, "formQuestionAnswer: " + mLanguage);

        if (mLanguage.equalsIgnoreCase("")) {
            mLanguage = "Question not answered" + next_line;
        }

        return mLanguage;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", gender='" + gender + '\'' +
                ", min_age='" + min_age + '\'' +
                ", max_age='" + max_age + '\'' +
                ", display='" + display + '\'' +
                ", display_oriya='" + display_oriya + '\'' +
                ", display_cebuno='" + display_cebuno + '\'' +
                ", display_hindi='" + display_hindi + '\'' +
                ", display_bengali='" + display_bengali + '\'' +
                ", display_kannada='" + display_kannada + '\'' +
                ", display_gujarati='" + display_gujarati + '\'' +
                ", display_assamese='" + display_assamese + '\'' +
                ", language='" + language + '\'' +
                ", choiceType='" + choiceType + '\'' +
                ", inputType='" + inputType + '\'' +
                ", physicalExams='" + physicalExams + '\'' +
                ", optionsList=" + optionsList +
                ", associatedComplaint='" + associatedComplaint + '\'' +
                ", jobAidFile='" + jobAidFile + '\'' +
                ", jobAidType='" + jobAidType + '\'' +
                ", pop_up='" + pop_up + '\'' +
                ", positiveCondition='" + positiveCondition + '\'' +
                ", negativeCondition='" + negativeCondition + '\'' +
                ", rootNode=" + rootNode +
                ", complaint=" + complaint +
                ", required=" + required +
                ", terminal=" + terminal +
                ", hasAssociations=" + hasAssociations +
                ", aidAvailable=" + aidAvailable +
                ", selected=" + selected +
                ", subSelected=" + subSelected +
                ", hasPhysicalExams=" + hasPhysicalExams +
                ", hasPopUp=" + hasPopUp +
                ", subPopUp=" + subPopUp +
                ", isNoSelected=" + isNoSelected +
                ", imagePathList=" + imagePathList +
                ", space='" + space + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

    public void fetchAge(float age) {

        //for 1st level
        for (int i = 0; i < optionsList.size(); i++) {
            if (!optionsList.get(i).getMin_age().equalsIgnoreCase("") &&
                    !optionsList.get(i).getMax_age().equalsIgnoreCase("")) {
                if (age < Float.parseFloat(optionsList.get(i).getMin_age().trim())) { //age = 1 , min_age = 5
                    remove(optionsList, i);
                    i--;
                }

                //else if(!optionsList.get(i).getMax_age().equalsIgnoreCase(""))
                else if (age > Float.parseFloat(optionsList.get(i).getMax_age())) { //age = 15 , max_age = 10
                    remove(optionsList, i);
                    i--;
                }
            }
        }

        //2nd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) {
                    if (!optionsList.get(i).getOptionsList()
                            .get(j).getMin_age().equalsIgnoreCase("") &&
                            !optionsList.get(i).getOptionsList()
                                    .get(j).getMax_age().equalsIgnoreCase("")) {
                        if (age < Float.parseFloat(optionsList.get(i).getOptionsList()
                                .get(j).getMin_age())) {
                            remove(optionsList.get(i).getOptionsList(), j);
                            j--;
                        } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList()
                                .get(j).getMax_age())) {
                            remove(optionsList.get(i).getOptionsList(), j);
                            j--;
                        }
                    }
                }
            }
        }

        //3rd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (!optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMin_age().equalsIgnoreCase("") && !optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMax_age().equalsIgnoreCase("")) {
                                if (age < Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMin_age())) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList(), k);
                                    k--;
                                } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMax_age())) {
                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList(), k);
                                    k--;
                                }
                            }
                        }
                    }
                }
            }
        }

        //4th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {

                                    if (!optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                            .getMin_age().equalsIgnoreCase("") && !optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                            .getMax_age().equalsIgnoreCase("")) {

                                        if (age < Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                                .getMin_age())) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                            remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), l);
                                            l--;
                                        } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                                .getMax_age())) {
                                            remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), l);
                                            l--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //5th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {
                                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                            .get(l).getOptionsList() != null) {
                                        for (int m = 0; m < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                .get(l).getOptionsList().size(); m++) {

                                            if (!optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                    .get(l).getOptionsList().get(m).getMin_age().equalsIgnoreCase("") && !optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                    .get(l).getOptionsList().get(m).getMax_age().equalsIgnoreCase("")) {
                                                if (age < Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                        .get(l).getOptionsList().get(m).getMin_age())) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                            .get(l).getOptionsList(), m);
                                                    m--;
                                                } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                        .get(l).getOptionsList().get(m).getMax_age())) {

                                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                            .get(l).getOptionsList(), m);
                                                    m--;

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void fetchItem(String s) {

        //for 1st level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getGender().equalsIgnoreCase(s)) {
                remove(optionsList, i);
                i--;
            }
        }

        //2nd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) {
                    if (optionsList.get(i).getOptionsList().get(j).getGender().equalsIgnoreCase(s)) {
                        remove(optionsList.get(i).getOptionsList(), j);
                        j--;
                    }
                }
            }
        }

        //3rd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getGender().equalsIgnoreCase(s)) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList(), k);
                                k--;
                            }
                        }
                    }
                }
            }
        }

        //4th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {
                                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                            .getGender().equalsIgnoreCase(s)) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                        remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), l);
                                        l--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //5th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {
                                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                            .get(l).getOptionsList() != null) {
                                        for (int m = 0; m < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                .get(l).getOptionsList().size(); m++) {

                                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                    .get(l).getOptionsList().get(m).getGender().equalsIgnoreCase(s)) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                        .get(l).getOptionsList(), m);
                                                m--;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // removes options specific to index from the json
    public void remove(List<Node> no, int index) {
        no.remove(index);
    }

    static public String dateformate_hi_or_gu_as_en(String displayStr, SessionManager sessionManager) {
        if (sessionManager.getCurrentLang().equalsIgnoreCase("hi")) {
            displayStr = displayStr.replaceAll("मिनट", "Minutes")
                    .replaceAll("घंटे", "Hours")
                    .replaceAll("दिन", "Days")
                    .replaceAll("हफ्तों", "Weeks")
                    .replaceAll("महीने", "Months")
                    .replaceAll("वर्ष", "Years")

                    .replaceAll("जन", "Jan")
                    .replaceAll("फ़र", "Feb")
                    .replaceAll("मार्च", "Mar")
                    .replaceAll("अप्रै", "Apr")
                    .replaceAll("मई", "May")
                    .replaceAll("जून", "Jun")
                    .replaceAll("जुला", "Jul")
                    .replaceAll("अग", "Aug")
                    .replaceAll("सित", "Sep")
                    .replaceAll("अक्टू", "Oct")
                    .replaceAll("नव", "Nov")
                    .replaceAll("दिस", "Dec");
        }
        else if (sessionManager.getCurrentLang().equalsIgnoreCase("or")) {
            displayStr = displayStr.replaceAll("ମିନିଟ୍ \\|", "Minutes")
                    .replaceAll("ଘଣ୍ଟା", "Hours")
                    .replaceAll("ଦିନଗୁଡିକ", "Days")
                    .replaceAll("ସପ୍ତାହଗୁଡିକ", "Weeks")
                    .replaceAll("ମାସଗୁଡିକ", "Months")
                    .replaceAll("ବର୍ଷଗୁଡିକ", "Years")

                    .replaceAll("ଜାନୁଆରୀ", "Jan")
                    .replaceAll("ଫେବୃଆରୀ", "Feb")
                    .replaceAll("ମାର୍ଚ୍ଚ", "Mar")
                    .replaceAll("ଅପ୍ରେଲ", "Apr")
                    .replaceAll("ମଲ", "May")
                    .replaceAll("ଜୁନ୍", "Jun")
                    .replaceAll("ଜୁଲାଇ", "Jul")
                    .replaceAll("ଅଗଷ୍ଟ", "Aug")
                    .replaceAll("ସେପ୍ଟେମ୍ବର", "Sep")
                    .replaceAll("ଅକ୍ଟୋବର", "Oct")
                    .replaceAll("ନଭେମ୍ବର", "Nov")
                    .replaceAll("ଡିସେମ୍ବର", "Dec");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("bn")) {
            displayStr = displayStr.replaceAll("মিনিট", "Minutes")
                    .replaceAll("ঘন্টার", "Hours")
                    .replaceAll("দিন", "Days")
                    .replaceAll("সপ্তাহ", "Weeks")
                    .replaceAll("মাস", "Months")
                    .replaceAll("বছর", "Years")

                    .replaceAll("জান", "Jan")
                    .replaceAll("ফেব্রুয়ারী", "Feb")
                    .replaceAll("মার", "Mar")
                    .replaceAll("এপ্রিল", "Apr")
                    .replaceAll("মে", "May")
                    .replaceAll("জুন", "Jun")
                    .replaceAll("জুল", "Jul")
                    .replaceAll("অগাস্ট", "Aug")
                    .replaceAll("সেপ্টেম্বর", "Sep")
                    .replaceAll("অক্টো", "Oct")
                    .replaceAll("নভেম্বর", "Nov")
                    .replaceAll("ডিসেম্বর", "Dec");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("kn")) {
            displayStr = displayStr.replaceAll("ನಿಮಿಷಗಳು", "Minutes")
                    .replaceAll("ಗಂಟೆಗಳು", "Hours")
                    .replaceAll("ದಿನಗಳು", "Days")
                    .replaceAll("ವಾರಗಳು", "Weeks")
                    .replaceAll("ತಿಂಗಳುಗಳು", "Months")
                    .replaceAll("ವರ್ಷಗಳು", "Years")

                    .replaceAll("ಜನವರಿ", "Jan")
                    .replaceAll("ಫೆಬ್ರವರಿ", "Feb")
                    .replaceAll("ಮಾರ್", "Mar")
                    .replaceAll("ಎಪ್ರಿಲ್", "Apr")
                    .replaceAll("ಮೇ", "May")
                    .replaceAll("ಜೂನ್", "Jun")
                    .replaceAll("ಜುಲೈ", "Jul")
                    .replaceAll("ಆಗಸ್ಟ್", "Aug")
                    .replaceAll("ಸೆ", "Sep")
                    .replaceAll("ಅಕ್ಟೋಬರ್", "Oct")
                    .replaceAll("ನವೆಂಬರ್", "Nov")
                    .replaceAll("ಡಿಸೆಂಬರ್", "Dec");
        }
        else if (sessionManager.getCurrentLang().equalsIgnoreCase("gu")) {
            displayStr = displayStr.replaceAll("મિનિટ", "Minutes")
                    .replaceAll("કલાકો", "Hours")
                    .replaceAll("દિવસ", "Days")
                    .replaceAll("અઠવાડિયા", "Weeks")
                    .replaceAll("મહિનાઓ", "Months")
                    .replaceAll("વર્ષ", "Years")

                    .replaceAll("જાન્યુ", "Jan")
                    .replaceAll("ફેબ્રુ", "Feb")
                    .replaceAll("માર્ચ", "Mar")
                    .replaceAll("એપ્રિલ", "Apr")
                    .replaceAll("મે", "May")
                    .replaceAll("જુન", "Jun")
                    .replaceAll("જુલાઇ", "Jul")
                    .replaceAll("ઓગસ્ટ", "Aug")
                    .replaceAll("સપ્ટે", "Sep")
                    .replaceAll("ઑક્ટો", "Oct")
                    .replaceAll("નવે", "Nov")
                    .replaceAll("ડિસે", "Dec");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("as")) {
            displayStr = displayStr.replaceAll("মিনিটবোৰ", "Minutes")
                    .replaceAll("ঘণ্টা", "Hours")
                    .replaceAll("দিনবোৰ", "Days")
                    .replaceAll("সপ্তাহ", "Weeks")
                    .replaceAll("মাহবোৰ", "Months")
                    .replaceAll("বছৰবোৰ", "Years")

                    .replaceAll("জানুৱাৰী", "Jan")
                    .replaceAll("ফেব্ৰুৱাৰী", "Feb")
                    .replaceAll("মাৰ্চ", "Mar")
                    .replaceAll("এপ্ৰিল", "Apr")
                    .replaceAll("হয়তো", "May")
                    .replaceAll("জুন", "Jun")
                    .replaceAll("জুলাই", "Jul")
                    .replaceAll("আগষ্ট", "Aug")
                    .replaceAll("ছেপ্টেম্বৰ", "Sep")
                    .replaceAll("অক্টোবৰ", "Oct")
                    .replaceAll("নৱেম্বৰ", "Nov")
                    .replaceAll("ডিচেম্বৰ", "Dec");
        }
        return displayStr;
    }

    static public String dateformat_en_hi_or_gu_as(String displayStr, SessionManager sessionManager) {
        if (sessionManager.getCurrentLang().equalsIgnoreCase("hi")) {
            displayStr = displayStr.replaceAll("Minutes", "मिनट")
                    .replaceAll("Hours", "घंटे")
                    .replaceAll("Days", "दिन")
                    .replaceAll("Weeks", "हफ्तों")
                    .replaceAll("Months", "महीने")
                    .replaceAll("Years", "वर्ष")

                    .replaceAll("Jan", "जन")
                    .replaceAll("Feb", "फ़र")
                    .replaceAll("Mar", "मार्च")
                    .replaceAll("Apr", "अप्रै")
                    .replaceAll("May", "मई")
                    .replaceAll("Jun", "जून")
                    .replaceAll("Jul", "जुला")
                    .replaceAll("Aug", "अग")
                    .replaceAll("Sep", "सित")
                    .replaceAll("Oct", "अक्टू")
                    .replaceAll("Nov", "नव")
                    .replaceAll("Dec", "दिस");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("or")) {
            displayStr = displayStr.replaceAll("Minutes", "ମିନିଟ୍ |")
                    .replaceAll("Hours", "ଘଣ୍ଟା")
                    .replaceAll("Days", "ଦିନଗୁଡିକ")
                    .replaceAll("Weeks", "ସପ୍ତାହଗୁଡିକ")
                    .replaceAll("Months", "ମାସଗୁଡିକ")
                    .replaceAll("Years", "ବର୍ଷଗୁଡିକ")

                    .replaceAll("Jan", "ଜାନୁଆରୀ")
                    .replaceAll("Feb", "ଫେବୃଆରୀ")
                    .replaceAll("Mar", "ମାର୍ଚ୍ଚ")
                    .replaceAll("Apr", "ଅପ୍ରେଲ")
                    .replaceAll("May", "ମଲ")
                    .replaceAll("Jun", "ଜୁନ୍")
                    .replaceAll("Jul", "ଜୁଲାଇ")
                    .replaceAll("Aug", "ଅଗଷ୍ଟ")
                    .replaceAll("Sep", "ସେପ୍ଟେମ୍ବର")
                    .replaceAll("Oct", "ଅକ୍ଟୋବର")
                    .replaceAll("Nov", "ନଭେମ୍ବର")
                    .replaceAll("Dec", "ଡିସେମ୍ବର");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("bn")) {
            displayStr = displayStr.replaceAll("Minutes", "মিনিট")
                    .replaceAll("Hours", "ঘন্টার")
                    .replaceAll("Days", "দিন")
                    .replaceAll("Weeks", "সপ্তাহ")
                    .replaceAll("Months", "মাস")
                    .replaceAll("Years", "বছর")

                    .replaceAll("Jan", "জান")
                    .replaceAll("Feb", "ফেব্রুয়ারী")
                    .replaceAll("Mar", "মার")
                    .replaceAll("Apr", "এপ্রিল")
                    .replaceAll("May", "মে")
                    .replaceAll("Jun", "জুন")
                    .replaceAll("Jul", "জুল")
                    .replaceAll("Aug", "অগাস্ট")
                    .replaceAll("Sep", "সেপ্টেম্বর")
                    .replaceAll("Oct", "অক্টো")
                    .replaceAll("Nov", "নভেম্বর")
                    .replaceAll("Dec", "ডিসেম্বর");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("kn")) {
            displayStr = displayStr.replaceAll("Minutes", "ನಿಮಿಷಗಳು")
                    .replaceAll("Hours", "ಗಂಟೆಗಳು")
                    .replaceAll("Days", "ದಿನಗಳು")
                    .replaceAll("Weeks", "ವಾರಗಳು")
                    .replaceAll("Months", "ತಿಂಗಳುಗಳು")
                    .replaceAll("Years", "ವರ್ಷಗಳು")

                    .replaceAll("Jan", "ಜನವರಿ")
                    .replaceAll("Feb", "ಫೆಬ್ರವರಿ")
                    .replaceAll("Mar", "ಮಾರ್")
                    .replaceAll("Apr", "ಎಪ್ರಿಲ್")
                    .replaceAll("May", "ಮೇ")
                    .replaceAll("Jun", "ಜೂನ್")
                    .replaceAll("Jul", "ಜುಲೈ")
                    .replaceAll("Aug", "ಆಗಸ್ಟ್")
                    .replaceAll("Sep", "ಸೆ")
                    .replaceAll("Oct", "ಅಕ್ಟೋಬರ್")
                    .replaceAll("Nov", "ನವೆಂಬರ್")
                    .replaceAll("Dec", "ಡಿಸೆಂಬರ್");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("gu")) {
            displayStr = displayStr.replaceAll("Minutes", "મિનિટ")
                    .replaceAll("Hours", "કલાકો")
                    .replaceAll("Days", "દિવસ")
                    .replaceAll("Weeks", "અઠવાડિયા")
                    .replaceAll("Months", "મહિનાઓ")
                    .replaceAll("Years", "વર્ષ")

                    .replaceAll("Jan", "જાન્યુ")
                    .replaceAll("Feb", "ફેબ્રુ")
                    .replaceAll("Mar", "માર્ચ")
                    .replaceAll("Apr", "એપ્રિલ")
                    .replaceAll("May", "મે")
                    .replaceAll("Jun", "જુન")
                    .replaceAll("Jul", "જુલાઇ")
                    .replaceAll("Aug", "ઓગસ્ટ")
                    .replaceAll("Sep", "સપ્ટે")
                    .replaceAll("Oct", "ઑક્ટો")
                    .replaceAll("Nov", "નવે")
                    .replaceAll("Dec", "ડિસે");
        } else if (sessionManager.getCurrentLang().equalsIgnoreCase("as")) {
            displayStr = displayStr.replaceAll("Minutes", "মিনিটবোৰ")
                    .replaceAll("Hours", "ঘণ্টা")
                    .replaceAll("Days", "দিনবোৰ")
                    .replaceAll("Weeks", "সপ্তাহ")
                    .replaceAll("Months", "মাহবোৰ")
                    .replaceAll("Years", "বছৰবোৰ")

                    .replaceAll("Jan", "জানুৱাৰী")
                    .replaceAll("Feb", "ফেব্ৰুৱাৰী")
                    .replaceAll("Mar", "মাৰ্চ")
                    .replaceAll("Apr", "এপ্ৰিল")
                    .replaceAll("May", "হয়তো")
                    .replaceAll("Jun", "জুন")
                    .replaceAll("Jul", "জুলাই")
                    .replaceAll("Aug", "আগষ্ট")
                    .replaceAll("Sep", "ছেপ্টেম্বৰ")
                    .replaceAll("Oct", "অক্টোবৰ")
                    .replaceAll("Nov", "নৱেম্বৰ")
                    .replaceAll("Dec", "ডিচেম্বৰ");
        }
        return displayStr;
    }

    //Check to see if all required exams have been answered before moving on.
    public AnswerResult checkAllRequiredAnswered(Context context) {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(context);
        String locale = sessionManager.getCurrentLang();

        AnswerResult answerResult = new AnswerResult();
        answerResult.totalCount = optionsList.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getResources().getString(R.string.answer_following_questions));
        stringBuilder.append("\n");
        for (int i = 0; i < optionsList.size(); i++) {
            Node node = optionsList.get(i);
            if (node.isRequired()) {
                if (node.optionsList != null && !node.optionsList.isEmpty()) {
                    if (!node.isSelected() || !node.anySubSelected() || (node.isSelected() && !isNestedMandatoryOptionsAnswered(node))) {
                        switch (locale) {
                            case "en":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display);
                                break;
                            case "hi":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_hindi);
                                break;
                            case "bn":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_bengali);
                                break;
                            case "kn":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_kannada);
                                break;
                            case "or":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_oriya);
                                break;
                            case "gu":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_gujarati);
                                break;
                            case "as":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_assamese);
                                break;
                        }
                        answerResult.result = false;
                    }
                } else {
                    if (!node.isSelected()) {
                        switch (locale) {
                            case "en":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display);
                                break;
                            case "hi":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_hindi);
                                break;
                            case "bn":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_bengali);
                                break;
                            case "kn":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_kannada);
                                break;
                            case "or":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_oriya);
                                break;
                            case "gu":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_gujarati);
                                break;
                            case "as":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_assamese);
                                break;
                        }
                        answerResult.result = false;
                    }
                }

                Log.v(TAG, node.text);
                Log.v(TAG, node.text);
                Log.v(TAG, String.valueOf(node.isSelected()));
            }
        }
        answerResult.requiredStrings = stringBuilder.toString();
        return answerResult;
    }

    //Check to see if all required exams have been answered before moving on.
    public AnswerResult checkAllRequiredAnsweredPhy(Context context) {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(context);
        String locale = sessionManager.getCurrentLang();

        AnswerResult answerResult = new AnswerResult();
        answerResult.totalCount = optionsList.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getResources().getString(R.string.answer_following_questions));
        stringBuilder.append("\n");
        for (int i = 0; i < optionsList.size(); i++) {
            Node node = optionsList.get(i);
            if (node.isRequired()) {
                if (node.optionsList != null && !node.optionsList.isEmpty()) {
                    for (int j = 0; j < node.optionsList.size(); j++) {
                        Node subnode = node.optionsList.get(j);
                        if (subnode.isRequired()) {
                            if (subnode.optionsList != null && !subnode.optionsList.isEmpty()) {
                                for (int k = 0; k < subnode.optionsList.size(); k++) {
                                    Node supersubnode = subnode.optionsList.get(k);
                                    if (supersubnode.isRequired()) {
                                        if (supersubnode.optionsList != null && !supersubnode.optionsList.isEmpty()) {
                                            if (!supersubnode.isSelected() || !supersubnode.anySubSelected() || (supersubnode.isSelected() && !isNestedMandatoryOptionsAnswered(supersubnode))) {
                                                switch (locale) {
                                                    case "en":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display);
                                                        break;
                                                    case "hi":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_hindi);
                                                        break;
                                                    case "bn":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_bengali);
                                                        break;
                                                    case "kn":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_kannada);
                                                        break;
                                                    case "or":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_oriya);
                                                        break;
                                                    case "gu":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_gujarati);
                                                        break;
                                                    case "as":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_assamese);
                                                        break;
                                                }
                                                answerResult.result = false;
                                            }
                                        } else {
                                            if (!supersubnode.isSelected()) {
                                                switch (locale) {
                                                    case "en":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display);
                                                        break;
                                                    case "hi":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_hindi);
                                                        break;
                                                    case "bn":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_bengali);
                                                        break;
                                                    case "kn":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_kannada);
                                                        break;
                                                    case "or":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_oriya);
                                                        break;
                                                    case "gu":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_gujarati);
                                                        break;
                                                    case "as":
                                                        stringBuilder.append("\n").append(bullet + " ").append(supersubnode.display_assamese);
                                                        break;
                                                }
                                                answerResult.result = false;
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (!subnode.isSelected()) {
                                    switch (locale) {
                                        case "en":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display);
                                            break;
                                        case "hi":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display_hindi);
                                            break;
                                        case "bn":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display_bengali);
                                            break;
                                        case "kn":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display_kannada);
                                            break;
                                        case "or":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display_oriya);
                                            break;
                                        case "gu":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display_gujarati);
                                            break;
                                        case "as":
                                            stringBuilder.append("\n").append(bullet + " ").append(subnode.display_assamese);
                                            break;
                                    }
                                    answerResult.result = false;
                                }
                            }
                        }
                    }
                } else {
                    if (!node.isSelected()) {
                        switch (locale) {
                            case "en":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display);
                                break;
                            case "hi":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_hindi);
                                break;
                            case "bn":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_bengali);
                                break;
                            case "kn":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_kannada);
                                break;
                            case "or":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_oriya);
                                break;
                            case "gu":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_gujarati);
                                break;
                            case "as":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_assamese);
                                break;
                        }
                        answerResult.result = false;
                    }
                }

                Log.v(TAG, node.text);
                Log.v(TAG, node.text);
                Log.v(TAG, String.valueOf(node.isSelected()));
            }
        }
        answerResult.requiredStrings = stringBuilder.toString();
        return answerResult;
    }

    public boolean isNestedMandatoryOptionsAnswered(Node node) {
        Log.v("isNestedMandatory", new Gson().toJson(node).toString());
        boolean allAnswered = node.isSelected();
        /*if(node.isSelected() && node.isRequired() && node.optionsList.size()==1){
            if(!node.optionsList.get(0).isSelected()){
                return  false;
            }
        }*/
        if (node.optionsList != null && !node.optionsList.isEmpty()) {
            for (int i = 0; i < node.optionsList.size(); i++) {
                Node innerNode = node.optionsList.get(i);
                if (innerNode.isRequired() && innerNode.isSelected() && innerNode.optionsList != null && !innerNode.optionsList.isEmpty()) {
                    if (!isNestedMandatoryOptionsAnswered(innerNode)) {
                        allAnswered = false;
                        break;
                    }
                }
            }

        }
        return allAnswered;
    }
}

