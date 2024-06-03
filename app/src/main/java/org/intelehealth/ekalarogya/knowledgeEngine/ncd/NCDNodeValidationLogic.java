package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import android.content.Context;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * This class provides validation logic for nodes after the user action.
 * It ensures that nodes meet certain criteria before they are moving to the next.
 * Created by Lincon Pradhan on 9th May, 2024
 * Contact me: lincon@intelehealth.org
 */
public class NCDNodeValidationLogic {
    public static final String TAG = NCDNodeValidationLogic.class.getSimpleName();

    /**
     * Validates the node logic from the specified node and
     * finds the next path, considering the root node update with autofill data if any.
     *
     * @param mmRootNode
     * @param selectedNode
     * @return An instance of NCDValidationResult representing the result of the validation.
     */
    public static NCDValidationResult validateAndFindNextPath(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, boolean isInboundRequest, ActionResult previousActionResult) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        boolean isFlowEnd = selectedNode.getFlowEnd();
        ValidationRules validationRules = selectedNode.getValidationRules();
        if (!isInboundRequest && validationRules == null)
            for (int i = 0; i < selectedNode.getOptionsList().size(); i++) {
                if (selectedNode.getOptionsList().get(i).isSelected()) {
                    validationRules = selectedNode.getOptionsList().get(i).getValidationRules();
                }
            }

        if (validationRules != null) {
            if (validationRules.getType().equalsIgnoreCase("NAVIGATE")) {
                return checkForNavigationType(context, patientUUID, mmRootNode, selectedRootIndex, selectedNode, validationRules);
            } else if (validationRules.getType().equalsIgnoreCase("RANGE_TEXT")) {
                return checkForRangeTextType(context, patientUUID, mmRootNode, selectedRootIndex, selectedNode, validationRules, isInboundRequest, previousActionResult);
            } else if (validationRules.getType().equalsIgnoreCase("TEXT_DATE")) {
                return checkForTextDateType(context, patientUUID, mmRootNode, selectedRootIndex, selectedNode, validationRules, isInboundRequest, previousActionResult);

            } else if (validationRules.getType().equalsIgnoreCase("RECURRING_NUMBER_SET")) {
                return checkForRecurringNumberSetType(context, patientUUID, mmRootNode, selectedRootIndex, selectedNode, validationRules, isInboundRequest, previousActionResult);

            }
        } else {
            if (isFlowEnd) {
                // need to remove the other nodes from root
                List<Node> temp = new ArrayList<Node>();
                for (int i = 0; i < mmRootNode.getOptionsList().size(); i++) {
                    if (i <= selectedRootIndex) {
                        temp.add(mmRootNode.getOptionsList().get(i));
                    } else {
                        break;
                    }
                }
                mmRootNode.setOptionsList(temp);
                ncdValidationResult.setTargetNodeID(null);
                ncdValidationResult.setReadyToEndTheScreening(true);
                ncdValidationResult.setUpdatedNode(mmRootNode);
            } else {
                ncdValidationResult.setTargetNodeID(null);
                ncdValidationResult.setReadyToEndTheScreening(false);
                for (int i = 0; i < mmRootNode.getOptionsList().size(); i++) {
                    if (i >= selectedRootIndex)
                        mmRootNode.getOptionsList().get(i).setHidden(false);
                }
                ncdValidationResult.setUpdatedNode(mmRootNode);
            }
        }

