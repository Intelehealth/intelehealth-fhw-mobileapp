package org.intelehealth.app.ui.queue.repository

import org.intelehealth.app.ui.queue.FakeQueueDAO
import org.intelehealth.app.ui.queue.model.QueueRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueRepositoryTest {

    private fun fullRow(
        visitUuid: String? = "visit-1",
        position: Int = 1,
        firstName: String? = "Asha",
        lastName: String? = "Patil"
    ): Map<String, Any?> = linkedMapOf(
        "openmrs_id" to "OMRS-1",
        "first_name" to firstName,
        "middle_name" to "M",
        "last_name" to lastName,
        "gender" to "F",
        "date_of_birth" to "1990-01-15",
        "status" to "QUEUED",
        "position" to position,
        "visitUuid" to visitUuid,
        "waitedMinutes" to 12,
        "etaMinutes" to 7,
        "etaAt" to "2026-11-23T10:00:00Z",
        "connectedAt" to "2026-11-23T09:50:00Z",
        "patient_photo" to "/data/photo.jpg",
        "speciality" to "General Physician",
        "queueEntryId" to "entry-1"
    )

    @Test
    fun getQueueList_passesPagingThroughToDao() {
        val dao = FakeQueueDAO()
        QueueRepository(dao).getQueueList(50, 10)

        assertEquals(50, dao.lastLimit)
        assertEquals(10, dao.lastOffset)
    }

    @Test
    fun getQueueList_mapsEveryJoinedColumn() {
        val dao = FakeQueueDAO(
            queueRows = listOf(fullRow()),
            chiefComplaints = mapOf("visit-1" to "Fever")
        )

        val rows = QueueRepository(dao).getQueueList(50, 0)

        assertEquals(
            listOf(
                QueueRow(
                    openmrsId = "OMRS-1",
                    patientName = "Asha Patil",
                    gender = "F",
                    dateOfBirth = "1990-01-15",
                    status = "QUEUED",
                    position = 1,
                    visitUuid = "visit-1",
                    chiefComplaint = "Fever",
                    waitedMinutes = 12,
                    etaMinutes = 7,
                    etaAt = "2026-11-23T10:00:00Z",
                    connectedAt = "2026-11-23T09:50:00Z",
                    patientPhoto = "/data/photo.jpg",
                    speciality = "General Physician",
                    queueEntryId = "entry-1"
                )
            ),
            rows
        )
    }

    @Test
    fun getQueueList_keepsDaoOrder() {
        val dao = FakeQueueDAO(
            queueRows = listOf(
                fullRow(visitUuid = "v1", position = 1),
                fullRow(visitUuid = "v2", position = 2),
                fullRow(visitUuid = "v3", position = 3)
            )
        )

        val rows = QueueRepository(dao).getQueueList(50, 0)

        assertEquals(listOf(1, 2, 3), rows.map { it.position })
        assertEquals(listOf("v1", "v2", "v3"), rows.map { it.visitUuid })
    }

    @Test
    fun getQueueList_emptyWhenDaoHasNoRows() {
        assertTrue(QueueRepository(FakeQueueDAO()).getQueueList(50, 0).isEmpty())
    }

    @Test
    fun patientName_usesOnlyAvailableNameParts() {
        val dao = FakeQueueDAO(
            queueRows = listOf(
                fullRow(firstName = "Asha", lastName = null),
                fullRow(firstName = null, lastName = "Patil"),
                fullRow(firstName = null, lastName = null)
            )
        )

        val names = QueueRepository(dao).getQueueList(50, 0).map { it.patientName }

        assertEquals(listOf("Asha", "Patil", ""), names)
    }

    @Test
    fun nullAndMissingColumns_defaultToNullOrZero() {
        val row = linkedMapOf<String, Any?>(
            "status" to null,
            "position" to null,
            "visitUuid" to null
            // every other column absent from the cursor
        )
        val dao = FakeQueueDAO(queueRows = listOf(row))

        val mapped = QueueRepository(dao).getQueueList(50, 0).single()

        assertNull(mapped.openmrsId)
        assertNull(mapped.status)
        assertNull(mapped.etaAt)
        assertNull(mapped.connectedAt)
        assertNull(mapped.patientPhoto)
        assertNull(mapped.speciality)
        assertNull(mapped.queueEntryId)
        assertEquals(0, mapped.position)
        assertEquals(0, mapped.waitedMinutes)
        assertEquals(0, mapped.etaMinutes)
        assertEquals("", mapped.patientName)
    }

    @Test
    fun chiefComplaint_isNotLookedUpForNullOrBlankVisit() {
        val dao = FakeQueueDAO(
            queueRows = listOf(fullRow(visitUuid = null), fullRow(visitUuid = "  "), fullRow(visitUuid = "v9")),
            chiefComplaints = mapOf("v9" to "Cough")
        )

        val rows = QueueRepository(dao).getQueueList(50, 0)

        assertEquals(listOf("v9"), dao.chiefComplaintLookups)
        assertEquals(listOf(null, null, "Cough"), rows.map { it.chiefComplaint })
    }

    @Test
    fun getNextInQueue_mapsRowsAndResolvesChiefComplaint() {
        val dao = FakeQueueDAO(
            nextInQueueRows = listOf(fullRow(visitUuid = "next-1")),
            chiefComplaints = mapOf("next-1" to "Headache")
        )

        val rows = QueueRepository(dao).getNextInQueue()

        assertEquals(1, rows.size)
        assertEquals("next-1", rows[0].visitUuid)
        assertEquals("Headache", rows[0].chiefComplaint)
    }

    @Test
    fun getQueueRowForVisit_returnsMappedRowWithoutComplaintLookup() {
        val dao = FakeQueueDAO(rowsByVisit = mapOf("visit-1" to fullRow()))

        val row = QueueRepository(dao).getQueueRowForVisit("visit-1")

        assertEquals("Asha Patil", row?.patientName)
        assertEquals("entry-1", row?.queueEntryId)
        assertNull(row?.chiefComplaint)
        assertTrue(dao.chiefComplaintLookups.isEmpty())
    }

    @Test
    fun getQueueRowForVisit_nullWhenVisitNotQueued() {
        assertNull(QueueRepository(FakeQueueDAO()).getQueueRowForVisit("unknown"))
    }
}
