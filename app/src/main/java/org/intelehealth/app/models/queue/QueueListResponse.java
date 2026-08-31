package org.intelehealth.app.models.queue;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Top-level envelope of the {@code /api/queue/list} response:
 * {@code { success, message, data: { items[], ... } }}.
 *
 * <p>Mirrors the {@link org.intelehealth.app.models.dto.ResponseDTO} pattern
 * used by the middleware pull-sync, but for the queue microservice.
 */
public class QueueListResponse {

    @SerializedName("success")
    @Expose
    private boolean success;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private QueueListData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public QueueListData getData() {
        return data;
    }
}
