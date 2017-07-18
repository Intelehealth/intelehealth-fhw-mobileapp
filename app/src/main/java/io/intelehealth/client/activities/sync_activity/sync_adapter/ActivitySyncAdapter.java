package io.intelehealth.client.activities.sync_activity.sync_adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.intelehealth.client.R;
import io.intelehealth.client.utilities.RecyclerViewCursorAdapter;

/**
 * Created by Dexter Barretto on 7/17/17.
 * Github : @dbarretto
 */

public class ActivitySyncAdapter extends RecyclerViewCursorAdapter<SyncViewHolder> {

    public ActivitySyncAdapter() {
        super(null);
    }

    @Override
    public SyncViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sync, parent, false);
        return new SyncViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SyncViewHolder holder, Cursor cursor) {
        SyncModel syncModel = new SyncModel(cursor);
        holder.bindItems(syncModel);
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.getRootView().getContext(), "View Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
