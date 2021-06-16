package org.intelehealth.swasthyasamparktelemedicine.models;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class ResultsItem{

    @SerializedName("display")
    private String display;

    /*@SerializedName("links")
    private List<LinksItem> links;*/

    @SerializedName("uuid")
    private String uuid;

    public void setDisplay(String display){
        this.display = display;
    }

    public String getDisplay(){
        return display;
    }

    /*public void setLinks(List<LinksItem> links){
        this.links = links;
    }*/

    /*public List<LinksItem> getLinks(){
        return links;
    }*/

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public String getUuid(){
        return uuid;
    }

    @Override
    public String toString(){
        return
                "ResultsItem{" +
                        "display = '" + display + '\'' +
//                        ",links = '" + links + '\'' +
                        ",uuid = '" + uuid + '\'' +
                        "}";
    }


    @Override
    public boolean equals(Object o) {
		/*if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;*/
        ResultsItem that = (ResultsItem) o;
        return display.equalsIgnoreCase(that.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(display);
    }
}