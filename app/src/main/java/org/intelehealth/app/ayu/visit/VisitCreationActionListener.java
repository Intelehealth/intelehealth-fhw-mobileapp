package org.intelehealth.app.ayu.visit;

public interface VisitCreationActionListener {
    public void onFormSubmitted(int nextAction, Object object);

    public void onProgress(int progress);

    public void onTitleChange(String title);

    public void onManualClose();
    public void onCameraOpenRequest();
    void onImageRemoved(int index, String image);
}
