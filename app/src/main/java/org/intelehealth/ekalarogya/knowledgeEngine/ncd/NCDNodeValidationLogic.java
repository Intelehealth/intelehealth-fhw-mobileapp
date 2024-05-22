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
    public static NCDValidationResult validateAndFindNextPath(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, boolean isInboundRequest) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        boolean isFlowEnd = selectedNode.getFlowEnd();
        ValidationRules validationRules = selectedNode.getValidationRules();
        if (!isInboundRequest)
            for (int i = 0; i < selectedNode.getOptionsList().size(); i++) {
                if (selectedNode.getOptionsList().get(i).isSelected()) {
                    validationRules = selectedNode.getOptionsList().get(i).getValidationRules();
                }
            }

        if (validationRules != null) {
            if (validationRules.getType().equalsIgnoreCase("NAVIGATE")) {
                return checkForNavigationType(context, patientUUID, mmRootNode, selectedRootIndex, selectedNode, validationRules);
            } else if (validationRules.getCheck().equalsIgnoreCase("RANGE_TEXT")) {
                return checkForRangeTextType(context, patientUUID, mmRootNode, selectedRootIndex, selectedNode);
            } else if (validationRules.getCheck().equalsIgnoreCase("TEXT_DATE")) {
                return checkForTextDateType(context, patientUUID,mmRootNode, selectedRootIndex, selectedNode, validationRules);

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
            } else if (sourceDataType.equals(ValidationConstants.SOURCE_DATA_TYPE_RANGE_NODE_VAL)) {
                sourceDataInfoValueList = DataSourceManager.getValuesForDataSourceFromTargetNode(sourceDataInfoList, mmRootNode, patientUUID);
            }
            List<CheckInfoData> checkInfoDataList = ValidationRulesParser.getCheckInfoList(validationRules.getCheck());
            ActionResult actionResult = ActionLogic.foundNextTargetNodeText(sourceDataInfoValueList, checkInfoDataList, validationRules.getActionList());
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
                                NCDValidationResult targetNcdValidationResult = validateAndFindNextPath(context, patientUUID, mmRootNode, i, tempNode, true);
                            }
                        } else {
                            mmRootNode.getOptionsList().get(i).setHidden(true);
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
     * @return
     */
    private static NCDValidationResult checkForRangeTextType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode) {
        return new NCDValidationResult();
    }

    private static NCDValidationResult checkForTextDateType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode, ValidationRules validationRulese) {
        return new NCDValidationResult();
    }


}
