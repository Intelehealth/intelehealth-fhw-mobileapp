package io.intelehealth.client.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Content provider for performing CRUD operations on delayed_jobs table.
 * <p>
 * Created by Dexter Barretto on 5/31/17.
 * Github : @dbarretto
 */

public class DelayedJobQueueProvider extends ContentProvider {


    public static final String PROVIDER_NAME = "io.intelehealth.client.database.DelayedJobQueueProvider";
    public static final String URL = "content://" + PROVIDER_NAME + "/jobs";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    //Strings for Database
    public static final String _ID = "_id"; //Integer
    public static final String JOB_TYPE = "service_call"; //String
    public static final String JOB_PRIORITY = "priority"; //Integer
    public static final String JOB_REQUEST_CODE = "request_code"; //Integer
    public static final String PATIENT_NAME = "patient_name"; //String
    public static final String PATIENT_ID = "patient_id"; //String
    public static final String VISIT_ID = "visit_id"; //String
    public static final String VISIT_UUID = "visit_uuid"; //String
    public static final String STATUS = "status"; //Integer
    public static final String DATA_RESPONSE = "data_response"; //String
    public static final String SYNC_STATUS = "sync_status"; //Integer


    private static HashMap<String, String> DELAYED_JOBS_PROJECTION_MAP;

    static final int DELAYED_JOBS = 1;
    static final int DELAYED_JOB_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "jobs", DELAYED_JOBS);
        uriMatcher.addURI(PROVIDER_NAME, "jobs/#", DELAYED_JOB_ID);
    }

    /**
     * Database specific constant declarations
     */

    private SQLiteDatabase db;
    public static final String DELAYED_JOBS_TABLE_NAME = "delayed_jobs";


    LocalRecordsDatabaseHelper dbHelper;


    public DelayedJobQueueProvider() {

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new LocalRecordsDatabaseHelper(context);
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */

        db = dbHelper.getWritableDatabase();
        return (db != null);

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all delayed records
             */
            case DELAYED_JOBS:
                return "vnd.android.cursor.dir/vnd.intelehealth.delayedjobs";
            /**
             * Get a particular delayed job
             */
            case DELAYED_JOB_ID:
                return "vnd.android.cursor.item/vnd.intelehealth.delayedjobs";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DELAYED_JOBS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case DELAYED_JOBS:
                qb.setProjectionMap(DELAYED_JOBS_PROJECTION_MAP);
                break;
            case DELAYED_JOB_ID:
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder == "") {
            /**
             * By default sort on priority and id
             */
            sortOrder = _ID;
        }

        Cursor c = qb.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        /**
         * Register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        /**
         * Add a new job record
         */
        long rowID = db.insert(DELAYED_JOBS_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case DELAYED_JOBS:
                count = db.delete(DELAYED_JOBS_TABLE_NAME, selection, selectionArgs);
                break;

            case DELAYED_JOB_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(DELAYED_JOBS_TABLE_NAME, _ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection +
                                ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case DELAYED_JOBS:
                count = db.update(DELAYED_JOBS_TABLE_NAME, values, selection, selectionArgs);
                break;

            case DELAYED_JOB_ID:
                count = db.update(DELAYED_JOBS_TABLE_NAME, values,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection +
                                        ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}