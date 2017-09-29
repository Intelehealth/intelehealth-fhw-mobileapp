package io.intelehealth.client.activities.sync_activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.sync_activity.sync_adapter.ActivitySyncAdapter;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.services.sync.JobDispatchService;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                Toast.makeText(this, "Syncing", Toast.LENGTH_SHORT).show();
                startJobDispatcherService(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSyncAdapter.swapCursor(null);
    }


    private void startJobDispatcherService(Context context) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job uploadCronJob = firebaseJobDispatcher.newJobBuilder()
                .setService(JobDispatchService.class)
                .setTag("Delayed Job Queue Manual Sync")
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(
                        0, 0
                ))
                .setReplaceCurrent(true)
                .setConstraints(
                        // only run on any network
                        Constraint.ON_ANY_NETWORK)
                .build();

        int i = firebaseJobDispatcher.schedule(uploadCronJob);
        Toast.makeText(context, ""+i, Toast.LENGTH_SHORT).show();
        /**
         *  0 - Indicates the schedule request seems to have been successful.
         *  1 - Indicates the schedule request encountered an unknown error.
         *  2 - Indicates the schedule request failed because the driver was unavailable.
         *  3 - Indicates the schedule request failed because the Trigger was unsupported.
         *  4 -  Indicates the schedule request failed because the service is not exposed or configured correctly.
         */

    }

}
