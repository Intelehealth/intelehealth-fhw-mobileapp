package edu.jhu.bme.cbid.healthassistantsclient.objects;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node implements Serializable{

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

