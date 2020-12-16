package app.intelehealth.client.models;


public class DocumentObject {

    private String documentName;
    private String documentPhoto;

    public DocumentObject(String documentName, String documentPhoto) {
        this.documentName = documentName;
        this.documentPhoto = documentPhoto;
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
