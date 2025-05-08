package org.intelehealth.app.models;


public class DocumentObject {

    private String documentName;
    private String documentPhoto;
    private boolean shouldShowDocumentCancelButton;

    public DocumentObject(String documentName, String documentPhoto, boolean shouldShowDocumentCancelButton) {
        this.documentName = documentName;
        this.documentPhoto = documentPhoto;
        this.shouldShowDocumentCancelButton = shouldShowDocumentCancelButton;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentPhoto() {
        return documentPhoto;
    }

    public void setDocumentPhoto(String documentPhoto) {
        this.documentPhoto = documentPhoto;
    }

    public boolean shouldShowDocumentCancelButton() {
        return shouldShowDocumentCancelButton;
    }

    public void setShouldShowDocumentCancelButton(boolean shouldShowDocumentCancelButton) {
        this.shouldShowDocumentCancelButton = shouldShowDocumentCancelButton;
    }
}
