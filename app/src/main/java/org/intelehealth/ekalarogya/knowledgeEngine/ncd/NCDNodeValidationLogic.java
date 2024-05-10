package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import org.intelehealth.ekalarogya.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides validation logic for nodes after the user action.
 * It ensures that nodes meet certain criteria before they are moving to the next.
 * Created by Lincon Pradhan on 9th May, 2024
 * Contact me: lincon@intelehealth.org
 */
public class NCDNodeValidationLogic {
    /**
     * Validates the node logic from the specified node and
     * finds the next path, considering the root node update with autofill data if any.
     *
     * @param mmRootNode
     * @param selectedNode
     * @return An instance of NCDValidationResult representing the result of the validation.
     */
    public static NCDValidationResult validateAndFindNextPath(Node mmRootNode, int selectedRootIndex, Node selectedNode) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();
        boolean isFlowEnd = selectedNode.getFlowEnd();
        ValidationRules validationRules = selectedNode.getValidationRules();
        if (validationRules != null) {

        } else {
            if (isFlowEnd) {
                // need to remove the other nodes from root
                List<Node> temp = new ArrayList<Node>();
                for (int i = 0; i < mmRootNode.getOptionsList().size(); i++) {
                    if(i<=selectedRootIndex){
                        temp.add(mmRootNode.getOptionsList().get(i));
                    }else{
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
}
