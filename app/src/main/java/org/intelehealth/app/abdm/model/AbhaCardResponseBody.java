package org.intelehealth.app.abdm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Created by - Prajwal W. on 20/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class AbhaCardResponseBody implements Serializable {

    @SerializedName("image")
    @Expose
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "AbhaCardResponseBody{" +
                "image='" + image + '\'' +
                '}';
    }
}
