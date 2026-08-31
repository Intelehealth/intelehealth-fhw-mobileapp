package org.intelehealth.app.models.queue;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The {@code data} object of the {@code /api/queue/list} response: the page of
 * queue {@link QueueItem items} plus paging metadata.
 */
public class QueueListData {

    @SerializedName("items")
    @Expose
    private List<QueueItem> items;

    @SerializedName("total")
    @Expose
    private int total;

    @SerializedName("limit")
    @Expose
    private int limit;

    @SerializedName("offset")
    @Expose
    private int offset;

    @SerializedName("hasMore")
    @Expose
    private boolean hasMore;

    // Dynamic, server-defined shape — kept raw.
    @SerializedName("appliedFilters")
    @Expose
    private JsonObject appliedFilters;

    public List<QueueItem> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public JsonObject getAppliedFilters() {
        return appliedFilters;
    }
}
