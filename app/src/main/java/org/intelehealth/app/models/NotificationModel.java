package org.intelehealth.app.models;

/**
 * Created by Prajwal Waingankar on 30/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class NotificationModel {
    private String uuid;
    private String first_name;
    private String last_name;
    private String patientuuid;
    private String description;
    private String obs_server_modified_date;
    private String notification_type;
    private String sync;

    // firstname and lastname not in table bt just fetched to store value before storing in desc table.


//    public NotificationModel(String first_name, String last_name, String obs_server_modified_date) {
//        this.first_name = first_name;
//        this.last_name = last_name;
//        this.obs_server_modified_date = obs_server_modified_date;
//    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getObs_server_modified_date() {
        return obs_server_modified_date;
    }

    public void setObs_server_modified_date(String obs_server_modified_date) {
        this.obs_server_modified_date = obs_server_modified_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public String getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(String notification_type) {
        this.notification_type = notification_type;
    }
}
