package org.intelehealth.app.ui.queue.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.intelehealth.app.ui.queue.FakeQueueDAO
import org.intelehealth.app.ui.queue.factory.QueueViewModelFactory
import org.intelehealth.app.ui.queue.model.QueueRow
import org.intelehealth.app.ui.queue.repository.QueueRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun row(visitUuid: String, position: Int) = linkedMapOf<String, Any?>(
        "first_name" to "Patient",
        "last_name" to visitUuid,
        "status" to "QUEUED",
        "position" to position,
        "visitUuid" to visitUuid
    )

    /** loadQueue posts from the IO dispatcher; wait for that emission. */
    private fun QueueViewModel.loadAndAwait(): List<QueueRow> {
        val latch = CountDownLatch(1)
        var result: List<QueueRow>? = null
        // observeForever replays any existing value synchronously; ignore it and
        // wait for the value posted by this load.
        var armed = false
        val observer = Observer<List<QueueRow>> {
            if (armed) {
                result = it
                latch.countDown()
            }
        }
        queue.observeForever(observer)
        try {
            armed = true
            loadQueue()
            assertTrue("queue was not posted in time", latch.await(5, TimeUnit.SECONDS))
        } finally {
            queue.removeObserver(observer)
        }
        return result!!
    }

    @Test
    fun queue_hasNoValueBeforeLoad() {
        val viewModel = QueueViewModel(QueueRepository(FakeQueueDAO()))
        assertNull(viewModel.queue.value)
    }

    @Test
    fun loadQueue_postsRepositoryRows() {
        val dao = FakeQueueDAO(
            queueRows = listOf(row("v1", 1), row("v2", 2)),
            chiefComplaints = mapOf("v1" to "Fever")
        )
        val viewModel = QueueViewModel(QueueRepository(dao))

        val rows = viewModel.loadAndAwait()

        assertEquals(listOf("v1", "v2"), rows.map { it.visitUuid })
        // The DAO returns "" (not null) for a visit with no chief complaint obs.
        assertEquals(listOf("Fever", ""), rows.map { it.chiefComplaint })
    }

    @Test
    fun loadQueue_requestsFirstPageOfFifty() {
        val dao = FakeQueueDAO()
        QueueViewModel(QueueRepository(dao)).loadAndAwait()

        assertEquals(50, dao.lastLimit)
        assertEquals(0, dao.lastOffset)
    }

    @Test
    fun loadQueue_postsEmptyListWhenEmpty() {
        val rows = QueueViewModel(QueueRepository(FakeQueueDAO())).loadAndAwait()
        assertTrue(rows.isEmpty())
    }

    @Test
    fun loadQueue_postsEmptyListWhenRepositoryThrows() {
        val dao = FakeQueueDAO(failWith = IllegalStateException("db closed"))

        val rows = QueueViewModel(QueueRepository(dao)).loadAndAwait()

        assertTrue(rows.isEmpty())
    }

    @Test
    fun loadQueue_reloadReflectsLatestData() {
        val dao = FakeQueueDAO(queueRows = listOf(row("v1", 1)))
        val viewModel = QueueViewModel(QueueRepository(dao))
        viewModel.loadAndAwait()

        dao.queueRows = listOf(row("v2", 1), row("v3", 2))
        val rows = viewModel.loadAndAwait()

        assertEquals(listOf("v2", "v3"), rows.map { it.visitUuid })
        assertEquals(rows, viewModel.queue.value)
    }

    @Test
    fun factory_createsQueueViewModel() {
        val factory = QueueViewModelFactory(QueueRepository(FakeQueueDAO()))
        val viewModel: Any = factory.create(QueueViewModel::class.java)
        assertTrue(viewModel is QueueViewModel)
    }
}
