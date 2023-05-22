package org.intelehealth.unicef.ayu.visit.common;

import org.intelehealth.unicef.knowledgeEngine.Node;

public interface OnItemSelection {
    void onSelect(Node node, int index);

    void needTitleChange(String title);

    void onAllAnswered(boolean isAllAnswered);

    void onCameraRequest();

    void onImageRemoved(int index, String image);
}
