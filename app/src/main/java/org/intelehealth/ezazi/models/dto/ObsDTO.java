
package org.intelehealth.ezazi.models.dto;

import static org.intelehealth.ezazi.utilities.DateAndTimeUtils.formatDateTimeNew;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.utils.DateTimeUtils;

public class ObsDTO implements ItemHeader {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("encounteruuid")
    @Expose
    private String encounteruuid;
    @SerializedName("conceptuuid")
    @Expose
    private String conceptuuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("obsServerModifiedDate")
    @Expose
    private String obsServerModifiedDate;
    @SerializedName("creator")
    @Expose
    private String creator;

    @SerializedName("creatoruuid")
    @Expose
    private String creatorUuid;

    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("voided")
    @Expose
    private Integer voided;

    private String name;

    private int noOfLine;

    private final int minLine = 2;

    private int contentLine = minLine;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncounteruuid() {
        return encounteruuid;
    }

    public void setEncounteruuid(String encounteruuid) {
        this.encounteruuid = encounteruuid;
    }

    public String getConceptuuid() {
        return conceptuuid;
    }

    public void setConceptuuid(String conceptuuid) {
        this.conceptuuid = conceptuuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public String getObsServerModifiedDate() {
        return obsServerModifiedDate;
    }

    public void setObsServerModifiedDate(String obsServerModifiedDate) {
        this.obsServerModifiedDate = obsServerModifiedDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        Log.d("TAG", "getCreatedDate: createdDate : " + createdDate);
        if (createdDate != null && !createdDate.isEmpty()) {
            if (createdDate.contains("T")) {
                return formatDateTimeNew(createdDate);
            } else if (createdDate.contains("am") || createdDate.contains("pm")) {
                return createdDate;
            } else {
                return DateTimeUtils.utcToLocalDate(createdDate, AppConstants.UTC_FORMAT, AppConstants.VISIT_FORMAT);
            }
        }
        return "";
    }

    public String dateWithDrName() {
        return name + " " + getCreatedDate();
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @NonNull
    @Override
    public String createdDate() {
        return createdDate;
    }

    public String getCreatorUuid() {
        return creatorUuid;
    }

    public void setCreatorUuid(String creatorUuid) {
        this.creatorUuid = creatorUuid;
    }

    public void setNoOfLine(int noOfLine) {
        this.noOfLine = noOfLine;
    }

    public int getNoOfLine() {
        return noOfLine;
    }

    public void updateVisibleContentLine() {
        if (noOfLine > minLine && contentLine == minLine) {
            contentLine = noOfLine;
        } else contentLine = minLine;
    }

    public int getContentLine() {
        return contentLine;
    }

    public int getMinLine() {
        return minLine;
    }

    public boolean isValidPlan() {
        return value != null && value.length() > 0;
    }

    public ObsDTO toObs(String encounterId, String creator, String createdDate,String conceptUuid) {
        Log.d("TAG", "toObs: creator : " + creator);
        ObsDTO obs = new ObsDTO();
        obs.setUuid(uuid);
        obs.setConceptuuid(conceptUuid);
        obs.setValue(value);
        obs.setCreator(creator);
        obs.setEncounteruuid(encounterId);
        obs.setCreatorUuid(creator);
        obs.setObsServerModifiedDate(creator);
        Log.e("plans", "toObs: " + new Gson().toJson(obs));
        return obs;
    }

}
