package org.intelehealth.kf.ayu.visit.common;

import org.intelehealth.kf.knowledgeEngine.Node;

public interface OnItemSelection {
    void onSelect(Node node, int index, boolean isSkipped, Node selectedNode);

    void needTitleChange(String title);

    void onAllAnswered(boolean isAllAnswered);

    void onCameraRequest();

    void onImageRemoved(int index, String image);
}
