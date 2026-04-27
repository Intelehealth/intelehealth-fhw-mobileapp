package org.intelehealth.app.ayu.visit.pocdevice;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {VisitDataEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract VisitDao visitDao();

}
