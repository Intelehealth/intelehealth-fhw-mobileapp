package io.intelehealth.client.node;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
import java.util.List;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.camera_activity.CameraActivity;
import io.intelehealth.client.activities.complaint_node_activity.CustomArrayAdapter;
import io.intelehealth.client.activities.custom_expandable_list_adapter.CustomExpandableListAdapter;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node implements Serializable {

    private String id;
    private String text;
    private String display;
    private String display_oriya;
    private String language;
    private String choiceType;
    private String inputType;
    private String physicalExams;
    private List<Node> optionsList;
    private String associatedComplaint;
    private String jobAidFile;
    private String jobAidType;

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

    private List<String> imagePathList;

    public static String bullet = "\u2022";
    public static String big_bullet = "\u25CF";
    public static String bullet_hollow = "\u25CB";
    public static String bullet_arrow = "\u25BA";
    public static String next_line = "<br/>";
    String space = "\t";


    //• = \u2022, ● = \u25CF, ○ = \u25CB, ▪ = \u25AA, ■ = \u25A0, □ = \u25A1, ► = \u25BA

    private String imagePath;

    private String bullet = "\u2022";
    private String big_bullet = "\u25CF";
    private String next_line = "<br/>";


    /**
     * Nodes refer to the structure that is used for a decision tree or mindmap.
     * The node object is stored in the same structure where the there is a root node which contains all the sub-nodes.
     * The nodes are also tagged based on the attributes each JSON object shows.
     * <p>
     * Most nodes are single choice questions. Therefore, they can just be clicked and selected.
     * Some nodes may be multi-choice, in which case there must be an attribute within the JSON to dictate that.
     * <p>
     * text - the text that is displayed on the app to user
     * language - the text that is displayed after answering a question
     * differs from the text attribute in that this is the response form of a question
     * inputType - dictates if the node is something other that choice-based
     * types include: text, number, date, duration, area, range, frequency
     * physicalExams - any physical exams that should be triggered in the application if the node is selected
     * optionsList - container of sub-nodes of the current node
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
            if (!(this.physicalExams == null)) {
                this.hasPhysicalExams = true;
            } else {
                this.hasPhysicalExams = false;
            }

            this.jobAidFile = jsonNode.optString("job-aid-file");
            if (!jobAidFile.isEmpty()) {
                this.jobAidType = jsonNode.optString("job-aid-type");
                this.aidAvailable = true;
            } else {
                this.aidAvailable = false;
            }

            this.associatedComplaint = jsonNode.optString("associated-complaint");
            if (associatedComplaint.isEmpty()) {
                this.hasAssociations = false;
            } else {
                this.hasAssociations = true;
            }


            this.selected = false;

            this.choiceType = jsonNode.optString("choice-type");

            this.required = false;

            this.positiveCondition = jsonNode.optString("pos-condition");
            this.negativeCondition = jsonNode.optString("neg-condition");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes a copy of the node, so that the original reference node is not modified.
     *
     * @param source source node to copy into a new node. Will always default as unselected.
     */
    public Node(Node source) {
        //this.id = source.id;
        this.text = source.text;
        this.display = source.display;
        this.display_oriya = source.display_oriya;
        this.optionsList = source.optionsList;
        this.terminal = source.terminal;
        this.language = source.language;
        this.inputType = source.inputType;
        this.physicalExams = source.physicalExams;
        this.complaint = source.complaint;
        this.jobAidFile = source.jobAidFile;
        this.jobAidType = source.jobAidType;
        this.aidAvailable = source.aidAvailable;
        this.associatedComplaint = source.associatedComplaint;
        this.hasAssociations = source.hasAssociations;
        this.hasPhysicalExams = source.hasPhysicalExams;
        this.selected = false;
        this.required = source.required;
        this.positiveCondition = source.positiveCondition;
        this.negativeCondition = source.negativeCondition;
    }

    /**
     * Takes a JSON Array from a node and creates the sub-nodes to store within it.
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
            e.printStackTrace();
        }

        return createdOptions;
    }

    //Terminal nodes are important to identify to know so that the app does not keep looking for sub-nodes.
    public boolean isTerminal() {
        return terminal;
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
            language = language + " " + newText;
            //Log.d("Node", language);
        }
    }

    public String findDisplay() {
        String locale = Locale.getDefault().getISO3Language();

        switch (locale) {
            case "eng": {
                //Log.i(TAG, "findDisplay: eng");
                if (display != null && display.isEmpty()) {
                    //Log.i(TAG, "findDisplay: eng txt");
                    return text;
                } else {
                    //Log.i(TAG, "findDisplay: eng dis");
                    return display;
                }
            }
            case "ori": {
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

    public boolean isSelected() {
        return selected;
    }

    public void setUnselected() {
        selected = false;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public void setSelected() {
        selected = true;
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

    /*
        Language needs to be built recursively for each first level question of a complaint.
        In this context, all the language must be built by searching a node, and then looking at sub-nodes to determine which are selected.
        Once a terminal node is found, then the "sentence" of the primary starting node is complete.
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
                    if (test.equals("%")) {
                    } else if (test.substring(0, 1).equals("%")) {
                        stringsList.add(test.substring(1));
                    } else {
                        stringsList.add(test);
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

    public String generateLanguage() {

        String raw = "";
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected()) {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals("Associated symptoms") ||
                            associatedTest.trim().equals("H/o specific illness"))) {
                        raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                    } else {
                        if (node_opt.getLanguage().equals("%")) {
                            raw = raw + bullet + " " + node_opt.formLanguage() + next_line;
                        } else if (node_opt.getLanguage().substring(0, 1).equals("%")) {
                            raw = raw + (bullet + " " + node_opt.getLanguage().substring(1) + " - " + node_opt.formLanguage()) + next_line;
                        } else {
                            raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + node_opt.formLanguage()) + next_line;
                        }
                    }
                    //raw = raw + ("\n"+"\n" + bullet +" "+ node_opt.formLanguage());
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

    public static void subLevelQuestion(final Node node, final Activity context, final CustomExpandableListAdapter callingAdapter,
                                        final String imagePath, final String imageName) {

        node.setSelected();
        List<Node> mNodes = node.getOptionsList();
        final CustomArrayAdapter adapter = new CustomArrayAdapter(context, R.layout.list_item_subquestion, mNodes);
        final AlertDialog.Builder subQuestion = new AlertDialog.Builder(context);

        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_subquestion, null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.dialog_subquestion_image_view);
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
        ListView listView = (ListView) convertView.findViewById(R.id.dialog_subquestion_list_view);
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
            }
        });
        subQuestion.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.toggleSelected();
                callingAdapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });

        subQuestion.setView(convertView);
        subQuestion.show();

    }

    public static void handleQuestion(Node questionNode, final Activity context, final CustomExpandableListAdapter adapter,
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

    public AlertDialog displayImage(final Activity context, final String path, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screen_height = displayMetrics.heightPixels;
                int screen_width = displayMetrics.widthPixels;

                ImageView imageView = (ImageView) dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);
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
        return dialog;

    }


    public static void askText(final Node node, Activity context, final CustomExpandableListAdapter adapter) {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
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
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        textInput.show();
    }

    public static void askDate(final Node node, final Activity context, final CustomExpandableListAdapter adapter) {
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
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", context.getResources().getConfiguration().locale);
                        String dateString = simpleDateFormat.format(date);


                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", dateString));
                        } else {
                            node.addLanguage(" " + dateString);
                            node.setText(node.getLanguage());
                            //node.setText(node.getLanguage());
                        }
                        node.setSelected();
                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        datePickerDialog.show();
    }

    public static void askLocation(final Node node, Activity context, final CustomExpandableListAdapter adapter) {

        final AlertDialog.Builder locationDialog = new AlertDialog.Builder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub


    }

    public static void askNumber(final Node node, Activity context, final CustomExpandableListAdapter adapter) {

        final AlertDialog.Builder numberDialog = new AlertDialog.Builder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
        final NumberPicker numberPicker = (NumberPicker) convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(100);
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", value));
                } else {
                    node.addLanguage(" " + value);
                    node.setText(value);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        numberDialog.show();

    }

    public static void askArea(final Node node, Activity context, final CustomExpandableListAdapter adapter) {

        final AlertDialog.Builder areaDialog = new AlertDialog.Builder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
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
                String durationString = String.valueOf(widthPicker.getValue()) + " X " + lengthPicker.getValue();


                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        areaDialog.show();

    }

    public static void askRange(final Node node, Activity context, final CustomExpandableListAdapter adapter) {

        final AlertDialog.Builder rangeDialog = new AlertDialog.Builder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
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
                String durationString = String.valueOf(startPicker.getValue()) + " to " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        rangeDialog.show();
    }

    public static void askFrequency(final Node node, Activity context, final CustomExpandableListAdapter adapter) {

        final AlertDialog.Builder frequencyDialog = new AlertDialog.Builder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{"per Hour", "per Day", "Per Week", "per Month", "per Year"};
        final String[] doctorUnits = new String[]{"times per hour", "time per day", "times per week", "times per month", "times per year"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + doctorUnits[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        frequencyDialog.show();

    }

    public static void askDuration(final Node node, Activity context, final CustomExpandableListAdapter adapter) {
        final AlertDialog.Builder durationDialog = new AlertDialog.Builder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + units[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        durationDialog.show();
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

    public static void subAskText(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
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
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        textInput.show();
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
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", context.getResources().getConfiguration().locale);
                        String dateString = simpleDateFormat.format(date);
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", dateString));
                        } else {
                            node.addLanguage(" " + dateString);
                            node.setText(node.getLanguage());
                            //node.setText(node.getLanguage());
                        }
                        node.setSelected();
                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        datePickerDialog.show();
    }

    public static void subAskLocation(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final AlertDialog.Builder locationDialog = new AlertDialog.Builder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub
    }

    public static void subAskNumber(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final AlertDialog.Builder numberDialog = new AlertDialog.Builder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
        final NumberPicker numberPicker = (NumberPicker) convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(100);
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", value));
                } else {
                    node.addLanguage(" " + value);
                    node.setText(value);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        numberDialog.show();

    }

    public static void subAskArea(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final AlertDialog.Builder areaDialog = new AlertDialog.Builder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
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
                String durationString = String.valueOf(widthPicker.getValue()) + " X " + lengthPicker.getValue();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        areaDialog.show();

    }

    public static void subAskRange(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final AlertDialog.Builder rangeDialog = new AlertDialog.Builder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
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
                String durationString = String.valueOf(startPicker.getValue()) + " to " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        rangeDialog.show();
    }

    public static void subAskFrequency(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final AlertDialog.Builder frequencyDialog = new AlertDialog.Builder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = context.getResources().getStringArray(R.array.units);
        final String[] doctorUnits = context.getResources().getStringArray(R.array.doctor_units);
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + doctorUnits[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        frequencyDialog.show();

    }

    public static void subAskDuration(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final AlertDialog.Builder durationDialog = new AlertDialog.Builder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = context.getResources().getStringArray(R.array.duration_units);
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + units[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
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
        durationDialog.show();
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

        String mLanguagePositive = associatedSymptomNode.positiveCondition;
        String mLanguageNegative = associatedSymptomNode.negativeCondition;

        Log.i(TAG, "generateAssociatedSymptomsOrHistory: " + mLanguagePositive);
        Log.i(TAG, "generateAssociatedSymptomsOrHistory: " + mLanguageNegative);


        for (int i = 0; i < mOptions.size(); i++) {


            if (mOptions.get(i).isSelected()) {
                if (mOptions.get(i).getLanguage().equals("%")) {
                } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
                    positiveAssociations.add(mOptions.get(i).getLanguage().substring(1));
                } else if (mOptions.get(i).getLanguage().isEmpty()) {
                    positiveAssociations.add(mOptions.get(i).getText());
                } else {
                    positiveAssociations.add(mOptions.get(i).getLanguage());
                }
                if (!mOptions.get(i).isTerminal()) {
                    if (positiveAssociations.size() > 0) {
                        String tempString = positiveAssociations.get(positiveAssociations.size() - 1) + " - " +
                                mOptions.get(i).formLanguage();

                        positiveAssociations.set(positiveAssociations.size() - 1, tempString);
                    }
                }
            } else {
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

        if (positiveAssociations != null && !positiveAssociations.isEmpty()) {
            finalTexts.add(bullet_hollow);
            for (String string : positiveAssociations) {
                Log.i(TAG, "generateAssociatedSymptomsOrHistory:  " + mLanguagePositive);
                finalTexts.add(mLanguagePositive + " " + string);
            }
        }


        if (negativeAssociations != null && !negativeAssociations.isEmpty()) {
            finalTexts.add(bullet_hollow);
            for (String string : negativeAssociations) {
                Log.i(TAG, "generateAssociatedSymptomsOrHistory:  " + mLanguageNegative);
                finalTexts.add(mLanguageNegative + " " + string);
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
        final_language = final_language.replaceAll(", " + bullet_hollow + ", ", " " + bullet_hollow + " ");
        Log.i(TAG, "generateAssociatedSymptomsOrHistory: " + final_language);

        return final_language;

    }

    public String formQuestionAnswer(int level) {
        List<String> stringsList = new ArrayList<>();
        List<Node> mOptions = optionsList;

        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected()) {
                String question;
                if (level == 0) {
                    question = big_bullet + " " + mOptions.get(i).findDisplay();
                } else {
                    question = bullet + " " + mOptions.get(i).findDisplay();
                }
                String answer = mOptions.get(i).getLanguage();
                if (mOptions.get(i).isTerminal()) {
                    if (mOptions.get(i).getInputType() != null && !mOptions.get(i).getInputType().trim().isEmpty()) {
                        Log.i(TAG, "ipt: " + mOptions.get(i).getInputType());
                        if (mOptions.get(i).getInputType().equals("camera")) {
                        } else {
                            if (answer.equals("%")) {
                            } else if (mOptions.get(i).getText().equals(mOptions.get(i).getLanguage())) {
                                stringsList.add(bullet_hollow + answer + next_line);
                            } else if (answer.substring(0, 1).equals("%")) {
                                stringsList.add(bullet_hollow + answer.substring(1) + next_line);
                            } else {
                                stringsList.add(bullet_hollow + answer + next_line);
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
        }

        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {

            if (!stringsList.get(i).isEmpty()) {
                mLanguage = mLanguage.concat(stringsList.get(i));
            }

        }
        Log.i(TAG, "formQuestionAnswer: " + mLanguage);
        return mLanguage;
    }
}

