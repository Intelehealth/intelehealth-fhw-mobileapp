package io.intelehealth.telemedicine.objects;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import io.intelehealth.telemedicine.CustomArrayAdapter;
import io.intelehealth.telemedicine.CustomExpandableListAdapter;
import io.intelehealth.telemedicine.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node implements Serializable {

    private String id;
    private String text;
    private String language;
    private boolean multiChoice;
    private String inputType;
    private String physicalExams;
    private List<Node> optionsList;
    private String associatedComplaint;
    private String jobAidFile;
    private String jobAidType;

    private boolean complaint;
    private boolean required;
    private boolean terminal;
    private boolean hasAssociations;
    private boolean aidAvailable;
    private boolean selected;
    private boolean subSelected;

    /**
     * When a node is clicked, it may have a specified input-type.
     * If there is an input, this method should be called, which will then call the appropriate method for the type.
     * Give this method the node, it wil pass it to the right method, and you will get a dialog box with the right input type.
     *
     * @param questionNode Input node that has a "input-type" attribute.
     * @param context The current context.
     * @param adapter The adapter the node is in (typically CustomExpandableListAdapter or CustomArrayAdapter)
     */
    public static void handleQuestion(Node questionNode, final Activity context, final Object adapter) {
        String type = questionNode.getInputType();
        final String[] frequencyUnits = new String[]{"per Hour", "per Day", "per Week", "per Month", "per Year"};
        final String[] doctorUnits = new String[]{"times per hour", "time per day", "times per week", "times per month", "times per year"};
        final String[] durationUnits = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        switch (type) {
            case "text":
                askText(questionNode, context, adapter);
                break;
            case "date":
                askDate(questionNode, context, adapter);
                break;
            case "number":
                askNumber(questionNode, context, adapter);
                break;
            case "area":
                askPickerDialog(questionNode, context, adapter, type);
                break;
            case "duration":
                askPickerUnitDialog(questionNode, context, adapter, durationUnits, null);
                break;
            case "range":
                askPickerDialog(questionNode, context, adapter, type);
                break;
            case "frequency":
                askPickerUnitDialog(questionNode, context, adapter, frequencyUnits, doctorUnits);
                break;
        }
    }

    private static void askPickerDialog(final Node node, Activity context,
                                       final Object adapter, final String type) {
        final AlertDialog.Builder pickerDialog = new AlertDialog.Builder(context);
        pickerDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        pickerDialog.setView(convertView);
        final NumberPicker picker1 = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker picker2 = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);

        if (type.equalsIgnoreCase("range")) {
            middleText.setText(" - ");
            pickerDialog.setTitle(R.string.question_area_picker);
        } else if (type.equalsIgnoreCase("range")) {
            middleText.setText("X");
            pickerDialog.setTitle(R.string.question_area_picker);
        }

        picker1.setMinValue(0);
        picker1.setMaxValue(100);
        picker2.setMinValue(0);
        picker2.setMaxValue(100);

        pickerDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String middle = middleText.getText().toString();

                picker1.setValue(picker1.getValue());
                picker2.setValue(picker2.getValue());
                String durationString = String.valueOf(picker1.getValue()) + middle + picker2.getValue();
                node.addLanguage(" " + durationString);
                node.setLanguage(durationString);
                node.setSelected();
                if (adapter instanceof CustomExpandableListAdapter) {
                    ((CustomExpandableListAdapter) adapter).notifyDataSetChanged();
                } else if (adapter instanceof CustomArrayAdapter) {
                    ((CustomArrayAdapter) adapter).notifyDataSetChanged();
                }                dialog.dismiss();
            }
        });
        pickerDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        pickerDialog.show();
    }

    //Displays a dialog box with a text input
    //TODO: change the title of this to the title of the calling node
    private static void askText(final Node node, Activity context, final Object adapter) {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.addLanguage(dialogEditText.getText().toString());
                node.setLanguage(node.getLanguage());
                node.setSelected();
                if (adapter instanceof CustomExpandableListAdapter) {
                    ((CustomExpandableListAdapter) adapter).notifyDataSetChanged();
                } else if (adapter instanceof CustomArrayAdapter) {
                    ((CustomArrayAdapter) adapter).notifyDataSetChanged();
                }                dialog.dismiss();
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

    //Displays a calendar to choose a date from
    private static void askDate(final Node node, final Activity context, final Object adapter) {
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
                        node.addLanguage(dateString);
                        node.setLanguage(node.getLanguage());
                        node.setSelected();
                        if (adapter instanceof CustomExpandableListAdapter) {
                            ((CustomExpandableListAdapter) adapter).notifyDataSetChanged();
                        } else if (adapter instanceof CustomArrayAdapter) {
                            ((CustomArrayAdapter) adapter).notifyDataSetChanged();
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        datePickerDialog.show();
    }

    //Displays a number picker (the wheel thing)
    private static void askNumber(final Node node, Activity context, final Object adapter) {
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
                node.addLanguage(" " + value);
                node.setLanguage(value);
                node.setSelected();

                if (adapter instanceof CustomExpandableListAdapter) {
                    ((CustomExpandableListAdapter) adapter).notifyDataSetChanged();
                } else if (adapter instanceof CustomArrayAdapter) {
                    ((CustomArrayAdapter) adapter).notifyDataSetChanged();
                }

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


    //Displays a number picker and a unit picker
    private static void askPickerUnitDialog(final Node node, Activity context, final Object adapter,
                                           final String[] timeUnits, final String[] doctorUnits) {
        final AlertDialog.Builder pickerUnitDialog = new AlertDialog.Builder(context);
        if (doctorUnits != null) {
            pickerUnitDialog.setTitle(R.string.question_frequency_picker);
        } else {
            pickerUnitDialog.setTitle(R.string.question_duration_picker);
        }
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        pickerUnitDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        unitPicker.setDisplayedValues(timeUnits);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        pickerUnitDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString;
                if (doctorUnits != null) {
                    durationString = String.valueOf(quantityPicker.getValue()) + " " + doctorUnits[unitPicker.getValue()];
                } else {
                    durationString = String.valueOf(quantityPicker.getValue()) + " " + timeUnits[unitPicker.getValue()];
                }
                node.addLanguage(" " + durationString);
                node.setLanguage(durationString);
                node.setSelected();
                if (adapter instanceof CustomExpandableListAdapter) {
                    ((CustomExpandableListAdapter) adapter).notifyDataSetChanged();
                } else if (adapter instanceof CustomArrayAdapter) {
                    ((CustomArrayAdapter) adapter).notifyDataSetChanged();
                }                dialog.dismiss();
            }
        });
        pickerUnitDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        pickerUnitDialog.show();
    }


    /**
     * The subLevel questions refer to nodes which are contained within a level 3 node.
     * Take a look at the structure below:
     * Complaint {
     *     Level 1 {
     *         Level 2 {
     *              Level 3{
     *                  Level 4
     *              }
     *         }
     *         Level 2
     *         Level 2
     *     }
     * }
     *
     * Expandable lists only display 2 levels of information.
     * In our case, that would mean level 1 would be a question, and level 2 is the answer choices.
     * If the level 2 answer choice leads to yet another question, then this method should be called.
     * This method is similar to the question handler from before, but it works specifically with Level 3 or further level questions.
     *
     *
     * @param node The node that was selected.
     * @param context The current context.
     * @param callingAdapter The adapter that is holding the node.
     */
    public static void subLevelQuestion(final Node node, final Activity context, final CustomExpandableListAdapter callingAdapter){
        node.setSelected();
        List<Node> mNodes = node.getOptionsList();
        final CustomArrayAdapter adapter = new CustomArrayAdapter(context, R.layout.list_item_subquestion, mNodes);
        final AlertDialog.Builder subQuestion = new AlertDialog.Builder(context);

        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_subquestion, null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.dialog_subquestion_image_view);
        if(node.isAidAvailable()){
            if(node.getJobAidType().equals("image")){
                String drawableName = node.getJobAidFile();
                int resID = context.getResources().getIdentifier(drawableName, "drawable",  context.getPackageName());
                imageView.setImageResource(resID);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        subQuestion.setTitle(node.getText());
        ListView listView = (ListView) convertView.findViewById(R.id.dialog_subquestion_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setClickable(true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                node.getOption(position).toggleSelected();
                adapter.notifyDataSetChanged();
                if(node.getOption(position).getInputType() != null){
                    handleQuestion(node.getOption(position), context, adapter);
                }

                if(!node.getOption(position).isTerminal()){
                    subLevelQuestion(node.getOption(position), context, callingAdapter);
                }
            }
        });
        subQuestion.setView(listView);
        subQuestion.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.setLanguage(node.generateLanguage());
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


    /**
     * Nodes refer to the structure that is used for a decision tree or mindmap.
     * The node object is stored in the same structure where the there is a root node which contains all the sub-nodes.
     * The nodes are also tagged based on the attributes each JSON object shows.
     *
     * Most nodes are single choice questions. Therefore, they can just be clicked and selected.
     * Some nodes may be multi-choice, in which case there must be an attribute within the JSON to dictate that.
     *
     * text - the text that is displayed on the app to user
     * language - the text that is displayed after answering a question
     *            differs from the text attribute in that this is the response form of a question
     * inputType - dictates if the node is something other that choice-based
     *             types include: text, number, date, duration, area, range, frequency
     * physicalExams - any physical exams that should be triggered in the application if the node is selected
     * optionsList - container of sub-nodes of the current node
     * associatedComplaint - just like the name says
     * jobAidFile - the filename of the job aid
     *              should be stored in the physicalExamAssets folder within the app when compiling
     * jobAidType - options are audio, video, or image
     *
     * @param jsonNode
     * A JSON Object of a mindmap should be used here. The object that is generated will hold objects within it.
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

            this.language = jsonNode.optString("language");

            this.inputType = jsonNode.optString("input-type");

            this.physicalExams = jsonNode.optString("perform-physical-exam");
            if (!(this.physicalExams == null)) {
                this.complaint = true;
            } else {
                this.complaint = false;
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

            this.required = false;

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
        this.selected = false;
        this.required = source.required;
    }

    /**
     * Takes a JSON Array from a node and creates the sub-nodes to store within it.
     * This is how we handle recursive construction.
     * Nodes are stores within each other. This method is maintains good organizational structure, but makes it difficult to loop back to higher level nodes.
     * This is will be modified as the knowledge curating method is updated.
     *
     * The current structure of the knowledge, and the way it is stored here, is as follows"
     * Node 1 {
     *     Node 1.1 {
     *         Node 1.1.1
     *         Node 1.1.2
     *         Node 1.1.3
     *     }
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
        Log.d("Node", language);
        if (language.contains("_")) {
            language = language.replace("_", newText);
            Log.d("Node", language);
        } else {
            language = language + " " + newText;
            Log.d("Node", language);
        }
    }


    public int size() {
        return optionsList.size();
    }

    public boolean hasAssociations() {
        return hasAssociations;
    }

    public String getAssociatedComplaint() {
        return associatedComplaint;
    }

    public boolean isAidAvailable() {
        return aidAvailable;
    }

    public String getExams() {
        return physicalExams;
    }

    public List<Node> getOptionsList() {
        return optionsList;
    }

    public Node getOptionByName(String name) {
        Node foundNode = null;
        for (Node node : optionsList) {
            if (node.getText().equals(name)) {
                foundNode = node;
            }
        }
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
        if(!terminal){
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
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected()) {
                stringsList.add(mOptions.get(i).getLanguage());
                if (!mOptions.get(i).isTerminal()) {
                    stringsList.add(mOptions.get(i).formLanguage());
                }
            }
        }

        String languageSeparator = ", ";
        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {
            if (i == 0) {
                if (!stringsList.get(i).isEmpty()) {
                    mLanguage = mLanguage.concat(stringsList.get(i));
                }
            } else {
                if (!stringsList.get(i).isEmpty()) {
                    mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
                }
            }
        }
        //Log.d("Form language", mLanguage);
        return mLanguage;
    }

    public String generateLanguage() {
        String raw = this.formLanguage();
        String formatted;
        if (Character.toString(raw.charAt(0)).equals(",")) {
            formatted = raw.substring(2);
        } else {
            formatted = raw;
        }
        return formatted;
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

    public void removeOptionsList(){
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
}

