package org.intelehealth.swasthyasamparktelemedicine.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClsUserGetResponse{

    @SerializedName("results")
    private List<ResultsItem> results;

    public void setResults(List<ResultsItem> results){
        this.results = results;
    }

    public List<ResultsItem> getResults(){
        return results;
    }

    @Override
    public String toString(){
        return
                "ClsUserGetResponse{" +
                        "results = '" + results + '\'' +
                        "}";
    }
}