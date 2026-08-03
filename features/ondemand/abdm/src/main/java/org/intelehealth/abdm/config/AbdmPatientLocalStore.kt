package org.intelehealth.abdm.config

/**
 * Bridge to the host's local patient store.
 *
 * ABHA is modelled by the server as patient identifiers, so it lives on the host patient record.
 * The patient database belongs to the host app, so the host implements this. Implementations must
 * be safe to call from a coroutine (do DB work off the main thread).
 */
interface AbdmPatientLocalStore {

    /**
     * Whether the already-known HMIS patient (by [openMrsId]) is already linked locally to
     * [abhaAddress] — lets abdm skip a redundant server identifier update.
     */
    suspend fun isPatientLinkedWithAbhaAddress(openMrsId: String, abhaAddress: String): Boolean

    /**
     * Links the chosen ABHA identifiers to the existing local patient row ([patientUuid]) so the
     * next sync pushes them as patient identifiers. Implementations should not throw; failures are
     * best-effort and must not break the create flow.
     */
    suspend fun linkAbha(patientUuid: String, abhaNumber: String?, abhaAddress: String)

    /**
     * Finds the local patient to reconcile against during ABHA verification: matched first by
     * [abhaNumber], falling back to [phoneNumber] together with [dateOfBirth] (most-recently-modified
     * wins). Returns null when no local copy exists. Implementations must not throw.
     *
     * [dateOfBirth] arrives already normalised to yyyy-MM-dd, since only this module knows the shape
     * ABDM returns it in. It is blank when the profile carried no usable date; implementations must
     * then skip the phone fallback rather than match on the number alone, because a phone is often
     * shared across a household and the compare screen writes onto whichever row is returned.
     *
     * How [phoneNumber] is matched is the host's business — it is passed as ABDM supplies it, and the
     * host holds it in its own format.
     */
    suspend fun findPatientForComparison(
        abhaNumber: String,
        phoneNumber: String,
        dateOfBirth: String,
    ): LocalPatientRecord?

    /**
     * Whether a local patient already exists for an ABHA account shown in the multiple-accounts
     * picker — drives the "Registered / Not registered with Intelehealth" status line. Mirrors the
     * legacy query: abha_number LIKE %[abhaNumberLastFour]% AND first_name = [firstName] AND
     * last_name = [lastName]. Implementations must not throw.
     */
    suspend fun isPatientRegisteredLocally(
        abhaNumberLastFour: String,
        firstName: String,
        lastName: String,
    ): Boolean

    /**
     * Persists the merged record chosen on the compare screen back to the local patient row and
     * marks it unsynced. Returns true on success.
     */
    suspend fun savePatientAfterComparison(record: LocalPatientRecord): Boolean
}
