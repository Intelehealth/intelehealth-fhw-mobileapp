package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import org.intelehealth.ekalarogya.knowledgeEngine.Node;

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
     * @param mmRootNode
     * @param selectedNode
     * @return An instance of NCDValidationResult representing the result of the validation.
     */
    public static NCDValidationResult validateAndFindNextPath(Node mmRootNode, Node selectedNode) {
        NCDValidationResult ncdValidationResult = new NCDValidationResult();

        return ncdValidationResult;
    }
}
