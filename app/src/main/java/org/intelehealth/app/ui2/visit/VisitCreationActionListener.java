package org.intelehealth.app.ui2.visit;

public interface VisitCreationActionListener {
    public void onFormSubmitted(int nextAction);
    public void onProgress(int progress);
    public void onManualClose();
}
