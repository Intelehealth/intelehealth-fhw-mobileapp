package app.intelehealth.client.knowledgeEngine;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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

import app.intelehealth.client.R;
import app.intelehealth.client.activities.questionNodeActivity.QuestionsAdapter;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.utilities.InputFilterMinMax;
import app.intelehealth.client.utilities.SessionManager;

import app.intelehealth.client.activities.cameraActivity.CameraActivity;
import app.intelehealth.client.activities.complaintNodeActivity.CustomArrayAdapter;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node implements Serializable {

    private String id;
    private String text;
    private String display;
    private String display_oriya;
    private String display_cebuno;
    private String language;
    private String choiceType;
    private String inputType;
    private String physicalExams;
    private List<Node> optionsList;
    private String associatedComplaint;
    private String jobAidFile;
    private String jobAidType;
    private String pop_up;

    //for Associated Complaints and medical history only
    private String positiveCondition;
    private String negativeCondition;

    //These are specific for physical exams
    private boolean rootNode;

    private boolean complaint;
    private boolean required;
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
            //this.id = jsonNode.getString("id");

            this.text = jsonNode.getString("text");

            JSONArray optionsArray = jsonNode.optJSONArray("options");
            if (optionsArray == null) {
                this.terminal = true;
            } else {
                this.terminal = false;
                this.optionsList = createOptions(optionsArray);
            }

            this.display = jsonNode.optString("display");
            if (this.display.isEmpty()) {
                this.display = jsonNode.optString("display ");
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

            this.required = false;

            this.positiveCondition = jsonNode.optString("pos-condition");
            this.negativeCondition = jsonNode.optString("neg-condition");

            this.pop_up = jsonNode.optString("pop-up");
            this.hasPopUp = !pop_up.isEmpty();

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /**
     * Makes a copy of the knowledgeEngine, so that the original reference knowledgeEngine is not modified.
     *
     * @param source source knowledgeEngine to copy into a new knowledgeEngine. Will always default as unselected.
     */
    public Node(Node source) {
        //this.id = source.id;
        this.text = source.text;
        this.display = source.display;
        this.display_oriya = source.display_oriya;
        this.display_cebuno = source.display_cebuno;
        this.optionsList = source.optionsList;
        this.terminal = source.terminal;
        this.language = source.language;
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
            case "cb": {
                //Log.i(TAG, "findDisplay: cb");
                if (display_cebuno != null && !display_cebuno.isEmpty()) {
                    //Log.i(TAG, "findDisplay: cb ");
                    return display_cebuno;
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
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                } else {
                    node.addLanguage(dialogEditText.getText().toString());
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public String generateLanguage() {

        String raw = "";
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected()) {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms") ||
                            (associatedTest.trim().equals("H/o specific illness")) ||
                            (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")))) {

                        if ((associatedTest.trim().equals("Associated symptoms")) || (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))) {
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
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms")
                            || (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")))) {
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
        datePickerDialog.setTitle(R.string.question_date_picker);
        //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
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

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", value));
                } else {
                    node.addLanguage(" " + value);
                    node.setText(value);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = numberDialog.show();
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
                String durationString = startPicker.getValue() + " to " + endPicker.getValue();
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
                dialog.dismiss();
            }
        });
        AlertDialog dialog = rangeDialog.show();
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
        final String[] units = new String[]{"per Hour", "per Day", "Per Week", "per Month", "per Year"};
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
        final String[] units = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = quantityPicker.getValue() + " " + units[unitPicker.getValue()];

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
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
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
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                } else {
                    node.addLanguage(dialogEditText.getText().toString());
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
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
        datePickerDialog.setTitle(R.string.question_date_picker);
        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
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
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", value));
                } else {
                    node.addLanguage(" " + value);
                    node.setText(value);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = numberDialog.show();
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
                String durationString = startPicker.getValue() + " to " + endPicker.getValue();
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
                dialog.dismiss();
            }
        });
        AlertDialog dialog = rangeDialog.show();
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
        final String[] units = context.getResources().getStringArray(R.array.units);
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
        final String[] units = context.getResources().getStringArray(R.array.duration_units);
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = quantityPicker.getValue() + " " + units[unitPicker.getValue()];

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
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
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

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        {
            dialog.supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        }
        else
        {
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
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))) {
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
            }
            else if (mOptions.get(i).getText() != null &&
                    ((mOptions.get(i).getText().equalsIgnoreCase("Associated symptoms"))
                    || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")))) {

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
            }
            else
            {
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
                ", display='" + display + '\'' +
                ", display_oriya='" + display_oriya + '\'' +
                ", display_cebuno='" + display_cebuno + '\'' +
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
}

