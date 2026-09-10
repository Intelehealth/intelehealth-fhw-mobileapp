package org.intelehealth.app.ui.queue.repository

import android.database.Cursor
import org.intelehealth.app.database.dao.QueueDAO
import org.intelehealth.app.ui.queue.model.QueueRow

/**
 * Repository for the patient queue screen. Wraps [QueueDAO] and exposes the
 * queue as a list of [QueueRow] domain models.
 *
 * The rows come from a single JOIN query ([QueueDAO.getQueueWithPatient]) that
 * already carries the patient display fields, so there is no per-row patient
 * lookup (no N+1). Each joined cursor row is mapped inline during that one pass.
 */
class QueueRepository(private val queueDao: QueueDAO) {

    /**
     * Reads the locally-synced queue joined with patient details, paged by
     * [limit]/[offset] (same convention as VisitsDAO's listing queries). Runs a
     * synchronous query, so call it off the main thread (the ViewModel does).
     */
    fun getQueueList(limit: Int, offset: Int): List<QueueRow> =
        queueDao.getQueueWithPatient(limit, offset) { cursor -> mapRow(cursor) }

    private fun mapRow(c: Cursor): QueueRow {
        val name = listOfNotNull(c.str("first_name"), c.str("last_name"))
            .joinToString(" ")
            .trim()
        return QueueRow(
            openmrsId = c.str("openmrs_id"),
            patientName = name,
            gender = c.str("gender"),
            dateOfBirth = c.str("date_of_birth"),
            status = c.str("status"),
            position = c.int("position"),
            chiefComplaint = c.str("chiefComplaint"),
            waitedMinutes = c.int("waitedMinutes"),
            etaMinutes = c.int("etaMinutes")
        )
    }

    /** Null-safe column read: null when the column is absent or NULL. */
    private fun Cursor.str(col: String): String? {
        val i = getColumnIndex(col)
        return if (i < 0 || isNull(i)) null else getString(i)
    }

    /** Null-safe column read: 0 when the column is absent or NULL. */
    private fun Cursor.int(col: String): Int {
        val i = getColumnIndex(col)
        return if (i < 0 || isNull(i)) 0 else getInt(i)
    }
}
