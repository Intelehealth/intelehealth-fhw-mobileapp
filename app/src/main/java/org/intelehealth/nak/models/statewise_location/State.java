package org.intelehealth.nak.models.statewise_location;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Prajwal Waingankar
 * on 27-Jan-2021.
 * Github: prajwalmw
 */

public class State {

    @SerializedName("results")
    @Expose
    private List<Result> results = null;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

}
