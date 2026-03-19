package org.intelehealth.abdm.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import org.intelehealth.abdm.exception.DAOException;
import org.intelehealth.abdm.model.PatientDTO;
import org.intelehealth.abdm.utils.AbdmManager;
import org.intelehealth.abdm.utils.DateTimeUtils;
import org.intelehealth.abdm.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PatientDao {

    public boolean isPatientExistWithAbhaAddress(String patientOpenMRSUuid, String abhaAddress) {
        boolean isInserted = false;
        SQLiteDatabase db = AbdmManager.getDbClient();
        Cursor cursor = null;

        if (db != null) {
            try {
                cursor = db.rawQuery("SELECT COUNT(*) as count FROM tbl_patient WHERE openmrs_id = ? AND abha_address LIKE ? COLLATE NOCASE", new String[]{patientOpenMRSUuid, "%" + abhaAddress + "%"});

                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                    isInserted = count > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        return isInserted;
    }

    public String getPatientUuidByAbhaDetails(String abhaNumber) {
        String patientUuid = null;
        SQLiteDatabase db = AbdmManager.getDbClient();
        String query = "SELECT uuid FROM tbl_patient WHERE abha_number = ?";
        if (db != null) {
            try (Cursor cursor = db.rawQuery(query, new String[]{abhaNumber})) {
                if (cursor.moveToFirst()) {
                    patientUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return patientUuid;
    }

    public static PatientDTO getPatientDetailsForAbhaComparison(String patientUuid) {
        PatientDTO patientDTO = null;
        SQLiteDatabase db = AbdmManager.getDbClient();
        if (db != null) {
            final Cursor cursor = db.rawQuery("SELECT DISTINCT p.uuid, p.openmrs_id, p.first_name, p.middle_name, p.last_name, p.date_of_birth, p.phone_number, p.address1, " +
                            "p.address2, p.city_village, p.state_province, p.postal_code, p.country, p.gender, p.abha_number, p.abha_address, pa.value " +
                            "FROM tbl_patient AS p " +
                            "JOIN tbl_patient_attribute AS pa " +
                            "ON p.uuid = pa.patientuuid " +
                            "WHERE p.uuid = ? " +
                            "AND pa.person_attribute_type_uuid = ? " +
                            "AND (p.sync = 'TRUE' or p.sync = 1 or p.sync = 'true') " +
                            "ORDER BY p.modified_date DESC",
                    new String[]{patientUuid, "14d4f066-15f5-102d-96e4-000c29c2a5d7"}
            );


            if (cursor.moveToFirst()) {
                do {
                    patientDTO = new PatientDTO();
                    patientDTO.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    patientDTO.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    patientDTO.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    patientDTO.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    patientDTO.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    patientDTO.setAddress1(cursor.getString(cursor.getColumnIndexOrThrow("address1")));
                    patientDTO.setAddress2(cursor.getString(cursor.getColumnIndexOrThrow("address2")));
                    patientDTO.setCityvillage(cursor.getString(cursor.getColumnIndexOrThrow("city_village")));
                    patientDTO.setStateprovince(cursor.getString(cursor.getColumnIndexOrThrow("state_province")));
                    patientDTO.setCountry(cursor.getString(cursor.getColumnIndexOrThrow("country")));
                    patientDTO.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    patientDTO.setPostalcode(cursor.getString(cursor.getColumnIndexOrThrow("postal_code")));
                    patientDTO.setAbhaNumber(cursor.getString(cursor.getColumnIndexOrThrow("abha_number")));
                    patientDTO.setAbhaAddress(cursor.getString(cursor.getColumnIndexOrThrow("abha_address")));
                    patientDTO.setPhonenumber(cursor.getString(cursor.getColumnIndexOrThrow("value")));
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }
        return patientDTO;
    }

    public static PatientDTO getPatientDetailsByPhoneNum(String phoneNum, String gender, String dob, String firstName, String lastName, String postCode) {
        List<PatientDTO> modelList = new ArrayList<>();

        SQLiteDatabase db = AbdmManager.getDbClient();
        final Cursor cursor = db.rawQuery(
                "select distinct p.* from tbl_patient as p join tbl_patient_attribute as pa on p.uuid = pa.patientuuid where p.date_of_birth = ? and " +
                        " p.voided = 0 and pa.person_attribute_type_uuid = '14d4f066-15f5-102d-96e4-000c29c2a5d7' and pa.value = ? order by p.modified_date desc limit 1",
                new String[]{dob, phoneNum});

        try {
            if (cursor.moveToFirst()) {
                do {
                    PatientDTO model = new PatientDTO();
                    model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
                    model.setPatientPhoto(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                    model.setPostalcode(cursor.getString(cursor.getColumnIndexOrThrow("postal_code")));
                    model.setAddress1(cursor.getString(cursor.getColumnIndexOrThrow("address1")));
                    model.setAddress2(cursor.getString(cursor.getColumnIndexOrThrow("address2")));
                    model.setCityvillage(cursor.getString(cursor.getColumnIndexOrThrow("city_village")));
                    model.setStateprovince(cursor.getString(cursor.getColumnIndexOrThrow("state_province")));
                    model.setAbhaAddress(cursor.getString(cursor.getColumnIndexOrThrow("abha_address")));
                    model.setAbhaAddress(cursor.getString(cursor.getColumnIndexOrThrow("abha_number")));
                    modelList.add(model);
                } while (cursor.moveToNext());
            }
        } catch (DAOException e) {
            return null;
        }

        cursor.close();
        if (modelList.isEmpty()) return null;
        if (modelList.size() > 1) {
            List<PatientDTO> modelListAfterGender = new ArrayList<PatientDTO>();
            for (int i = 0; i < modelList.size(); i++) {
                if (modelList.get(i).getGender().equals(gender)) {
                    modelListAfterGender.add(modelList.get(i));
                }
            }

            if (modelListAfterGender.size() > 1) {
                // Step 1: Filter by Year
                List<PatientDTO> modelListAfterYear = new ArrayList<>();
                String dobYear = dob.substring(0, 4); // "1996"

                for (PatientDTO patient : modelListAfterGender) {
                    if (patient.getDateofbirth().substring(0, 4).equals(dobYear)) {
                        modelListAfterYear.add(patient);
                    }
                }

                if (modelListAfterYear.size() > 1) {
                    // Step 2: Filter by Month
                    List<PatientDTO> modelListAfterMonth = new ArrayList<>();
                    String dobMonth = dob.substring(5, 7); // "09"

                    for (PatientDTO patient : modelListAfterYear) {
                        if (patient.getDateofbirth().substring(5, 7).equals(dobMonth)) {
                            modelListAfterMonth.add(patient);
                        }
                    }

                    if (modelListAfterMonth.size() > 1) {
                        // Step 3: Filter by Day
                        List<PatientDTO> modelListAfterDay = new ArrayList<>();
                        String dobDay = dob.substring(8, 10); // "01"

                        for (PatientDTO patient : modelListAfterMonth) {
                            if (patient.getDateofbirth().substring(8, 10).equals(dobDay)) {
                                modelListAfterDay.add(patient);
                            }
                        }

                        if (modelListAfterDay.size() > 1 && firstName != null) {
                            // Step 4: Filter by First Name
                            List<PatientDTO> modelListAfterFirstName = new ArrayList<>();
                            for (PatientDTO patient : modelListAfterDay) {
                                if (patient.getFirstname().equalsIgnoreCase(firstName)) {
                                    modelListAfterFirstName.add(patient);
                                }
                            }

                            if (modelListAfterFirstName.size() > 1 && lastName != null) {
                                // Step 5: Filter by Last Name
                                List<PatientDTO> modelListAfterLastName = new ArrayList<>();
                                for (PatientDTO patient : modelListAfterFirstName) {
                                    if (patient.getLastname().equalsIgnoreCase(lastName)) {
                                        modelListAfterLastName.add(patient);
                                    }
                                }

                                if (modelListAfterLastName.size() > 1 && postCode != null) {
                                    List<PatientDTO> modelListAfterPostCode = new ArrayList<>();
                                    for (PatientDTO patient : modelListAfterLastName) {

                                        if (/*patient.getPostalcode() == null || */patient.getPostalcode().equalsIgnoreCase(postCode)) {
                                            modelListAfterPostCode.add(patient);
                                        }
                                    }
                                    if (!modelListAfterPostCode.isEmpty()) {
                                        return modelListAfterPostCode.get(0);
                                    } else {
                                        return null;
                                    }

                                } else {
                                    if (!modelListAfterLastName.isEmpty()) {
                                        return modelListAfterLastName.get(0);
                                    } else {
                                        return null;
                                    }
                                }


                            } else {
                                if (!modelListAfterFirstName.isEmpty()) {
                                    return modelListAfterFirstName.get(0);
                                } else {
                                    return null;
                                }
                            }

                        } else {
                            if (!modelListAfterDay.isEmpty()) {
                                return modelListAfterDay.get(0);
                            } else {
                                return null;
                            }
                        }

                    } else {
                        if (!modelListAfterMonth.isEmpty()) {
                            return modelListAfterMonth.get(0);
                        } else {
                            return null;
                        }
                    }

                } else {
                    if (!modelListAfterYear.isEmpty()) {
                        return modelListAfterYear.get(0);
                    } else {
                        return null;
                    }
                }
            } else {
                if (!modelListAfterGender.isEmpty()) {
                    return modelListAfterGender.get(0);
                } else {
                    return null;
                }
            }
        } else {
            if (!modelList.isEmpty()) {
                return modelList.get(0);
            } else {
                return null;
            }
        }
    }

    public static String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        SQLiteDatabase db = AbdmManager.getDbClient();
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? AND " +
                "person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7'", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                }
            }
        } catch (SQLException s) {
            throw new DAOException(s);
        }
        idCursor.close();
        return phone;
    }

    public boolean updatePatientWithABHA(@NonNull PatientDTO patientDTO) throws DAOException {
        boolean isCreated = true;
        SQLiteDatabase db = AbdmManager.getDbClient();
        assert db != null;
        ContentValues values = new ContentValues();
        String whereclause = "Uuid=?";
        db.beginTransaction();
        try {
            values.put("first_name", patientDTO.getFirstname());
            values.put("last_name", patientDTO.getLastname());
            values.put("phone_number", patientDTO.getPhonenumber());
            values.put("address1", patientDTO.getAddress1());
            values.put("date_of_birth", patientDTO.getDateofbirth());
            values.put("gender", patientDTO.getGender());
            values.put("modified_date", DateTimeUtils.getCurrentDateTime());
            values.put("abha_number", patientDTO.getAbhaNumber());
            values.put("abha_address", patientDTO.getAbhaAddress());
            values.put("postal_code", patientDTO.getPostalcode());
            values.put("sync", false);

            db.update("tbl_patient", values, whereclause, new String[]{patientDTO.getUuid()});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;
    }

    public static boolean isPatientPresentInLocal(String abhaNumber, String firstName, String lastName) {
        String queryAbhaNumberFormat = "%" + abhaNumber + "%";
        String patientUuid = getPatientUuidByAbhaNumber(queryAbhaNumberFormat, firstName, lastName);
        return patientUuid != null;
    }

    public static String getPatientUuidByAbhaNumber(String abhaNumberLastDigits, String firstName, String lastName) {
        String patientUuid = null;
        SQLiteDatabase db = AbdmManager.getDbClient();
        String query = "SELECT uuid FROM tbl_patient WHERE abha_number LIKE ? AND first_name = ? AND last_name = ?";
        if (db != null) {
            try (Cursor cursor = db.rawQuery(query, new String[]{abhaNumberLastDigits, firstName, lastName})) {
                if (cursor.moveToFirst()) {
                    patientUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return patientUuid;
    }
}
