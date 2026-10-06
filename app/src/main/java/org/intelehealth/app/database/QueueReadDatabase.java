package org.intelehealth.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;

/**
 * Dedicated read-only connection to the local DB, used only by the Patient's
 * Queue reads (QueueDAO / QueueRepository).
 *
 * <p>Why: the app shares one {@link SQLiteDatabase} object
 * ({@link InteleHealthDatabaseHelper}) without WAL, which Android backs by a
 * single connection — every query from every thread runs one after another.
 * So the tiny queue query waited behind the Home dashboard's multi-second count
 * queries. SQLite itself allows concurrent readers on separate connections, so
 * a second, read-only connection lets the queue read in parallel without
 * touching the legacy Home code or the app-wide DB configuration.
 *
 * <p>A reader can still briefly wait while a sync commits a write (Android's
 * connection busy timeout); callers retry once on
 * {@link android.database.sqlite.SQLiteDatabaseLockedException}.
 */
public final class QueueReadDatabase {

    private static volatile SQLiteDatabase database;

    private QueueReadDatabase() {
    }

    /** The shared read-only connection, opened on first use. */
    @NonNull
    public static SQLiteDatabase get() {
        SQLiteDatabase db = database;
        if (db != null && db.isOpen()) {
            return db;
        }
        synchronized (QueueReadDatabase.class) {
            if (database == null || !database.isOpen()) {
                // Let the main helper create / upgrade the schema first, so this
                // connection never opens a missing or half-migrated file.
                IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

                Context context = IntelehealthApplication.getAppContext();
                String path = context.getDatabasePath(AppConstants.DATABASE_NAME).getPath();
                database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            }
            return database;
        }
    }
}
