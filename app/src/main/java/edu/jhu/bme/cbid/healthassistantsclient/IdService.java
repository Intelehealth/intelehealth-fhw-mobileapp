package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Sends Identification data to OpenMRS and receives the OpenMRS ID of the newly-created patient
 */
public class IdService extends IntentService {

    KnowledgeDatabaseHelper mDbHelper = new KnowledgeDatabaseHelper(this);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public IdService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String dataString = intent.getDataString(); // The dataString is the _id of the patient to send

        SQLiteDatabase db = mDbHelper.getWritableDatabase();



    }
}
