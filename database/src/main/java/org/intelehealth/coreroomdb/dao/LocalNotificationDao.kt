package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.LocalNotification

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface LocalNotificationDao : CoreDao<LocalNotification> {
    @Query("SELECT * FROM tbl_notifications")
    override fun getAll(): LiveData<List<LocalNotification>>

    @Query("SELECT * FROM tbl_notifications WHERE uuid = :uuid")
    fun getNotificationByUuid(uuid: String): LiveData<LocalNotification>

    @Query("SELECT * FROM tbl_notifications WHERE notification_type = :type")
    fun getNotificationByType(type: String): LiveData<LocalNotification>

    @Query("UPDATE tbl_notifications SET isdeleted = 1 WHERE uuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("UPDATE tbl_notifications SET isdeleted = 1 WHERE notification_type = :type")
    suspend fun deleteByType(type: String)
}