package org.intelehealth.app.knowledgeEngine;

import org.intelehealth.app.utilities.CustomLog;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Physical Exam information class
 * The creation of this class was so that the original physical exam engine can be modified for each specific use of it.
 */

/**
 * Created by Amal Afroz Alam on 28, April, 2016.
 * Contact me: contact@amal.io
 */
public class PhysicalExam extends Node {

    private static final String TAG = PhysicalExam.class.getSimpleName();

    private ArrayList<String> selection;
    private List<Node> selectedNodes;
    private int totalExams;
    private List<String> pageTitles, pageTitlesLocale;
    private String engineVersionRoot;

    public PhysicalExam(JSONObject jsonObject, ArrayList<String> selection) {
        super(jsonObject);
        this.selection = selection;
        this.selectedNodes = matchSelections();
        this.totalExams = calculateTotal();
        this.pageTitles = determineTitles();
        this.pageTitlesLocale = determineTitlesLocale();

    }

    public PhysicalExam(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void refresh(ArrayList<String> selection) {
        this.selection = selection;
        this.selectedNodes = matchSelections();
        this.totalExams = calculateTotal();
        this.pageTitles = determineTitles();
        this.pageTitlesLocale = determineTitlesLocale();
    }

    public void refreshOnlyLocaleTitle() {
        this.pageTitlesLocale = determineTitlesLocale();
    }

    /**
     * When this object is first created, the constructor requires an input string of the exams for the current patient.
     * These exams are located first, and a copy of the original mind map is created using only those required exams.
     * If no exams are selected, the general exams are triggered instead.
     * Currently, exams are stored as follows:
     * Location Node 1 {
     * Exam Node 1 {
     * Question Node 1.1
     * Question Node 1.2
     * Question Node 1.3
     * }
     * }
     */
    private List<Node> matchSelections() {
        List<Node> newOptionsList = new ArrayList<>();
        List<String> foundLocations = new ArrayList<>();
        //Add the general ones into here first
        newOptionsList.add(getOption(0));
        getOption(0).setRequired(true);
        foundLocations.add(newOptionsList.get(0).getText());

        //TODO: Physical exam mind map needs to be modified to include required attribute
        if (getOption(0).getOptionsList() != null) {
            for (int i = 0; i < getOption(0).getOptionsList().size(); i++) {
                getOption(0).getOption(i).setRequired(true);
                if (getOption(0).getOption(i).getOptionsList() != null) {
                    for (int j = 0; j < getOption(0).getOption(i).getOptionsList().size(); j++) {
                        getOption(0).getOption(i).getOption(j).setRequired(true);
                    }
                }
            }


            //Find the other exams that need to be conducted and add them in
            if (selection == null || selection.isEmpty()) {
                //If no exams were required, just do the general ones
                return newOptionsList;
            } else {
                for (String current : selection) {
                    if (!current.trim().isEmpty()) {
                /*
                First, the selection texts are taken individually, and split up into location:exam
                The location knowledgeEngine is identified first, and then the exam nodes
                 */

                        if (current != null && !current.isEmpty()) {

                            String[] split = current.split(":");
                            if (split.length > 1) {
                                String location = split[0];
                                String exam = split[1];
                                if (location != null && !location.isEmpty() && exam != null && !exam.isEmpty()) {
                                    Node locationNodeRef = null;

                                    locationNodeRef = getOptionByName(location);

                                    Node examNodeRef = null;
                                    if (locationNodeRef != null) {
                                        CustomLog.i(TAG, "matchSelections: [Location]" + location);
                                        examNodeRef = locationNodeRef.getOptionByName(exam);
                                    }
                                    if (examNodeRef != null) {


                                        //The foundLocation list is to ensure that the same exam isn't display twice
                                        if (foundLocations.contains(location)) {
                                            int locationIndex = foundLocations.indexOf(location);
                                            Node foundLocationNode = newOptionsList.get(locationIndex);
                                            foundLocationNode.addOptions(new Node(examNodeRef));
                                        } else {
                                            //If it's a new exam, the location needs to be added to the list of things to check
                                            foundLocations.add(location);
                                            Node locationNode = new Node(locationNodeRef);
                                            locationNode.removeOptionsList();
                                            locationNode.addOptions(new Node(examNodeRef));
                                            newOptionsList.add(locationNode);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return newOptionsList;
    }

    public List<Node> getSelectedNodes() {
        return selectedNodes;
    }

    private int calculateTotal() {
        int examTotal = 0;
        for (Node node : selectedNodes) {
            for (Node node1 : node.getOptionsList()) {
                examTotal++;
            }
        }

        return examTotal;
    }

    private List<String> determineTitles() {
        List<String> titles = new ArrayList<>();

        for (Node node : selectedNodes) {
            for (Node subNode : node.getOptionsList()) {
                titles.add(node.getText() + " : " + subNode.getText());
            }
        }

        return titles;
    }

    private List<String> determineTitlesLocale() {
        List<String> titles = new ArrayList<>();

        for (Node node : selectedNodes) {
            for (Node subNode : node.getOptionsList()) {
                titles.add(node.findDisplay() + " : " + subNode.findDisplay());
            }
        }

        return titles;
    }

    public int getTotalNumberOfExams() {
        return totalExams;
    }

    public List<String> getAllTitles() {
        return pageTitles;
    }

    public String getTitle(int index) {
        return pageTitles.get(index);
    }

    /**
     * Once the list of exams has been generated, this is used to populate the views for each exam.
     *
     * @param index View number
     * @return Exam for that particular view
     */
    public Node getExamNode(int index) {

        Node lvlTwoNode = null;

        String title = getTitle(index);
        String[] split = title.split(" : ");
        String levelOne = split[0];
        String levelTwo = split[1];

        for (Node selectedNode : selectedNodes) {
            if (selectedNode.getText().equals(levelOne)) {
                for (Node node : selectedNode.getOptionsList()) {
                    if (node.getText().equals(levelTwo)) {
                        lvlTwoNode = node;
                    }
                }
            }
        }

        return lvlTwoNode;

    }

    public String getExamParentNodeName(int index) {

        String title = getTitle(index);
        String[] split = title.split(" : ");

        String parent_node = split[0];

        for (Node selectedNode : selectedNodes) {
            if (selectedNode.getText().equals(split[0])) {
                parent_node = selectedNode.findDisplay();
              //  parent_node = selectedNode.getText();
            }
        }
        return parent_node;

    }

    public String getExamParentNodeName_NAS(int index) {
        String title = getTitle(index);
        String[] split = title.split(" : ");

        String parent_node = split[0];

        for (Node selectedNode : selectedNodes) {
            if (selectedNode.getText().equals(split[0])) {
                parent_node = selectedNode.getText();
            }
        }
        return parent_node;

    }

    //Check to see if all required exams have been answered before moving on.
    public boolean areRequiredAnswered() {

        boolean allAnswered = true;

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);
            if (node.isRequired() && !node.anySubSelected()) {
                allAnswered = false;
                break;
            }
        }
        return allAnswered;
    }

    /**
     *
     * @param node
     */
    private void cleanUpTheImages(Node node){
        if(!node.isDataCaptured()){
            for (int i = 0; i < node.getImagePathList().size(); i++) {
                String image = node.getImagePathList().get(i);
                getImagePathList().remove(image);
            }
        }
    }
    //TODO: Physical exam map needs to modified to make language generation easier.
    public String generateFindings() {
        String mLanguage = "";
        Set<String> rootStrings = new HashSet<>();
        List<String> stringsList = new ArrayList<>();

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);

            String title = getTitle(i);
            String[] split = title.split(" : ");
            String levelOne = split[0];
            if ((node.isSelected() | node.anySubSelected())) {
                boolean checkSet = rootStrings.add(levelOne);
                cleanUpTheImages(node);
                if (checkSet)
                    stringsList.add(bullet_arrow + "<b>" + levelOne + ": " + "</b>" + bullet + " " + node.getLanguage());
                else stringsList.add(bullet + " " + node.getLanguage());
                if (!node.isTerminal()) {
                    String lang = node.formLanguage();
                    CustomLog.i(TAG, "generateFindings: " + lang);
                    stringsList.add(lang);
                }
            }
        }


        String languageSeparator = next_line;

        for (int i = 0; i < stringsList.size(); i++) {
            mLanguage = mLanguage.concat(stringsList.get(i) + languageSeparator);
//            if (i == 0) {
//                if (!stringsList.get(i).isEmpty()) {
//                    mLanguage = mLanguage.concat(stringsList.get(i));
//                }
//            } else {
//                if (!stringsList.get(i).isEmpty()) {
//                    mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
//                }
//            }
        }

//        mLanguage = removeCharsFindings(mLanguage);
        mLanguage = mLanguage.replaceAll("\\. -", ".");
        mLanguage = mLanguage.replaceAll("\\.", "\\. ");
        mLanguage = mLanguage.replaceAll("\\: -", "\\: ");
        mLanguage = mLanguage.replaceAll("% - ", "");
        mLanguage = mLanguage.replace(next_line, "-");
        mLanguage = mLanguage.replaceAll("-" + bullet, next_line + bullet);
        mLanguage = mLanguage.replaceAll(bullet_arrow + "<b>", next_line + bullet_arrow + "<b>");
        mLanguage = mLanguage.replaceAll("-" + "<b>", next_line + "<b>");
        mLanguage = mLanguage.replaceAll("</b>" + bullet, "</b>" + next_line + bullet);

        if (StringUtils.right(mLanguage, 2).equals(" -")) {
            mLanguage = mLanguage.substring(0, mLanguage.length() - 2);
        }

        mLanguage = mLanguage.replaceAll("%-", " ");
        return mLanguage;
    }

    public String generateFindingsByLocale(String localeCode) {
        String mLanguage = "";
        Set<String> rootStrings = new HashSet<>();
        List<String> stringsList = new ArrayList<>();

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);
            CustomLog.v(TAG, "getExamNode - " + node.toString());

            String title = getPageTitlesLocale().get(i);
            CustomLog.v(TAG, "getPageTitlesLocale - " + node.toString());
            String[] split = title.split(" : ");
            String levelOne = split[0];
            CustomLog.v(TAG, "levelOne - " + levelOne);
            if ((node.isSelected() | node.anySubSelected())) {
                cleanUpTheImages(node);
                boolean checkSet = rootStrings.add(levelOne);
                CustomLog.i(TAG, "rootStrings: " + rootStrings);
                if (checkSet)
                    //stringsList.add(bullet_arrow+"<b>"+levelOne + ": "+"</b>" + bullet + " " + node.getLanguage());
                    stringsList.add(bullet_arrow + "<b>" + levelOne + ": " + "</b>" + bullet + " " + node.findDisplay());
                    //else stringsList.add(bullet + " " + node.getLanguage());
                else stringsList.add(bullet + " " + node.findDisplay());
                CustomLog.i(TAG, "stringsList: " + stringsList);
                if (!node.isTerminal()) {
                    //String lang = node.formLanguage();
                    String lang = node.formQuestionAnswer(0, false);
                    CustomLog.i(TAG, "generateFindings: " + lang);
                    stringsList.add(lang);
                    CustomLog.i(TAG, "Not isTerminal - stringsList: " + stringsList);
                }

            }
        }


        String languageSeparator = next_line;

        for (int i = 0; i < stringsList.size(); i++) {
            mLanguage = mLanguage.concat(stringsList.get(i) + languageSeparator);
//            if (i == 0) {
//                if (!stringsList.get(i).isEmpty()) {
//                    mLanguage = mLanguage.concat(stringsList.get(i));
//                }
//            } else {
//                if (!stringsList.get(i).isEmpty()) {
//                    mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
//                }
//            }
        }

//        mLanguage = removeCharsFindings(mLanguage);
        mLanguage = mLanguage.replaceAll("\\. -", ".");
        mLanguage = mLanguage.replaceAll("\\.", "\\. ");
        mLanguage = mLanguage.replaceAll("\\: -", "\\: ");
        mLanguage = mLanguage.replaceAll("% - ", "");
        mLanguage = mLanguage.replace(next_line, "-");
        mLanguage = mLanguage.replaceAll("-" + bullet, next_line + bullet);
        mLanguage = mLanguage.replaceAll("-" + "<b>", next_line + "<b>");
        mLanguage = mLanguage.replaceAll("</b>" + bullet, "</b>" + next_line + bullet);

        if (StringUtils.right(mLanguage, 2).equals(" -")) {
            mLanguage = mLanguage.substring(0, mLanguage.length() - 2);
        }

        mLanguage = mLanguage.replaceAll("%-", " ");
        return mLanguage;
    }


    private String removeCharsFindings(String raw) {
        String formatted;
        if (Character.toString(raw.charAt(0)).equals(",")) {
            formatted = raw.substring(2);
        } else {
            formatted = raw;
        }
        return formatted;
    }


    public List<String> getPageTitlesLocale() {
        return pageTitlesLocale;
    }

    public void setPageTitlesLocale(List<String> pageTitlesLocale) {
        this.pageTitlesLocale = pageTitlesLocale;
    }
    /*Node Engine - 3.0 support with new UI*/
    public String getEngineVersion() {
        return engineVersionRoot;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersionRoot = engineVersion;
    }

    /*End*/

}
