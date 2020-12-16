
package app.intelehealth.client.models.ObsImageModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private Concept concept;
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
    private Encounter encounter;
    @SerializedName("voided")
    @Expose
    private Boolean voided;
    @SerializedName("value")
    @Expose
    private Value value;
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
    private List<Link___> links = null;
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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
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

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
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

    public List<Link___> getLinks() {
        return links;
    }

    public void setLinks(List<Link___> links) {
        this.links = links;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

}
