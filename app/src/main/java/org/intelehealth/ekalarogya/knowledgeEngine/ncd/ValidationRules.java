package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValidationRules {

    private String type;
    private String sourceDataType;
    private String sourceData;
    private String check;
    private List<Action> actionList;

    public ValidationRules(JSONObject jsonObject) {
        this.type = jsonObject.optString("type");
        this.sourceDataType = jsonObject.optString("source-data-type");
        this.sourceData = jsonObject.optString("source-data");
        this.check = jsonObject.optString("check");

        JSONArray tempArray = jsonObject.optJSONArray("action");
        if (tempArray != null) {
            this.actionList = getActions(tempArray);
        }
    }

    private List<Action> getActions(JSONArray jsonArray) {
        List<Action> actionsList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Action current = new Action(jsonArray.getJSONObject(i));
                actionsList.add(current);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return actionsList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getSourceData() {
        return sourceData;
    }

    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
    }
}

class Action {
    private String ifCondition;
    private String thenCondition;
    private String data;

    public Action(JSONObject jsonObject) {
        this.ifCondition = jsonObject.optString("IF");
        this.thenCondition = jsonObject.optString("THEN");
        this.data = jsonObject.optString("DATA");
    }

    public String getIfCondition() {
        return ifCondition;
    }

    public void setIfCondition(String ifCondition) {
        this.ifCondition = ifCondition;
    }

    public String getThenCondition() {
        return thenCondition;
    }

    public void setThenCondition(String thenCondition) {
        this.thenCondition = thenCondition;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}