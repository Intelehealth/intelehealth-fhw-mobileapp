package edu.jhu.bme.cbid.healthassistantsclient.objects;

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

    public Knowledge(JSONObject jsonObject) {
        super(jsonObject);
        /**
         * This is to create a master list of all the complaints stored in the database.
         * This allows for easier searching when looking for associated complaints.
         */
        this.mComplaints = gatherComplaints(this);

    }

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


    public Node getComplaint(String title) {
        Node foundComplaint = null;
        for (int i = 0; i < mComplaints.size(); i++) {
            if (mComplaints.get(i).text().equals(title)) {
                foundComplaint = mComplaints.get(i);
            }
        }
        return foundComplaint;
    }

    public void storeSelectedComplaint(String title) {
        for (int i = 0; i < mComplaints.size(); i++) {
            if (mComplaints.get(i).text() == title) {
                mComplaints.get(i).toggleSelected();
            }
        }
    }


    public ArrayList<String> getSelectedComplaints() {
        ArrayList<String> selectedComplaints = new ArrayList<>();
        for (int i = 0; i < mComplaints.size(); i++) {
            if (mComplaints.get(i).isSelected()) {
                selectedComplaints.add(mComplaints.get(i).text());
            }
        }
        return selectedComplaints;
    }

}
