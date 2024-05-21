package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides action generation logic for nodes using the validation rules.
 * Created by Lincon Pradhan on 13th May, 2024
 * Contact me: lincon@intelehealth.org
 */
public class ActionLogic {
    public static ActionResult foundNextTargetNodeText(List<SourceData> sourceDataInfoValueList, List<CheckInfoData> checkInfoDataList, List<Action> actionList) {
        ActionResult actionResult = new ActionResult();

        for (int i = 0; i < actionList.size(); i++) {
            Action action = actionList.get(i);
            String conditionStatement = action.getIfCondition();
            // check for multiple attribute required
            if (checkInfoDataList.size() > 1) {
                //Ex: "check": "AGE[GREATER_THAN]AND-GENDER[EQUAL]"
                // "IF": "11-MALE"
                String[] targetValues = conditionStatement.split("-");
                List<Boolean> conditionsPassStatuList = new ArrayList<>();
                List<String> associateOperatorList = new ArrayList<>();
                for (int j = 0; j < targetValues.length; j++) {
                    CheckInfoData checkInfoData = checkInfoDataList.get(j);
                    SourceData sourceData = sourceDataInfoValueList.get(j);
                    if (checkInfoData.isHavingAssociateCondition())
                        associateOperatorList.add(checkInfoData.getAssociateOperator());
                    if (sourceData.getDataType().equals(ValidationConstants.INTEGER)) {
                        int valA = Integer.parseInt(sourceData.getValue());
                        int valB = Integer.parseInt(targetValues[j]);
                        if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_GREATER_THAN)) {
                            // pass condition
                            conditionsPassStatuList.add(valA > valB);

                        } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_LESS_THAN)) {
                            // pass condition
                            conditionsPassStatuList.add(valA < valB);

                        } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_GREATER_THAN_EQUAL)) {
                            // pass condition
                            conditionsPassStatuList.add(valA >= valB);

                        } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_LESS_THAN_EQUAL)) {

                            // pass condition
                            conditionsPassStatuList.add(valA <= valB);

                        } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_EQUAL)) {

                            // pass condition
                            conditionsPassStatuList.add(valA == valB);

                        }
                    } else if (sourceData.getDataType().equals(ValidationConstants.STRING)) {
                        String valA = sourceData.getValue();
                        String valB = targetValues[j];
                        if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_EQUAL)) {

                            // pass condition
                            conditionsPassStatuList.add(valA.equals(valB));

                        }
                    }
                }
                boolean isFinalPass = false;
                for (int j = 0; j < conditionsPassStatuList.size(); j++) {
                    boolean isFinalItem = j == conditionsPassStatuList.size() - 1;
                    if (isFinalItem) break;
                    if (associateOperatorList.isEmpty()) {
                        isFinalPass = conditionsPassStatuList.get(j);
                        break;
                    } else {
                        if (associateOperatorList.get(j).equals(ValidationConstants.CHECK_AND)) {
                            isFinalPass = conditionsPassStatuList.get(j) && conditionsPassStatuList.get(j + 1);
                        } else if (associateOperatorList.get(j).equals(ValidationConstants.CHECK_OR)) {
                            isFinalPass = conditionsPassStatuList.get(j) || conditionsPassStatuList.get(j + 1);
                        }
                    }


                }
                if (isFinalPass) {
                    actionResult.setTarget(action.getThenCondition());
                    actionResult.setTargetData(action.getData());
                    actionResult.setInsideNodeDataUpdate(action.getData() == null);
                    return actionResult;
                }


            } else {
                //Ex: "check": "EQUAL"
                // "IF": "Refer to DH/CHC/RH",
            }
        }


        return null;
    }
}
