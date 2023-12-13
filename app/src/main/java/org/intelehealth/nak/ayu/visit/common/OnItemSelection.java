package org.intelehealth.nak.ayu.visit.common;

import org.intelehealth.nak.knowledgeEngine.Node;

public interface OnItemSelection {
    void onSelect(Node node, int index, boolean isSkipped, Node selectedNode);

    void needTitleChange(String title);

    void onAllAnswered(boolean isAllAnswered);

    void onCameraRequest();

    void onImageRemoved(int nodeIndex,int imageIndex, String image);


}
