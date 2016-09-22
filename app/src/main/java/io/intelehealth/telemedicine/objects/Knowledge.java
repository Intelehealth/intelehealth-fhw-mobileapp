package io.intelehealth.telemedicine.objects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amal Afroz Alam on 24, April, 2016.
 * Contact me: contact@amal.io
 */
public class Knowledge extends Node {

    private String LOG_TAG = "Knowledge Class";
    private List<Node> mComplaints;
    private int selectedCounter = 0;

    /**
     * This is just for the specific use case for the knowledge engine.
     * This particular object as created as a way to gather all selected complaint nodes.
     * @param jsonObject
     */
    public Knowledge(JSONObject jsonObject) {
        super(jsonObject);
        /**
         * This is to create a master list of all the complaints stored in the database.
         * This allows for easier searching when looking for associated complaints.
         */
        this.mComplaints = gatherComplaints(this);

    }

    public List<Node> getComplaints() {
        return mComplaints;
    }

    /**
     * This method was originally to make a way to compile a master list of complaints.
     * This has become redundant due to the change in the way the knowledge engine is used.
     * Now that first level nodes are always complaints, this is not necessary to do.
     * @param node
     * @return
     */
    private List<Node> gatherComplaints(Node node) {
        List<Node> foundComplaints = new ArrayList<>();
        List<Node> mOptions = node.getOptionsList();
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isComplaint()) {
                foundComplaints.add(mOptions.get(i));
                //Log.d(LOG_TAG, mOptions.get(i).text());
            } else if (!mOptions.get(i).isTerminal()) {
                foundComplaints.addAll(gatherComplaints(mOptions.get(i)));
            }
        }
        return foundComplaints;
    }

    //Get the specific complaint that was given.
    //Used when searching for an associated complaint.
    public Node getComplaint(String title) {
        Node foundComplaint = null;
        for (int i = 0; i < mComplaints.size(); i++) {
            if (mComplaints.get(i).getText().equals(title)) {
                foundComplaint = mComplaints.get(i);
            }
        }
        return foundComplaint;
    }

    //Store the complaint nodes that were selected.
    public void storeSelectedComplaint(String title) {
        for (int i = 0; i < mComplaints.size(); i++) {
            if (mComplaints.get(i).getText() == title) {
                mComplaints.get(i).toggleSelected();
            }
        }
    }

    public ArrayList<String> getSelectedComplaints() {
        ArrayList<String> selectedComplaints = new ArrayList<>();
        for (int i = 0; i < mComplaints.size(); i++) {
            if (mComplaints.get(i).isSelected()) {
                selectedComplaints.add(mComplaints.get(i).getText());
            }
        }
        return selectedComplaints;
    }

}
