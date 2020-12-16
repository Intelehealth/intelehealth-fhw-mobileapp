
package app.intelehealth.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EncounterProvider {

    @SerializedName("encounterRole")
    @Expose
    private String encounterRole;
    @SerializedName("provider")
    @Expose
    private String provider;

    public String getEncounterRole() {
        return encounterRole;
    }

    public void setEncounterRole(String encounterRole) {
        this.encounterRole = encounterRole;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

}
