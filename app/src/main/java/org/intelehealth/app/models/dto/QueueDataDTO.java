package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The {@code queueData} object of the pull-data response, wrapping the queue
 * {@link QueueDTO items}.
 */
public class QueueDataDTO {

    @SerializedName("items")
    @Expose
    private List<QueueDTO> items = null;

    public List<QueueDTO> getItems() {
        return items;

    }

    public void setItems(List<QueueDTO> items) {
        this.items = items;
    }
}
