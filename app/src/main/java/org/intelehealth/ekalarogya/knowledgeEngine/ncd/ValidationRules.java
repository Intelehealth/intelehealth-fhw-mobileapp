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

    // if true then we can consider its an inbound logic and will check the validation rules to autofill the inner options data
    private boolean isSelfCheck;

    // this represent the type of action need to do after rules satisfied
    private String actionType;
    private List<Action> actionList;

    public ValidationRules(JSONObject jsonObject) {
        this.type = jsonObject.optString("type");
        this.sourceDataType = jsonObject.optString("source-data-type");
        this.sourceData = jsonObject.optString("source-data");
        this.check = jsonObject.optString("check");
        this.isSelfCheck = jsonObject.optBoolean("is-self-check");
        this.actionType = jsonObject.optString("action-type");

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

    public boolean isSelfCheck() {
        return isSelfCheck;
    }

    public void setSelfCheck(boolean selfCheck) {
        isSelfCheck = selfCheck;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}

class Action {
    private String ifCondition;
    private String thenCondition;
    private String data;
    private String popUpMessage;

    public Action(JSONObject jsonObject) {
        this.ifCondition = jsonObject.optString("IF");
        this.thenCondition = jsonObject.optString("THEN");
        this.data = jsonObject.optString("DATA");
        this.popUpMessage = jsonObject.optString("pop-up");
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

    public String getPopUpMessage() {
        return popUpMessage;
    }

    public void setPopUpMessage(String popUpMessage) {
        this.popUpMessage = popUpMessage;
    }
}