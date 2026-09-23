package org.intelehealth.app.models;


public class DocumentObject {

    private String documentName;
    private String documentPhoto;
    // Whether this document shows the remove control. Existing (already uploaded) documents are
    // non-deletable; newly added, not-yet-uploaded documents are deletable. Defaults to true so
    // the new-visit creation flow (where every document is freshly added) is unchanged.
    private boolean deletable = true;

    public DocumentObject(String documentName, String documentPhoto) {
        this.documentName = documentName;
        this.documentPhoto = documentPhoto;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
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
}
