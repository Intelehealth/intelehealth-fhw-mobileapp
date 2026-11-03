package org.intelehealth.abdm.presentation.abha_verify

import org.intelehealth.abdm.domain.model.AbhaProfile
import org.junit.Assert.assertEquals
import org.junit.Test

// Fictional data only.
class AbhaVerifyResultMapperTest {

    @Test
    fun `comparison record carries the ABHA middle name`() {
        val record = profile(middleName = "Ramesh").toComparisonRecord("uuid-1", "10XYZ-2")

        assertEquals("Ramesh", record.middleName)
        assertEquals("1980-03-05", record.dateOfBirth)
    }

    @Test
    fun `comparison record carries the ABHA photo`() {
        val record = profile(photo = "/9j/fakeJpegBase64").toComparisonRecord("uuid-1", "10XYZ-2")

        assertEquals("/9j/fakeJpegBase64", record.profilePhoto)
    }

    @Test
    fun `missing middle name maps to blank`() {
        assertEquals("", profile(middleName = null).toComparisonRecord("uuid-1", "10XYZ-2").middleName)
    }

    @Test
    fun `year-only date of birth maps to blank`() {
        val record = profile(day = "", month = "").toComparisonRecord("uuid-1", "10XYZ-2")

        assertEquals("", record.dateOfBirth)
    }

    private fun profile(
        middleName: String? = "Ramesh",
        day: String = "5",
        month: String = "3",
        photo: String? = null,
    ) = AbhaProfile(
        abhaNumber = "91-1234-5678-9012",
        preferredAbhaAddress = "kamla.pawar@sbx",
        mobile = "9876543210",
        firstName = "Kamla",
        middleName = middleName,
        lastName = "Pawar",
        yearOfBirth = "1980",
        monthOfBirth = month,
        dayOfBirth = day,
        gender = "F",
        profilePhoto = photo,
        address = "Village Road",
        pinCode = "422001",
        stateName = "MAHARASHTRA",
    )
}
