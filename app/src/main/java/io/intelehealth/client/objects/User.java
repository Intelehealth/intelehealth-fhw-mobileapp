package io.intelehealth.client.objects;

/**
 * User information class for Gson data serialization
 */
public class User {
    private int id;
    private Integer openmrs_provider_id;
    private Integer openmrs_user_id;
    private String openmrs_role;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String username;
    private String password;
    private String date_created;
    private Integer creator;
    private String date_changed;
    private Integer changed_by;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOpenmrsProviderId() {
        return openmrs_provider_id;
    }

    public void setOpenmrsProviderId(int openmrs_provider_id) {
        this.openmrs_provider_id = openmrs_provider_id;
    }

    public int getOpenmrsUserId() {
        return openmrs_user_id;
    }

    public void setOpenmrsUserId(int openmrs_user_id) {
        this.openmrs_user_id = openmrs_user_id;
    }

    public String getOpenmrsRole() {
        return openmrs_role;
    }

    public void setOpenmrsRole(String openmrs_role) {
        this.openmrs_role = openmrs_role;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddleName() {
        return middle_name;
    }

    public void setMiddleName(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateCreated() {
        return date_created;
    }

    public void setDateCreated(String date_created) {
        this.date_created = date_created;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public String getDateChanged() {
        return date_changed;
    }

    public void setDateChanged(String date_changed) {
        this.date_changed = date_changed;
    }

    public Integer getChangedBy() {
        return changed_by;
    }

    public void setChangedBy(Integer changed_by) {
        this.changed_by = changed_by;
    }
}
