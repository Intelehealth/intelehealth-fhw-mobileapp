package org.intelehealth.app.ayu.visit.common;

import org.intelehealth.app.knowledgeEngine.Node;

public interface OnItemSelection {
    void onSelect(Node node, int index, boolean isSkipped);

    void needTitleChange(String title);

    void onAllAnswered(boolean isAllAnswered);

    void onCameraRequest();

    void onImageRemoved(int index, String image);
}
