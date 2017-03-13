package io.intelehealth.client.objects;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical Exam information class
 * The creation of this class was so that the original physical exam engine can be modified for each specific use of it.
 */

/**
 * Created by Amal Afroz Alam on 28, April, 2016.
 * Contact me: contact@amal.io
 */
public class PhysicalExam extends Node {

    private ArrayList<String> selection;
    private List<Node> selectedNodes;
    private int totalExams;
    private List<String> pageTitles;

    public PhysicalExam(JSONObject jsonObject, ArrayList<String> selection) {
        super(jsonObject);
        this.selection = selection;
        this.selectedNodes = matchSelections();
        this.totalExams = calculateTotal();
        this.pageTitles = determineTitles();

    }

    /**
     * When this object is first created, the constructor requires an input string of the exams for the current patient.
     * These exams are located first, and a copy of the original mind map is created using only those required exams.
     * If no exams are selected, the general exams are triggered instead.
     * Currently, exams are stored as follows:
     * Location Node 1 {
     *     Exam Node 1 {
     *         Question Node 1.1
     *         Question Node 1.2
     *         Question Node 1.3
     *     }
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

        for (int i = 0; i < getOption(0).getOptionsList().size(); i++) {
            getOption(0).getOption(i).setRequired(true);
            for (int j = 0; j < getOption(0).getOption(i).getOptionsList().size(); j++) {
                getOption(0).getOption(i).getOption(j).setRequired(true);
            }
        }


        //Find the other exams that need to be conducted and add them in
        if (selection == null || selection.isEmpty()) {
            //If no exams were required, just do the general ones
            return newOptionsList;
        } else {
            for (int i = 0; i < selection.size(); i++) {

                /*
                First, the selection texts are taken individually, and split up into location:exam
                The location node is identified first, and then the exam nodes
                 */

                String current = selection.get(i);
                Log.d("Exam current ", current);
                String[] split = current.split(":");
                String location = split[0];
                Log.d("Exam location ", location);
                String exam = split[1];
                Log.d("Exam exam ", exam);
                Node locationNodeRef = getOptionByName(location);
                Log.d("Exam locRef", locationNodeRef.getText());
                Node examNodeRef = locationNodeRef.getOptionByName(exam);
                Log.d("Exam examRef", examNodeRef.getText());

                //The foundLocation list is to ensure that the same exam isn't display twice
                if (foundLocations.contains(location)) {
                    Log.d("Exam if", "location in foundLocations");
                    int locationIndex = foundLocations.indexOf(location);
                    Node foundLocationNode = newOptionsList.get(locationIndex);
                    foundLocationNode.addOptions(new Node(examNodeRef));
                } else {

                    //If it's a new exam, the location needs to be added to the list of things to check
                    Log.d("Exam if", "not found");
                    foundLocations.add(location);
                    Node locationNode = new Node(locationNodeRef);
                    locationNode.removeOptionsList();
                    locationNode.addOptions(new Node(examNodeRef));
                    newOptionsList.add(locationNode);
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

    //Check to see if all required exams have been answered before moving on.
    public boolean areRequiredAnswered() {

        boolean allAnswered = true;

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);
            if(node.isRequired() && !node.anySubSelected()){
                allAnswered = false;
                break;
            }
        }
        return allAnswered;
    }

    //TODO: Physical exam map needs to modified to make language generation easier.
    public String generateFindings() {
        String mLanguage = "";
        List<String> stringsList = new ArrayList<>();

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);
            if (node.isSelected() | node.anySubSelected()) {
//                Log.d(node.getText(), node.getLanguage());
                stringsList.add(node.getLanguage());
                if (!node.isTerminal()) {
                    stringsList.add(node.formLanguage());
                }
            }
            mLanguage = mLanguage + node.getLanguage();
        }


        String languageSeparator = ", ";

        for (int i = 0; i < stringsList.size(); i++) {
            mLanguage = mLanguage.concat(stringsList.get(i));
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

        mLanguage = removeCharsFindings(mLanguage);

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

}
