package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides action generation logic for nodes using the validation rules.
 * Created by Lincon Pradhan on 13th May, 2024
 * Contact me: lincon@intelehealth.org
 */
public class ActionLogic {
    public static ActionResult foundActionResult(List<SourceData> sourceDataInfoValueList, List<CheckInfoData> checkInfoDataList, List<Action> actionList) {
        ActionResult actionResult = new ActionResult();

        for (int i = 0; i < actionList.size(); i++) {
            Action action = actionList.get(i);
            String conditionStatement = action.getIfCondition();

            String prefix = conditionStatement.substring(0, 3);
            String regex = "^C\\d\\($";
            // Compile the pattern
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(prefix);
            boolean matches = matcher.matches();
            String checkSec = "";

            if (matches) {
                checkSec = prefix.substring(0, 2);
                conditionStatement = conditionStatement.substring(3, conditionStatement.length() - 1);
            }
            List<CheckInfoData> finalCheckInfoDataList = new ArrayList<>();
            for (int j = 0; j < checkInfoDataList.size(); j++) {
                CheckInfoData temp = checkInfoDataList.get(j);
                if(temp.getCheckSectionName().equals(checkSec)){
                    finalCheckInfoDataList.add(temp);
                }
            }
            if(finalCheckInfoDataList.isEmpty()){
                finalCheckInfoDataList = checkInfoDataList;
            }

            // check for multiple attribute required
            if (finalCheckInfoDataList.size() > 1) {
                //Ex: "check": "AGE[GREATER_THAN]AND-GENDER[EQUAL]"
                // "IF": "11-MALE"
                String[] targetValues = conditionStatement.split("-");
                List<Boolean> conditionsPassStatuList = new ArrayList<>();
                List<String> associateOperatorList = new ArrayList<>();
                for (int j = 0; j < targetValues.length; j++) {
                    CheckInfoData checkInfoData = finalCheckInfoDataList.get(j);
                    SourceData sourceData = sourceDataInfoValueList.size() == 1 ? sourceDataInfoValueList.get(0) : sourceDataInfoValueList.get(j);
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
                    } else if (sourceData.getDataType().equals(ValidationConstants.DOUBLE)) {
                        double valA = Double.parseDouble(sourceData.getValue());
                        double valB = Double.parseDouble(targetValues[j]);
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
                if (associateOperatorList.isEmpty()) {
                    isFinalPass = conditionsPassStatuList.get(0);

                } else {
                    // check all are one type or mixed
                    Set<String> tempSet = new HashSet<>(associateOperatorList);
                    if (tempSet.size() == 1) {
                        if (tempSet.toArray()[0].equals(ValidationConstants.CHECK_OR)) {
                            isFinalPass = conditionsPassStatuList.contains(true);


                        } else if (tempSet.toArray()[0].equals(ValidationConstants.CHECK_AND)) {
                            isFinalPass = !conditionsPassStatuList.contains(false);
                        }
                    } else {
                        for (int j = 0; j < conditionsPassStatuList.size(); j++) {
                            boolean isFinalItem = j == conditionsPassStatuList.size() - 1;
                            if (isFinalItem) break;

                            if (associateOperatorList.get(j).equals(ValidationConstants.CHECK_AND)) {
                                isFinalPass = conditionsPassStatuList.get(j) && conditionsPassStatuList.get(j + 1);
                            } else if (associateOperatorList.get(j).equals(ValidationConstants.CHECK_OR)) {
                                isFinalPass = conditionsPassStatuList.get(j) || conditionsPassStatuList.get(j + 1);
                            }

                        }
                    }

                }
                if (isFinalPass) {
                    actionResult.setTarget(action.getThenCondition());
                    actionResult.setTargetData(action.getData());
                    actionResult.setInsideNodeDataUpdate(action.getData() == null);
                    actionResult.setPopupMessage(action.getPopUpMessage());
                    return actionResult;
                }


            } else {
                //Ex: "check": "EQUAL"
                // "IF": "Refer to DH/CHC/RH",
            }
        }


        return null;
    }

    public static ActionResult foundGeneralSingleMatching(SourceData sourceData, CheckInfoData checkInfoData, List<Action> actionList) {
        ActionResult actionResult = new ActionResult();
        boolean isFinalPass = false;
        for (int i = 0; i < actionList.size(); i++) {
            Action action = actionList.get(i);
            String conditionStatement = action.getIfCondition();


            if (sourceData.getDataType().equals(ValidationConstants.INTEGER)) {
                int valA = Integer.parseInt(sourceData.getValue());
                int valB = Integer.parseInt(conditionStatement);
                if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_GREATER_THAN)) {
                    // pass condition
                    isFinalPass = valA > valB;


                } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_LESS_THAN)) {
                    // pass condition
                    isFinalPass = valA < valB;
                } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_GREATER_THAN_EQUAL)) {
                    // pass condition
                    isFinalPass = valA >= valB;


                } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_LESS_THAN_EQUAL)) {

                    // pass condition
                    isFinalPass = valA <= valB;

                } else if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_EQUAL)) {

                    // pass condition
                    isFinalPass = valA == valB;

                }
            } else if (sourceData.getDataType().equals(ValidationConstants.STRING)) {
                String valA = sourceData.getValue();
                if (checkInfoData.getCondition().equals(ValidationConstants.CHECK_EQUAL)) {

                    // pass condition
                    isFinalPass = valA.equals(conditionStatement);

                }
            }


            if (isFinalPass) {
                actionResult.setTarget(action.getThenCondition());
                actionResult.setTargetData(action.getData());
                actionResult.setInsideNodeDataUpdate(action.getData() == null);
                return actionResult;
            }


        }


        return null;
    }
}
