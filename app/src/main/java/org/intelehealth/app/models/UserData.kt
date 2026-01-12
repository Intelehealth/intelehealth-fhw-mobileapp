package org.intelehealth.app.models


data class UserData(
    val fName: String,
    val lName: String,
    val dob: String,
    val gender: String,
    val address: String,
    val pinCode: String,
    val abhaAddress: String,
    val abhaNumber: String,
    val phoneNumber: String
) {
    companion object {
        fun getEmptyDataAsDashes(): UserData {
            return UserData(
                fName = "-",
                lName = "-",
                dob = "-",
                gender = "-",
                address = "-",
                pinCode = "-",
                abhaAddress = "-",
                abhaNumber = "-",
                phoneNumber = "-"
            )
        }
    }
}