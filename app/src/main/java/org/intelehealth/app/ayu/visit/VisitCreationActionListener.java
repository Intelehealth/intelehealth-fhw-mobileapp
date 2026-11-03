package org.intelehealth.app.ayu.visit;

import org.intelehealth.app.knowledgeEngine.Node;

public interface VisitCreationActionListener {
    public void onFormSubmitted(int nextAction, boolean isEditMode, Object object);

    public void onProgress(int progress);

    public void onTitleChange(String title);

    public void onManualClose();
    public void onCameraOpenRequest();
    void onImageRemoved(int nodeIndex, int imageIndex, String image);

    void onAyuDeviceRequest(Node node);
    // void onAyuDeviceRequest(Node node);
}