        return ncdValidationResult;
    }

    /**
     * @param mmRootNode
     * @param selectedRootIndex
     * @param selectedNode
     * @return
     */
    private static NCDValidationResult checkForNavigationType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, ValidationRules validationRules) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        String rulesType = validationRules.getType();
        String sourceDataType = validationRules.getSourceDataType();
        String sourceDataNameType = validationRules.getSourceData();
        List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataNameType);
        if (sourceDataInfoList != null) {
            List<SourceData> sourceDataInfoValueList = new ArrayList<>();
            if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_PATIENT_ATTRIBUTE)) {
                sourceDataInfoValueList = DataSourceManager.getValuesForDataSourceFromPatientAttributes(sourceDataInfoList, patientUUID);
            } /*else if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_RANGE_NODE_VAL)) {
                sourceDataInfoValueList = DataSourceManager.getValuesForDataSourceFromTargetNode(sourceDataInfoList, mmRootNode, patientUUID);
            }*/
            List<CheckInfoData> checkInfoDataList = ValidationRulesParser.getCheckInfoList(validationRules.getCheck());
            ActionResult actionResult = ActionLogic.foundActionResult(sourceDataInfoValueList, checkInfoDataList, validationRules.getActionList());
            if (actionResult == null) {
                ncdValidationResult.setTargetNodeID(null);
                ncdValidationResult.setReadyToEndTheScreening(false);
                ncdValidationResult.setUpdatedNode(mmRootNode);
            } else {
                for (int i = 0; i < mmRootNode.getOptionsList().size(); i++) {
                    Node tempNode = mmRootNode.getOptionsList().get(i);
                    if (i != selectedRootIndex) {
                        if (actionResult.getTarget().equalsIgnoreCase(tempNode.getText())) {
                            // found the target node
                            mmRootNode.getOptionsList().get(i).setHidden(false);
                            ncdValidationResult.setTargetNodeID(mmRootNode.getOptionsList().get(i).getId());
                            // check if it required the autofill for target node
                            // "is-auto-fill": true,
                            if (tempNode.getAutoFill()) {
                                // need to again parse the
                                NCDValidationResult targetNcdValidationResult = validateAndFindNextPath(context, patientUUID, mmRootNode, i, tempNode, true, actionResult);
                                mmRootNode = targetNcdValidationResult.getUpdatedNode();
                            }
                        } else {
                            mmRootNode.getOptionsList().get(i).setHidden(true);
                            mmRootNode.getOptionsList().get(i).setSelected(false);
                            mmRootNode.getOptionsList().get(i).setDataCapture(false);
                            mmRootNode.getOptionsList().get(i).unselectAllNestedNode();
                        }
                    } else {
                        mmRootNode.getOptionsList().get(i).setHidden(false);
                    }

                }
                ncdValidationResult.setReadyToEndTheScreening(false);
                ncdValidationResult.setUpdatedNode(mmRootNode);

            }


        } else {
            // something went wrong
            Timber.tag(TAG).v(context.getString(R.string.something_went_wrong));
        }

        return ncdValidationResult;
    }

    /**
     * @param mmRootNode
     * @param selectedRootIndex
     * @param selectedNode
     * @param validationRules
     * @param isInboundRequest
     * @param previousActionResult
     * @return
     */
    private static NCDValidationResult checkForRangeTextType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, ValidationRules validationRules, boolean isInboundRequest, ActionResult previousActionResult) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        String rulesType = validationRules.getType();
        String sourceDataType = validationRules.getSourceDataType();
        String sourceDataNameType = validationRules.getSourceData();
        //List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataNameType);
        // if (sourceDataInfoList != null) {
        List<SourceData> sourceDataInfoValueList = new ArrayList<>();
        if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_NODE_VAL_DOUBLE)) {
            //List<SourceData> sourceDataInfoList = new ArrayList<>();
            SourceData sourceData = new SourceData();
            sourceData.setDataType(ValidationConstants.DOUBLE);
            sourceData.setDataName(sourceDataNameType);

            //if (sourceDataInfoList.size() == 1)
            sourceDataInfoValueList.add(DataSourceManager.getValuesForDataSourceFromTargetNode(sourceData, mmRootNode));
        } /*else if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_RANGE_NODE_VAL)) {
                sourceDataInfoValueList = DataSourceManager.getValuesForDataSourceFromTargetNode(sourceDataInfoList, mmRootNode, patientUUID);
            }*/
        List<CheckInfoData> checkInfoDataList = ValidationRulesParser.getCheckInfoList(validationRules.getCheck());

        ActionResult actionResult = ActionLogic.foundActionResult(sourceDataInfoValueList, checkInfoDataList, validationRules.getActionList());
        if (actionResult == null) {
            ncdValidationResult.setTargetNodeID(null);
            ncdValidationResult.setReadyToEndTheScreening(false);
            ncdValidationResult.setUpdatedNode(mmRootNode);
        } else {
            if (validationRules.isSelfCheck()) {
                for (int i = 0; i < mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().size(); i++) {
                    if (mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(i).getText().equalsIgnoreCase(actionResult.getTarget())) {
                        mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(i).setSelected(true);
                        mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(i).setDataCapture(true);
                        mmRootNode.getOptionsList().get(selectedRootIndex).setSelected(true);

                    } else {
                        mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(i).setSelected(false);
                        mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(i).setDataCapture(false);
                    }
                }
            }
            /*for (int i = 0; i < mmRootNode.getOptionsList().size(); i++) {
                Node tempNode = mmRootNode.getOptionsList().get(i);
                if (i != selectedRootIndex) {
                    if (actionResult.getTarget().equalsIgnoreCase(tempNode.getText())) {
                        // found the target node
                        mmRootNode.getOptionsList().get(i).setHidden(false);
                        ncdValidationResult.setTargetNodeID(mmRootNode.getOptionsList().get(i).getId());
                        // check if it required the autofill for target node
                        // "is-auto-fill": true,
                        if (tempNode.getAutoFill()) {
                            // need to again parse the
                            NCDValidationResult targetNcdValidationResult = validateAndFindNextPath(context, patientUUID, mmRootNode, i, tempNode, true, actionResult);
                            mmRootNode = targetNcdValidationResult.getUpdatedNode();
                        }
                    } else {
                        mmRootNode.getOptionsList().get(i).setHidden(true);
                    }
                } else {
                    mmRootNode.getOptionsList().get(i).setHidden(false);
                }

            }*/
            ncdValidationResult.setReadyToEndTheScreening(false);
            ncdValidationResult.setUpdatedNode(mmRootNode);

        }


       /* } else {
            // something went wrong
            Timber.tag(TAG).v(context.getString(R.string.something_went_wrong));
        }*/

        return ncdValidationResult;
    }

    private static NCDValidationResult checkForTextDateType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, ValidationRules validationRules, boolean isInboundRequest, ActionResult previousActionResult) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        String rulesType = validationRules.getType();
        String sourceDataType = validationRules.getSourceDataType();
        String sourceDataNameType = validationRules.getSourceData();
        List<CheckInfoData> checkInfoDataList = ValidationRulesParser.getCheckInfoList(validationRules.getCheck());
        // if (isInboundRequest) {
        // no need to check the source data bcz we have already the value in previousActionResult object
        if (validationRules.getActionType().equals(ValidationConstants.ACTION_TYPE_SET_SINGLE_OPTION_VALUE)) {
            if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_NODE_VAL_STR)) {
                SourceData sourceData = new SourceData();
                sourceData.setDataType(ValidationConstants.STRING);
                sourceData.setDataName(sourceDataNameType);
                if (isInboundRequest) {
                    sourceData.setValue(previousActionResult.getTargetData());
                } else {
                    sourceData = DataSourceManager.getValuesForDataSourceFromTargetNode(sourceData, mmRootNode);

                }
                ActionResult actionResult = ActionLogic.foundGeneralSingleMatching(sourceData, checkInfoDataList.get(0), validationRules.getActionList());

                if (actionResult != null) {
                    // decoding the value
                    String setValue = actionResult.getTarget();
                    String dateVal = ValidationRulesParser.getTheDateFromEncodedString(setValue);
                    // set the 1st nested value
                    mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(0).setLanguage(dateVal);
                    String validation = mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(0).getValidation();
                    if (validation.endsWith("?")) {
                        mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(0).setValidation(validation.replace("?", dateVal));
                    } else {
                        mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(0).setValidation(validation.split("_")[0] + "_" + dateVal);
                    }

                    mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(0).setSelected(true);
                    mmRootNode.getOptionsList().get(selectedRootIndex).getOptionsList().get(0).setDataCapture(true);
                    mmRootNode.getOptionsList().get(selectedRootIndex).setSelected(true);
                    ncdValidationResult.setUpdatedNode(mmRootNode);
                }

            }
        }

       /* } else {
            List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataNameType);

        }*/

        return ncdValidationResult;
    }

    private static NCDValidationResult checkForRecurringNumberSetType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, ValidationRules validationRules, boolean isInboundRequest, ActionResult previousActionResult) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        String rulesType = validationRules.getType();
        String sourceDataType = validationRules.getSourceDataType();
        String sourceDataNameType = validationRules.getSourceData();
        //List<CheckInfoData> checkInfoDataList = ValidationRulesParser.getCheckInfoList(validationRules.getCheck());
        // if (isInboundRequest) {
        // no need to check the source data bcz we have already the value in previousActionResult object
        if (validationRules.getActionType().equals(ValidationConstants.ACTION_TYPE_WAIT_FOR_RECURRING_DATA_CAPTURE)) {
            if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_THIS_NODE_VAL_LIST_INT_SET)) {
                List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataNameType);
                List<SourceData> sourceDataInfoValueList = new ArrayList<>();
                if (sourceDataInfoList != null) {
                    sourceDataInfoValueList = DataSourceManager.getValuesForDataSourceFromCurrentNodeRecurringPairDataList(sourceDataInfoList, selectedNode);
                }

                List<CheckInfoData> checkInfoDataList = ValidationRulesParser.getCheckInfoList(validationRules.getCheck());
                if (checkInfoDataList.size() != sourceDataInfoValueList.size()) {
                    for (int i = 0; i < checkInfoDataList.size(); i++) {

                        if (i >= sourceDataInfoValueList.size()) {
                            int size = sourceDataInfoValueList.size();
                            for (int j = 0; j < size; j++) {
                                if (checkInfoDataList.get(i).getDataName().equals(sourceDataInfoValueList.get(j).getDataName())) {
                                    SourceData sourceData = sourceDataInfoValueList.get(j);
                                    sourceDataInfoValueList.add(sourceData);
                                }
                            }
                        }


                    }
                }

                ActionResult actionResult = ActionLogic.foundActionResult(sourceDataInfoValueList, checkInfoDataList, validationRules.getActionList());

                if (actionResult == null) {
                    ncdValidationResult.setTargetNodeID(null);
                    ncdValidationResult.setReadyToEndTheScreening(false);
                    ncdValidationResult.setUpdatedNode(mmRootNode);
                } else {
                    /*for (int i = 0; i < selectedNode.getOptionsList().size(); i++) {
                        selectedNode.getOptionsList().get(i).setHidden(true);
                    }    */

                    ncdValidationResult.setActionResult(actionResult);
                    ncdValidationResult.setReadyToEndTheScreening(false);
                    ncdValidationResult.setUpdatedNode(mmRootNode);

                }
            }
        }

       /* } else {
            List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataNameType);

        }*/

        return ncdValidationResult;
    }


}
