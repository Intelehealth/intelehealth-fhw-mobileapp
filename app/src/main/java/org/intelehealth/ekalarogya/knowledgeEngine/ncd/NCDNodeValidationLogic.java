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
    public static NCDValidationResult validateAndFindNextPath(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        boolean isFlowEnd = selectedNode.getFlowEnd();
        ValidationRules validationRules = selectedNode.getValidationRules();
        if (validationRules != null) {
            if (validationRules.getCheck().equalsIgnoreCase("NAVIGATE")) {
                return checkForNavigationType(context, patientUUID,mmRootNode, selectedRootIndex, selectedNode);
            } else if (validationRules.getCheck().equalsIgnoreCase("RANGE_TEXT")) {
                return checkForRangeTextType(mmRootNode, selectedRootIndex, selectedNode);
            } else if (validationRules.getCheck().equalsIgnoreCase("TEXT_DATE")) {
                return checkForTextDateType(mmRootNode, selectedRootIndex, selectedNode);

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
    private static NCDValidationResult checkForNavigationType(Context context, String patientUUID, Node mmRootNode, int selectedRootIndex, Node selectedNode) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        String rulesType = selectedNode.getValidationRules().getType();
        String sourceDataType = selectedNode.getValidationRules().getSourceDataType();
        String sourceDataNameType = selectedNode.getValidationRules().getSourceData();
        List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataNameType);
        if (sourceDataInfoList != null) {
            List<SourceData> sourceDataInfoValueList = DataSourceManager.getValuesForDataSourceFromPatientAttributes(sourceDataInfoList, patientUUID);
        } else {
            // something went wrong
            Timber.tag(TAG).v(context.getString(R.string.something_went_wrong));
        }

        return new NCDValidationResult();
    }

    /**
     * @param mmRootNode
     * @param selectedRootIndex
     * @param selectedNode
     * @return
     */
    private static NCDValidationResult checkForRangeTextType(Node mmRootNode, int selectedRootIndex, Node selectedNode) {
        return new NCDValidationResult();
    }

    private static NCDValidationResult checkForTextDateType(Node mmRootNode, int selectedRootIndex, Node selectedNode) {
        return new NCDValidationResult();
    }
}
