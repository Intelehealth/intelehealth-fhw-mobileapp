package io.intelehealth.client.models;

/**
 * Created by Dexter Barretto on 10/5/17.
 * Github : @dbarretto
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Identifier {

    @SerializedName("identifiers")
    @Expose
    private List<String> identifiers = null;

    /**
     * No args constructor for use in serialization
     */
    public Identifier() {
    }

    /**
     * @param identifiers
     */
    public Identifier(List<String> identifiers) {
        super();
        this.identifiers = identifiers;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

}
