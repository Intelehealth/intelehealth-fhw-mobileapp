package io.intelehealth.client.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqliteDbCloseHelper {

    public void dbClose(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public void cursorClose(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
