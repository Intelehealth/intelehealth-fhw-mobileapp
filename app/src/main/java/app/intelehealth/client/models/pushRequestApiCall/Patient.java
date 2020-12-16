
package app.intelehealth.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Patient {

    @SerializedName("person")
    @Expose
    private String person;
    @SerializedName("identifiers")
    @Expose
    private List<Identifier> identifiers = null;

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

}
