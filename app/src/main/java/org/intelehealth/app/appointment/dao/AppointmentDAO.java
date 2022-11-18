package org.intelehealth.app.appointment.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.UuidGenerator;
import org.intelehealth.app.utilities.exception.DAOException;

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


//    public List<AppointmentInfo> getAppointments() {
//        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();
//        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments", new String[]{});
//
//
//        if (idCursor.getCount() != 0) {
//            while (idCursor.moveToNext()) {
//                AppointmentInfo appointmentInfo = new AppointmentInfo();
//                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
//                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
//                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
//                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
//                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
//                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
//                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
//                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
//                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
//                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
//                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
//                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
//                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name")));
//                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
//                appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
//                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
//                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
//                appointmentInfos.add(appointmentInfo);
//            }
//
//        }
//        idCursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        //db.close();
//
//        return appointmentInfos;
//    }

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
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
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

    public List<AppointmentInfo> getAppointmentsWithFilters(String fromDate,
                                                            String toDate, String searchPatientText) {
        String search = searchPatientText.trim().replaceAll("\\s", "");

        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor;
        String table = "tbl_appointments";


        if (!fromDate.isEmpty() && !toDate.isEmpty() && !searchPatientText.isEmpty()) {
            String selectQuery = "SELECT * FROM " + table +
                    " WHERE patient_name LIKE " + "'%" + search + "%'  and slot_date BETWEEN '" + fromDate + "' and '" + toDate + "'" +
                    " ORDER BY patient_name ASC";
            Log.d(TAG, "getAppointmentsWithFilters: 1selectQuery : " + selectQuery);
            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "SELECT * FROM tbl_appointments where slot_date BETWEEN '" + fromDate + "'  and '" + toDate + "'";
            Log.d(TAG, "getAppointments: 2selectQuery : " + selectQuery);
            idCursor = db.rawQuery(selectQuery, null);
        } else if (!searchPatientText.isEmpty()) {
            String selectQuery = "SELECT * FROM " + table +
                    " WHERE patient_name LIKE " + "'%" + search + "%' ORDER BY patient_name ASC";
            Log.d(TAG, "getAppointments: 3selectQuery : " + selectQuery);

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else {
            idCursor = db.rawQuery("SELECT * FROM tbl_appointments", new String[]{});

        }
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
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
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

    public boolean updatePreviousAppointmentDetails(String appointment_id, String visit_uuid,
                                                    String prev_slot_day, String prev_slot_date,
                                                    String prev_slot_time) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        String whereclause = "appointment_id=? and visit_uuid=?";
        db.beginTransaction();
        try {
            values.put("prev_slot_day", prev_slot_day);
            values.put("prev_slot_date", prev_slot_date);
            values.put("prev_slot_time", prev_slot_time);

            createdRecordsCount1 = db.update("tbl_appointments", values, whereclause, new String[]{appointment_id, visit_uuid});
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

}
