package org.intelehealth.app.ui.queue

import android.database.Cursor
import org.intelehealth.app.database.dao.QueueDAO
import java.lang.reflect.Proxy

/**
 * In-memory [QueueDAO] for queue screen tests. Each row is a column -> value map
 * handed to the caller's mapper through a [fakeCursor], exactly like the real
 * single-pass cursor read, so the repository's column mapping is exercised.
 */
class FakeQueueDAO(
    var queueRows: List<Map<String, Any?>> = emptyList(),
    var nextInQueueRows: List<Map<String, Any?>> = emptyList(),
    var rowsByVisit: Map<String, Map<String, Any?>> = emptyMap(),
    var chiefComplaints: Map<String, String> = emptyMap(),
    var failWith: RuntimeException? = null
) : QueueDAO() {

    var lastLimit: Int? = null
    var lastOffset: Int? = null
    val chiefComplaintLookups = mutableListOf<String>()

    override fun <T> getQueueWithPatient(
        limit: Int,
        offset: Int,
        mapper: QueueRowMapper<T>?
    ): ArrayList<T> {
        failWith?.let { throw it }
        lastLimit = limit
        lastOffset = offset
        return mapAll(queueRows, mapper)
    }

    override fun <T> getNextInQueueWithPatient(mapper: QueueRowMapper<T>?): ArrayList<T> {
        failWith?.let { throw it }
        return mapAll(nextInQueueRows, mapper)
    }

    override fun <T> getQueueWithPatientByVisit(visitUuid: String?, mapper: QueueRowMapper<T>?): T? {
        failWith?.let { throw it }
        val row = rowsByVisit[visitUuid] ?: return null
        return mapper?.map(fakeCursor(row))
    }

    override fun getChiefComplaint(visitUuid: String?): String {
        chiefComplaintLookups += visitUuid.orEmpty()
        return chiefComplaints[visitUuid].orEmpty()
    }

    private fun <T> mapAll(rows: List<Map<String, Any?>>, mapper: QueueRowMapper<T>?): ArrayList<T> {
        if (mapper == null) return ArrayList()
        return rows.mapTo(ArrayList()) { mapper.map(fakeCursor(it)) }
    }
}

/**
 * Minimal [Cursor] over a single row: supports the column-index / null / string /
 * int reads the queue repository uses. Absent columns report index -1.
 */
fun fakeCursor(row: Map<String, Any?>): Cursor {
    val columns = row.keys.toList()
    return Proxy.newProxyInstance(
        Cursor::class.java.classLoader,
        arrayOf(Cursor::class.java)
    ) { _, method, args ->
        when (method.name) {
            "getColumnIndex" -> columns.indexOf(args[0] as String)
            "getColumnCount" -> columns.size
            "isNull" -> row[columns[args[0] as Int]] == null
            "getString" -> row[columns[args[0] as Int]]?.toString()
            "getInt" -> (row[columns[args[0] as Int]] as Number).toInt()
            "close" -> Unit
            else -> throw UnsupportedOperationException("FakeCursor.${method.name}")
        }
    } as Cursor
}
