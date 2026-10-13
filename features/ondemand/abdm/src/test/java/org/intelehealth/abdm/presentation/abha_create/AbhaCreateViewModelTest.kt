package org.intelehealth.abdm.presentation.abha_create

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.config.AbdmPatientLocalStore
import org.intelehealth.abdm.config.AbdmSessionProvider
import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.domain.model.EnrolledAbhaProfile
import org.intelehealth.abdm.domain.model.EnrolledAbhaToken
import org.intelehealth.abdm.domain.model.PatientServerData
import org.intelehealth.abdm.domain.model.RequestedOtp
import org.intelehealth.abdm.domain.repository.AbhaCreateRepository
import org.intelehealth.abdm.domain.repository.PatientRepository
import org.intelehealth.abdm.result.AbdmOutcomes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers how Create ABHA decides between linking to a patient already held and registering a new one.
 * All patient data here is fictional.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AbhaCreateViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `existing ABHA unknown to the server links to the local patient matched on phone and DOB`() =
        runTest(dispatcher) {
            val store = FakeLocalStore(match = LOCAL_PATIENT)
            val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
            val vm = viewModel(existingAbhaSession(), patients, store)
            val events = collectEvents(vm)

            vm.completeAadhaarVerification()

            assertEquals(listOf(LookupArgs(ABHA_NUMBER, MOBILE, "1980-03-05", "Kamla")), store.lookups)
            assertTrue(events.last() is AbhaCreateEvent.ShowAddressChecklist)

            vm.onAbhaAddressSelected(EXISTING_ADDRESS)

            assertEquals(listOf(LOCAL_PATIENT.uuid to EXISTING_ADDRESS), patients.linkedIdentifiers)
            assertEquals(listOf(Triple(LOCAL_PATIENT.uuid, ABHA_NUMBER, EXISTING_ADDRESS)), store.links)
            val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
            assertEquals(
                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_EXISTING_DETAILS_FOR_CREATION,
                result.outcome,
            )
            assertEquals(LOCAL_PATIENT.uuid, result.uuid)
            assertEquals(LOCAL_PATIENT.openMrsId, result.openMrsId)
        }

    // NAS-1817 reproduced case: isNew=false and check/id answers uuid/openmrsid "NA".
    @Test
    fun `existing ABHA the server answers NA for also falls back to the local patient`() =
        runTest(dispatcher) {
            val store = FakeLocalStore(match = LOCAL_PATIENT)
            val patients = FakePatientRepository(PatientServerData(uuid = "NA", openMrsId = "NA"))
            val vm = viewModel(existingAbhaSession(), patients, store)
            val events = collectEvents(vm)

            vm.completeAadhaarVerification()
            vm.onAbhaAddressSelected(EXISTING_ADDRESS)

            assertEquals(listOf(LOCAL_PATIENT.uuid to EXISTING_ADDRESS), patients.linkedIdentifiers)
            val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
            assertEquals(
                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_EXISTING_DETAILS_FOR_CREATION,
                result.outcome,
            )
            assertEquals(LOCAL_PATIENT.uuid, result.uuid)
            assertEquals(LOCAL_PATIENT.openMrsId, result.openMrsId)
        }

    @Test
    fun `existing ABHA the server answers NA for with no local patient still registers a new patient`() =
        runTest(dispatcher) {
            val store = FakeLocalStore(match = null)
            val patients = FakePatientRepository(PatientServerData(uuid = "NA", openMrsId = "NA"))
            val vm = viewModel(existingAbhaSession(), patients, store)
            val events = collectEvents(vm)

            vm.completeAadhaarVerification()
            assertTrue(events.last() is AbhaCreateEvent.ShowAddressChecklist)
            vm.onAbhaAddressSelected(EXISTING_ADDRESS)

            assertRegistersNewPatient(events, patients, store)
        }

    @Test
    fun `local patient with a different first name is not linked`() =
        assertNotLinked(LOCAL_PATIENT.copy(firstName = "Rekha"))

    @Test
    fun `local patient with a different gender is not linked`() =
        assertNotLinked(LOCAL_PATIENT.copy(gender = "M"))

    @Test
    fun `local patient already holding a different ABHA is not linked`() =
        assertNotLinked(LOCAL_PATIENT.copy(abhaNumber = "91-9999-8888-7777"))

    @Test
    fun `first name is compared ignoring case and extra spaces`() = runTest(dispatcher) {
        val store = FakeLocalStore(match = LOCAL_PATIENT.copy(firstName = " kamla  "))
        val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
        val vm = viewModel(existingAbhaSession(), patients, store)
        val events = collectEvents(vm)

        vm.completeAadhaarVerification()
        vm.onAbhaAddressSelected(EXISTING_ADDRESS)

        val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
        assertEquals(LOCAL_PATIENT.uuid, result.uuid)
    }

    @Test
    fun `local patient already holding this ABHA is linked`() = runTest(dispatcher) {
        val store = FakeLocalStore(match = LOCAL_PATIENT.copy(firstName = "Kamlabai", abhaNumber = ABHA_NUMBER))
        val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
        val vm = viewModel(existingAbhaSession(), patients, store)
        val events = collectEvents(vm)

        vm.completeAadhaarVerification()
        vm.onAbhaAddressSelected(EXISTING_ADDRESS)

        val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
        assertEquals(LOCAL_PATIENT.uuid, result.uuid)
    }

    @Test
    fun `new address requested for an existing ABHA is linked to the local patient`() =
        runTest(dispatcher) {
            val store = FakeLocalStore(match = LOCAL_PATIENT)
            val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
            val vm = viewModel(existingAbhaSession(), patients, store)
            val events = collectEvents(vm)

            vm.completeAadhaarVerification()
            vm.onCreateNewAddressRequested()
            assertTrue(events.last() is AbhaCreateEvent.NavigateToSuggestions)
            vm.onSuggestionsAddressChosen(NEW_ADDRESS)

            assertEquals(listOf(LOCAL_PATIENT.uuid to NEW_ADDRESS), patients.linkedIdentifiers)
            val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
            assertEquals(
                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_AFTER_ABHA_SUGGESTIONS_FOR_CREATION,
                result.outcome,
            )
            assertEquals(LOCAL_PATIENT.uuid, result.uuid)
        }

    @Test
    fun `existing ABHA matching nobody on the server or locally still registers a new patient`() =
        runTest(dispatcher) {
            val store = FakeLocalStore(match = null)
            val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
            val vm = viewModel(existingAbhaSession(), patients, store)
            val events = collectEvents(vm)

            vm.completeAadhaarVerification()

            val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
            assertEquals(
                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION,
                result.outcome,
            )
            assertNull(result.uuid)
            assertTrue(patients.linkedIdentifiers.isEmpty())
            assertTrue(store.links.isEmpty())
        }

    @Test
    fun `server match wins and the local lookup is not consulted`() = runTest(dispatcher) {
        val store = FakeLocalStore(match = LOCAL_PATIENT)
        val patients = FakePatientRepository(PatientServerData(uuid = SERVER_UUID, openMrsId = "10ABC-1"))
        val vm = viewModel(existingAbhaSession(), patients, store)
        val events = collectEvents(vm)

        vm.completeAadhaarVerification()
        vm.onAbhaAddressSelected(EXISTING_ADDRESS)

        assertTrue(store.lookups.isEmpty())
        assertEquals(listOf(SERVER_UUID to EXISTING_ADDRESS), patients.linkedIdentifiers)
        val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
        assertEquals(SERVER_UUID, result.uuid)
        assertEquals("10ABC-1", result.openMrsId)
    }

    @Test
    fun `freshly enrolled ABHA keeps resolving locally without asking the server`() =
        runTest(dispatcher) {
            val store = FakeLocalStore(match = LOCAL_PATIENT)
            val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
            val vm = viewModel(newAbhaSession(), patients, store)
            val events = collectEvents(vm)

            vm.completeAadhaarVerification()

            assertTrue(patients.checkedAbhaNumbers.isEmpty())
            assertTrue(events.last() is AbhaCreateEvent.NavigateToSuggestions)

            vm.onSuggestionsAddressChosen(NEW_ADDRESS)

            assertEquals(listOf(LOCAL_PATIENT.uuid to NEW_ADDRESS), patients.linkedIdentifiers)
            val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
            assertEquals(LOCAL_PATIENT.uuid, result.uuid)
        }

    // Server miss with this tablet candidate must fall through to a new patient.
    private fun assertNotLinked(candidate: LocalPatientRecord) = runTest(dispatcher) {
        val store = FakeLocalStore(match = candidate)
        val patients = FakePatientRepository(PatientServerData(uuid = null, openMrsId = null))
        val vm = viewModel(existingAbhaSession(), patients, store)
        val events = collectEvents(vm)

        vm.completeAadhaarVerification()

        assertRegistersNewPatient(events, patients, store)
    }

    private fun assertRegistersNewPatient(
        events: List<AbhaCreateEvent>,
        patients: FakePatientRepository,
        store: FakeLocalStore,
    ) {
        val result = (events.last() as AbhaCreateEvent.CompleteWithResult).result
        assertEquals(AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION, result.outcome)
        assertNull(result.uuid)
        assertTrue(patients.linkedIdentifiers.isEmpty())
        assertTrue(store.links.isEmpty())
    }

    /** Snackbars are incidental to routing, so only the routing events are kept. */
    private fun TestScope.collectEvents(vm: AbhaCreateViewModel): List<AbhaCreateEvent> {
        val events = mutableListOf<AbhaCreateEvent>()
        backgroundScope.launch(dispatcher) {
            vm.events.collect { if (it !is AbhaCreateEvent.ShowSnackbar) events += it }
        }
        return events
    }

    private fun AbhaCreateViewModel.completeAadhaarVerification() {
        onConsentChecked(true)
        onInputChanged(InputField.Aadhaar, VALID_AADHAAR)
        onSendAadhaarOtpClicked()
        onInputChanged(InputField.AadhaarOtp, "123456")
        onInputChanged(InputField.Mobile, MOBILE)
        onVerifyAadhaarOtpClicked()
    }

    private fun viewModel(
        session: AbhaCreateSession,
        patients: FakePatientRepository,
        store: FakeLocalStore,
    ) = AbhaCreateViewModel(
        createRepository = FakeCreateRepository(session),
        patientRepository = patients,
        patientLocalStore = store,
        sessionProvider = object : AbdmSessionProvider {
            override fun getEncodedCredentials(): String = "credentials"
            override fun getLocationUuid(): String = LOCATION_UUID
        },
        config = object : AbdmConfig {
            override val baseUrl: String = "https://example.test/"
            override val abhaAddressSuffix: String = SUFFIX
        },
    )

    private data class LookupArgs(
        val abhaNumber: String,
        val phoneNumber: String,
        val dateOfBirth: String,
        val firstName: String,
    )

    private class FakeCreateRepository(private val session: AbhaCreateSession) : AbhaCreateRepository {
        override suspend fun verifyMobileForEnrollment(otp: String, txnId: String, mobileNumber: String) =
            Result.failure<RequestedOtp>(UnsupportedOperationException())

        override suspend fun requestAadhaarOtp(value: String, scope: String) =
            Result.success(RequestedOtp(txnId = "txn-otp", message = "OTP sent"))

        override suspend fun requestMobileOtp(mobileNumber: String, txnId: String) =
            Result.failure<RequestedOtp>(UnsupportedOperationException())

        override suspend fun verifyAadhaarOtp(otp: String, txnId: String, mobileNumber: String) =
            Result.success(session)
    }

    private class FakePatientRepository(private val serverData: PatientServerData) : PatientRepository {
        val checkedAbhaNumbers = mutableListOf<String>()
        val linkedIdentifiers = mutableListOf<Pair<String, String>>()

        override suspend fun checkExistingUser(abhaNumber: String): Result<PatientServerData> {
            checkedAbhaNumbers += abhaNumber
            return Result.success(serverData)
        }

        override suspend fun updatePatientIdentifier(
            patientUuid: String,
            identifier: String,
            identifierType: String,
            location: String,
        ): Result<Unit> {
            linkedIdentifiers += patientUuid to identifier
            return Result.success(Unit)
        }
    }

    private class FakeLocalStore(private val match: LocalPatientRecord?) : AbdmPatientLocalStore {
        val lookups = mutableListOf<LookupArgs>()
        val links = mutableListOf<Triple<String, String?, String>>()

        override suspend fun isPatientLinkedWithAbhaAddress(openMrsId: String, abhaAddress: String) = false

        override suspend fun linkAbha(patientUuid: String, abhaNumber: String?, abhaAddress: String) {
            links += Triple(patientUuid, abhaNumber, abhaAddress)
        }

        override suspend fun findPatientForComparison(
            abhaNumber: String,
            phoneNumber: String,
            dateOfBirth: String,
            firstName: String,
        ): LocalPatientRecord? {
            lookups += LookupArgs(abhaNumber, phoneNumber, dateOfBirth, firstName)
            return match
        }

        override suspend fun isPatientRegisteredLocally(
            abhaNumberLastFour: String,
            firstName: String,
            lastName: String,
        ) = false

        override suspend fun savePatientAfterComparison(record: LocalPatientRecord) = true
    }

    private companion object {
        const val VALID_AADHAAR = "234567890124"
        const val MOBILE = "9876543210"
        const val SUFFIX = "@sbx"
        const val ABHA_NUMBER = "91-1234-5678-9012"
        const val EXISTING_ADDRESS = "kamla.pawar@sbx"
        const val NEW_ADDRESS = "kamlapawar80@sbx"
        const val SERVER_UUID = "server-patient-uuid"
        const val LOCATION_UUID = "location-uuid"

        val LOCAL_PATIENT = LocalPatientRecord(
            uuid = "local-patient-uuid",
            openMrsId = "10XYZ-2",
            firstName = "Kamla",
            lastName = "Pawar",
            dateOfBirth = "1980-03-05",
            gender = "F",
            phoneNumber = "+91$MOBILE",
        )

        fun profile(phrAddresses: List<String>) = EnrolledAbhaProfile(
            firstName = "Kamla",
            lastName = "Pawar",
            middleName = null,
            dateOfBirth = "05-03-1980",
            gender = "F",
            address = "Village Road",
            profilePhoto = null,
            mobile = MOBILE,
            phrAddresses = phrAddresses,
            pinCode = "422001",
            abhaNumber = ABHA_NUMBER,
            preferredAbhaAddress = phrAddresses.firstOrNull(),
        )

        /** ABDM already held an ABHA for this Aadhaar, so enrolment answers isNew=false. */
        fun existingAbhaSession() = AbhaCreateSession(
            txnId = "txn-enrol",
            profile = profile(listOf(EXISTING_ADDRESS)),
            isNew = false,
            enrolledAbhaToken = EnrolledAbhaToken(token = "token", expiresIn = 1800),
        )

        /** A just-minted ABHA whose only address is the auto-generated default. */
        fun newAbhaSession() = AbhaCreateSession(
            txnId = "txn-enrol",
            profile = profile(listOf("${ABHA_NUMBER.replace("-", "")}$SUFFIX")),
            isNew = true,
            enrolledAbhaToken = EnrolledAbhaToken(token = "token", expiresIn = 1800),
        )
    }
}
