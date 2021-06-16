package org.intelehealth.swasthyasamparktelemedicine.models.GetUserCallRes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar Shimpi
 */
public class UserCallRes {

    @SerializedName("person")
    @Expose
    public Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public class Person {

        @SerializedName("uuid")
        @Expose
        public String uuid;
        @SerializedName("display")
        @Expose
        public String display;

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
    }

}
