package org.intelehealth.ekalarogya.database.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.models.dto.LocationDTO;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_DistrictModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_LocationModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_StateModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_VillageModel;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.List;
import java.util.UUID;

public class NewLocationDao {

    long createdRecordsCount = 0;

    public boolean insertSetupLocations(Setup_LocationModel location) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            List<Setup_StateModel> stateList=location.getStates();
            if(stateList.size()>0) {
                for (int i = 0; i < stateList.size(); i++) {
                    String stateName = stateList.get(i).getName();
                    List<Setup_DistrictModel> districtList = stateList.get(i).getDistricts();
                    if (districtList != null && districtList.size()>0) {
                        for(int j=0;j<districtList.size();j++) {
                            String districtName = districtList.get(j).getName();
                            List<Setup_VillageModel> villageList = districtList.get(j).getVillages();
                            if (villageList != null && villageList.size() > 0) {
                                for(int k=0;k<villageList.size();k++) {
                                    String villageName = villageList.get(k).getName();
                                    //------enter into db with name and state, district and village only----
                                    createSetupLocation(stateName, districtName, villageName, db);
                                }
                            } else {
                                //------enter into db with name and state and district only----
                                createSetupLocation(stateName, districtName, "", db);
                            }
                        }

                    } else {
                        //------enter into db with name and stateonly----
                        createSetupLocation(stateName,"","",db);
                    }
                }
            }
            //values.put("uuid", UUID.randomUUID().toString());
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;
    }

    private boolean createSetupLocation(String stateName,String districtName, String villageName, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("country", "India");
            if(villageName.length()>0){
                values.put("name", villageName);
                values.put("state", stateName);
                values.put("district", districtName);
                values.put("village", villageName);
            }else if(districtName.length()>0){
                values.put("name", districtName);
                values.put("state", stateName);
                values.put("district", districtName);
            }else if(stateName.length()>0){
                values.put("name", stateName);
                values.put("state", stateName);
            }
            values.put("locationuuid", UUID.randomUUID().toString());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            createdRecordsCount = db.insertWithOnConflict("tbl_location_new", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;
    }
}
