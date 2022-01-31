package org.intelehealth.unicef.appointment.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.appointment.model.AppointmentInfo;
import org.intelehealth.unicef.database.dao.EncounterDAO;
import org.intelehealth.unicef.utilities.UuidGenerator;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final String TAG = AppointmentDAO.class.getSimpleName();
    private long createdRecordsCount = 0;


    public void insert(AppointmentInfo appointmentInfo) throws DAOException {
        AppointmentInfo checkAppointmentInfo = getAppointmentByVisitId(appointmentInfo.getVisitUuid());
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put("uuid", checkAppointmentInfo != null ? checkAppointmentInfo.getUuid() : new UuidGenerator().UuidGenerator());
            values.put("appointment_id", appointmentInfo.getId());
            values.put("slot_day", appointmentInfo.getSlotDay());
            values.put("slot_date", appointmentInfo.getSlotDate());
            values.put("slot_duration", appointmentInfo.getSlotDuration());
            values.put("slot_duration_unit", appointmentInfo.getSlotDurationUnit());
            values.put("slot_time", appointmentInfo.getSlotTime());
            values.put("speciality", appointmentInfo.getSpeciality());
            values.put("user_uuid", appointmentInfo.getUserUuid());
            values.put("dr_name", appointmentInfo.getDrName());
            values.put("visit_uuid", appointmentInfo.getVisitUuid());
            values.put("patient_id", appointmentInfo.getPatientId());
            values.put("patient_name", appointmentInfo.getPatientName());
            values.put("open_mrs_id", appointmentInfo.getOpenMrsId());
            values.put("status", appointmentInfo.getStatus());
            values.put("created_at", appointmentInfo.getCreatedAt());
            values.put("updated_at", appointmentInfo.getUpdatedAt());

            createdRecordsCount = db.insertWithOnConflict("tbl_appointments", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();

        } catch (SQLException e) {

            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

    }

    public AppointmentInfo getAppointmentByVisitId(String visitUUID) {
        Log.v(TAG, "getByVisitUUID - visitUUID - " + visitUUID);

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments where visit_uuid = ?", new String[]{visitUUID});
        AppointmentInfo appointmentInfo = null;
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
            }

        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close();

        return appointmentInfo;
    }


    public List<AppointmentInfo> getAppointments() {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments", new String[]{});
        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                try {
                    if(!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    }else{
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close();

        return appointmentInfos;
    }

    public void deleteAppointmentByVisitId(String visitUuid) {
        Log.v(TAG, "getByVisitUUID - visitUUID - " + visitUuid);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete("tbl_appointments", "visit_uuid=?", new String[]{visitUuid});
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close();
    }

    public void deleteAllAppointments() {
        Log.v(TAG, "deleteAllAppointments ");
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete("tbl_appointments", null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close();
    }
}
