package edu.jhu.bme.cbid.healthassistantsclient;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

/**
 * Class to manage input/output with the database.
 */
public class DatabaseIO {
    private SQLiteDatabase localDb;

    /**
     * Creates a new instance of DatabaseIO.
     * @param db The database which with to interface
     */
    public DatabaseIO(SQLiteDatabase db) {
        this.localDb = db;
    }

    /**
     * Creates the table as per the database schema.
     * Should be run onCreate() because the table must exist for future use.
     */
    public void createTable() {
        // TODO: Update SQL for CREATE TABLE
        String sql = "CREATE TABLE IF NOT EXISTS";
        SQLiteStatement stmt = localDb.compileStatement(sql);
        stmt.execute();
    }

    /**
     * Inserts data into the local database.
     * Binds parameters to a prepared SQL statement to prevent injection.
     * @param sql SQL statement to execute, in prepared form
     * @param strings Array of values to be inserted
     * @param numStrings The number of values given
     */
    public void insertPatientData(String sql, String[] strings, int numStrings) {

        SQLiteStatement stmt = localDb.compileStatement(sql);
        for (int i = 1; i <= numStrings; i++) {
            stmt.bindString(i, strings[i - 1]);
        }
        stmt.executeInsert();

    }

    /**
     * Updates (or deletes) data into the local database.
     * Binds parameters to a prepared SQL statement to prevent injection.
     * @param sql SQL statement to execute, in prepared form
     * @param strings Array of values to be inserted
     * @param numStrings The number of values given
     * @return the number of rows affect by our update/delete statement
     */
    public int updatePatientData(String sql, String[] strings, int numStrings) {
        SQLiteStatement stmt = localDb.compileStatement(sql);
        for (int i = 1; i <= numStrings; i++) {
            stmt.bindString(i, strings[i - 1]);
        }
        return stmt.executeUpdateDelete();
    }

    /**
     * Query the local database for the given patient.
     * Binds parameters to a prepared SQL statement to prevent injection.
     * Parses through the cursor result in order to determine demographic data.
     * @param value the patient to query
     * @return the ArrayList of the patient information if found in the database; null otherwise
     */
    public ArrayList<String> queryPatient(String value) {
        // TODO: Remove placeholders - update to reflect actual schema

        ArrayList<String> patientInfo = new ArrayList<String>();
        String table = "table";
        String[] columnsToReturn = { "col1", "etc" };
        String selection = "col1 = ?";
        String[] args = { value }; // Matches value to ? in selection
        Cursor cursor = localDb.query(table, columnsToReturn, selection, args, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String firstName = cursor.getString(cursor.getColumnIndex("firstName"));
                    patientInfo.add(firstName);

                    String lastName = cursor.getString(cursor.getColumnIndex("lastName"));
                    patientInfo.add(lastName);

                    String phone = cursor.getString(cursor.getColumnIndex("phone"));
                    patientInfo.add(phone);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        return patientInfo; // Caller should check for null!

    }
}
