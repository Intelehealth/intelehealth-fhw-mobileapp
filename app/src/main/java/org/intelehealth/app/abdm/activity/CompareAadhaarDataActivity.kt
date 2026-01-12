package org.intelehealth.app.abdm.activity

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.intelehealth.app.R
import org.intelehealth.app.abdm.model.OTPVerificationResponse
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New
import org.intelehealth.app.databinding.ActivityCompareDataBinding
import org.intelehealth.app.models.UserData

class CompareAadhaarDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareDataBinding

    private lateinit var payload: OTPVerificationResponse
    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_compare_data)

        getIntentData()
        setUiData()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.btnConfirm.setOnClickListener {
            navigateToIdentificationScreen()
        }
    }

    private fun navigateToIdentificationScreen() {
        val intent = Intent(this@CompareAadhaarDataActivity, IdentificationActivity_New::class.java)
        intent.putExtra("payload", payload)
        intent.putExtra("accessToken", accessToken)
        startActivity(intent)
        finish()
    }

    private fun getIntentData() {
        if (intent != null) {
            payload = intent.getSerializableExtra("payload") as OTPVerificationResponse
            accessToken = intent.getStringExtra("accessToken") ?: ""
        }
    }

    private fun setUiData() {
        binding.abhaData = getUserData()
        binding.localData = UserData.getEmptyDataAsDashes()
        setAllAbhaRadioButtonsAsSelected()
    }

    private fun getUserData() = UserData(
        fName = payload.abhaProfile.firstName,
        lName = payload.abhaProfile.lastName,
        dob = payload.abhaProfile.dob,
        gender = payload.abhaProfile.gender,
        address = payload.abhaProfile.address,
        pinCode = payload.abhaProfile.pinCode,
        abhaAddress = payload.abhaProfile.phrAddress[0],
        abhaNumber = payload.abhaProfile.abhaNumber,
        phoneNumber = payload.abhaProfile.mobile
    )

    private fun setAllAbhaRadioButtonsAsSelected() {
        autoSelectItems(binding.rbFNameLocal, binding.rbFNameAbha)
        autoSelectItems(binding.rbLNameLocal, binding.rbLNameAbha)
        autoSelectItems(binding.rbDobLocal, binding.rbDobAbha)
        autoSelectItems(binding.rbGenderLocal, binding.rbGenderAbha)
        autoSelectItems(binding.rbAddressLocal, binding.rbAddressAbha)
        autoSelectItems(binding.rbPinCodeLocal, binding.rbPinCodeAbha)
        autoSelectItems(binding.rbAbhaAddressLocal, binding.rbAbhaAddressAbha)
        autoSelectItems(binding.rbAbhaNumberLocal, binding.rbAbhaNumberAbha)
    }

    private fun autoSelectItems(
        localRb: RadioButton,
        abhaRb: RadioButton,
    ) {
        abhaRb.isChecked = true
        abhaRb.isEnabled = false
        localRb.isEnabled = false
    }
}