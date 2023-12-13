package org.intelehealth.nak.ayu.visit;

public interface VisitCreationActionListener {
    public void onFormSubmitted(int nextAction, boolean isEditMode, Object object);

    public void onProgress(int progress);

    public void onTitleChange(String title);

    public void onManualClose();
    public void onCameraOpenRequest();
    void onImageRemoved(int nodeIndex, int imageIndex, String image);
}
