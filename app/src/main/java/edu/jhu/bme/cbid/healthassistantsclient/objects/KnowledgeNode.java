package edu.jhu.bme.cbid.healthassistantsclient.objects;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class KnowledgeNode {

    private String id;
    private String text;
    private String display;
    private String inputType;
    private String physicalExams;
    private boolean terminal;
    private List<KnowledgeNode> optionsList;
    private List associations;

    public KnowledgeNode() {
        this.id = null;
        this.text = null;
    }

    public KnowledgeNode(JSONObject jsonNode) {
        try {
            this.id = jsonNode.getString("id");
            Log.d("Starting ID", id.toString());
            this.text = jsonNode.getString("text");
            Log.d("Parent Node", text.toString());
            JSONArray optionsArray = jsonNode.optJSONArray("options");
            //Log.d("options", jsonArray.toString());

            if (optionsArray == null) {
                this.terminal = true;
                Log.d("Options are", "NULL");
            } else {
                Log.d("IF STATEMENT", "IM IN THIS SHIT");
                this.terminal = false;
                this.optionsList = createOptions(optionsArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private List<KnowledgeNode> createOptions(JSONArray jsonArray) {
        List<KnowledgeNode> createdOptions = new ArrayList<KnowledgeNode>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject current = jsonArray.getJSONObject(i);
                createdOptions.add(i, new KnowledgeNode(current));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return createdOptions;
    }
}
