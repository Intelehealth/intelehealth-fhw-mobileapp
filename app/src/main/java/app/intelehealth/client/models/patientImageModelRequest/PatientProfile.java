
package app.intelehealth.client.models.patientImageModelRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientProfile {

    @SerializedName("person")
    @Expose
    private String person;
    @SerializedName("base64EncodedImage")
    @Expose
    private String base64EncodedImage;

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getBase64EncodedImage() {
        return base64EncodedImage;
    }

    public void setBase64EncodedImage(String base64EncodedImage) {
        this.base64EncodedImage = base64EncodedImage;
    }

}
