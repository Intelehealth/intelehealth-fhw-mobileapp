package edu.jhu.bme.cbid.healthassistantsclient.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node {

    private String id;
    private String text;
    private String display;
    private String inputType;
    private String physicalExams;
    private boolean terminal;
    private List<Node> optionsList;
    private List associations;

    public Node(JSONObject jsonNode) {
        try {
            this.id = jsonNode.getString("id");
            this.text = jsonNode.getString("text");
            JSONArray optionsArray = jsonNode.optJSONArray("options");

            if (optionsArray == null) {
                this.terminal = true;
            } else {
                this.terminal = false;
                this.optionsList = createOptions(optionsArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private List<Node> createOptions(JSONArray jsonArray) {
        List<Node> createdOptions = new ArrayList<Node>();

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
}
