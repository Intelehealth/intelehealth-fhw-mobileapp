package io.intelehealth.client.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.EncounterDTO;
import io.intelehealth.client.dto.LocationDTO;
import io.intelehealth.client.dto.ObsDTO;
import io.intelehealth.client.dto.PatientAttributeTypeMasterDTO;
import io.intelehealth.client.dto.PatientAttributesDTO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.dto.ProviderDTO;
import io.intelehealth.client.dto.VisitDTO;

@Database(entities = {PatientDTO.class, VisitDTO.class, EncounterDTO.class, ObsDTO.class, PatientAttributesDTO.class, PatientAttributeTypeMasterDTO.class, ProviderDTO.class, LocationDTO.class}, version = AppConstants.DATABASE_VERSION)
public abstract class InteleHealthRoomDatabase extends RoomDatabase {

    private static volatile InteleHealthRoomDatabase instance;

    public static InteleHealthRoomDatabase getDatabase(final Context context) {
        /*if instance is null*/
        if (instance == null) {
            synchronized (InteleHealthRoomDatabase.class) {
                /*if instance is null*/
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            InteleHealthRoomDatabase.class, AppConstants.DATABASE_NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract InteleHealthDao inteleHealthDao();
    //doc 2
}
