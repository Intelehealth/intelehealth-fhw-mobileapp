package org.intelehealth.swasthyasamparktelemedicine.models.NewUserCreationCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sagar Shimpi
 */
public class UserCreationData {

    public UserCreationData() {
    }

    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("password")
    @Expose
    public String password;
    @SerializedName("person")
    @Expose
    public PersonUser person;
    @SerializedName("roles")
    @Expose
    public List<String> roles = null;

    public UserCreationData(String username, String password, PersonUser person, List<String> roles) {
        this.username = username;
        this.password = password;
        this.person = person;
        this.roles = roles;
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

    public PersonUser getPerson() {
        return person;
    }

    public void setPerson(PersonUser person) {
        this.person = person;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
