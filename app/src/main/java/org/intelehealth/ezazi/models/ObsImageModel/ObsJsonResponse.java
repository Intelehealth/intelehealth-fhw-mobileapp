
package org.intelehealth.ezazi.models.ObsImageModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.models.loginModel.Link;
import org.intelehealth.ezazi.models.loginModel.Person;

import java.util.List;

public class ObsJsonResponse {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("display")
    @Expose
    private String display;
    @SerializedName("concept")
    @Expose
    private Person concept;
    @SerializedName("person")
    @Expose
    private Person person;
    @SerializedName("obsDatetime")
    @Expose
    private String obsDatetime;
    @SerializedName("accessionNumber")
    @Expose
    private Object accessionNumber;
    @SerializedName("obsGroup")
    @Expose
    private Object obsGroup;
    @SerializedName("valueCodedName")
    @Expose
    private Object valueCodedName;
    @SerializedName("groupMembers")
    @Expose
    private Object groupMembers;
    @SerializedName("comment")
    @Expose
    private Object comment;
    @SerializedName("location")
    @Expose
    private Object location;
    @SerializedName("order")
    @Expose
    private Object order;
    @SerializedName("encounter")
    @Expose
    private Person encounter;
    @SerializedName("voided")
    @Expose
    private Boolean voided;
    @SerializedName("value")
    @Expose
    private Person value;
    @SerializedName("valueModifier")
    @Expose
    private Object valueModifier;
    @SerializedName("formFieldPath")
    @Expose
    private Object formFieldPath;
    @SerializedName("formFieldNamespace")
    @Expose
    private Object formFieldNamespace;
    @SerializedName("links")
    @Expose
    private List<Link> links = null;
    @SerializedName("resourceVersion")
    @Expose
    private String resourceVersion;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Person getConcept() {
        return concept;
    }

    public void setConcept(Person concept) {
        this.concept = concept;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(String obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public Object getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(Object accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Object getObsGroup() {
        return obsGroup;
    }

    public void setObsGroup(Object obsGroup) {
        this.obsGroup = obsGroup;
    }

    public Object getValueCodedName() {
        return valueCodedName;
    }

    public void setValueCodedName(Object valueCodedName) {
        this.valueCodedName = valueCodedName;
    }

    public Object getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(Object groupMembers) {
        this.groupMembers = groupMembers;
    }

    public Object getComment() {
        return comment;
    }

    public void setComment(Object comment) {
        this.comment = comment;
    }

    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }

    public Object getOrder() {
        return order;
    }

    public void setOrder(Object order) {
        this.order = order;
    }

    public Person getEncounter() {
        return encounter;
    }

    public void setEncounter(Person encounter) {
        this.encounter = encounter;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public Person getValue() {
        return value;
    }

    public void setValue(Person value) {
        this.value = value;
    }

    public Object getValueModifier() {
        return valueModifier;
    }

    public void setValueModifier(Object valueModifier) {
        this.valueModifier = valueModifier;
    }

    public Object getFormFieldPath() {
        return formFieldPath;
    }

    public void setFormFieldPath(Object formFieldPath) {
        this.formFieldPath = formFieldPath;
    }

    public Object getFormFieldNamespace() {
        return formFieldNamespace;
    }

    public void setFormFieldNamespace(Object formFieldNamespace) {
        this.formFieldNamespace = formFieldNamespace;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

}
