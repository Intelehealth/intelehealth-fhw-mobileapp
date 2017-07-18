package io.intelehealth.client.activities.sync_activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.sync_activity.sync_adapter.ActivitySyncAdapter;
import io.intelehealth.client.activities.today_patient_activity.TodayPatientActivity;
import io.intelehealth.client.database.DelayedJobQueueProvider;

public class ActivitySync extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mRecyclerView;
    ActivitySyncAdapter mSyncAdapter;
    private static final String TAG = ActivitySync.class.getSimpleName();

    private static final int LOADER_ID = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        mRecyclerView = (RecyclerView) findViewById(R.id.sync_RecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new
                DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                DelayedJobQueueProvider.CONTENT_URI
                , new String[]{DelayedJobQueueProvider._ID, DelayedJobQueueProvider.PATIENT_ID,
                DelayedJobQueueProvider.PATIENT_NAME, DelayedJobQueueProvider.JOB_TYPE,
                DelayedJobQueueProvider.STATUS, DelayedJobQueueProvider.SYNC_STATUS}, null, null, DelayedJobQueueProvider._ID + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (mSyncAdapter == null) {
            mSyncAdapter = new ActivitySyncAdapter();
            mRecyclerView.setAdapter(mSyncAdapter);
        }

        mSyncAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSyncAdapter.swapCursor(null);
    }


}
