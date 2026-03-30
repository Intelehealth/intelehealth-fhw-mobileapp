package org.intelehealth.abdm.compare_data

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.reactivex.disposables.CompositeDisposable
import org.intelehealth.abdm.R
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.database.dao.PatientDao
import org.intelehealth.abdm.databinding.ActivityCompareDataBinding
import org.intelehealth.abdm.enums.AbdmOutcomes
import org.intelehealth.abdm.model.AbdmResult
import org.intelehealth.abdm.model.AbhaProfileResponse
import org.intelehealth.abdm.model.PatientDTO
import org.intelehealth.abdm.model.UserData
import org.intelehealth.abdm.utils.AbdmUtils
import org.intelehealth.abdm.utils.DialogUtils
import org.intelehealth.abdm.utils.StatusBarUtil.setStatusBarChanges

class CompareDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareDataBinding
    var patientsDAO: PatientDao = PatientDao()
    private val disposables = CompositeDisposable()

    var accessToken = ""
    var xToken = ""
    var txnId = ""
    var firstRequestFulfilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_compare_data)
        window.decorView.setBackgroundColor(Color.WHITE)
        this.enableEdgeToEdge()
        setStatusBarChanges(window, binding.root)

        val intentRx = this.intent
        if (intentRx != null) {
            accessToken = intentRx.getStringExtra("accessToken") ?: ""
            xToken = intentRx.getStringExtra("xToken") ?: ""
            txnId = intentRx.getStringExtra("txnId") ?: ""
            firstRequestFulfilled = intentRx.getBooleanExtra("firstRequestFulfilled", false)
        }

        val abhaProfileResponse =
            intent.getSerializableExtra(AbdmConstant.ABHA_PATIENT) as AbhaProfileResponse
        val patientDto = intent.getSerializableExtra(AbdmConstant.LOCAL_PATIENT) as PatientDTO

        val addressStringBuilder = StringBuilder()
        addressStringBuilder
            .append(if (!patientDto.address1.isNullOrEmpty()) patientDto.address1 else "")
            .append(if (!patientDto.address2.isNullOrEmpty()) ", " + patientDto.address2 else "")
            .append(if (!patientDto.cityvillage.isNullOrEmpty()) ", " + patientDto.cityvillage else "")
            .append(if (!patientDto.district.isNullOrEmpty()) ", " + patientDto.district else "")
            .append(if (!patientDto.stateprovince.isNullOrEmpty()) ", " + patientDto.stateprovince else "")

        val localUser = UserData(
            fName = patientDto.firstname,
            lName = patientDto.lastname,
            dob = patientDto.dateofbirth,
            gender = patientDto.gender,
            address = addressStringBuilder.toString().ifEmpty { "Not Found" },
            pinCode = (patientDto.postalcode ?: "").ifEmpty { "Not Found" },
            abhaAddress = (patientDto.abhaAddress ?: "").ifEmpty { "Not Found" },
            abhaNumber = (patientDto.abhaNumber ?: "").ifEmpty { "Not Found" },
            phoneNumber = (patientDto.phonenumber ?: "").ifEmpty { "Not Found" }
        )

        val abhaUser = UserData(
            fName = abhaProfileResponse.firstName,
            lName = abhaProfileResponse.lastName,
            dob = "${abhaProfileResponse.yearOfBirth}-${abhaProfileResponse.monthOfBirth}-${abhaProfileResponse.dayOfBirth}",
            gender = abhaProfileResponse.gender,
            address = (abhaProfileResponse.address ?: "").ifEmpty { "Not Found" },
            pinCode = (abhaProfileResponse.pincode ?: "").ifBlank { "Not Found" },
            abhaAddress = (abhaProfileResponse.preferredAbhaAddress ?: "").ifBlank { "Not Found" },
            abhaNumber = (abhaProfileResponse.abhaNumber ?: "").ifEmpty { "Not Found" },
            phoneNumber = (abhaProfileResponse.mobile ?: "").ifEmpty { "Not Found" }
        )

        binding.localData = localUser
        binding.abhaData = abhaUser

        // Auto-select if same
        autoSelectIfSame(binding.rbFNameLocal, binding.rbFNameAbha, localUser.fName, abhaUser.fName)
        autoSelectIfSame(binding.rbLNameLocal, binding.rbLNameAbha, localUser.lName, abhaUser.lName)
        autoSelectIfSame(binding.rbDobLocal, binding.rbDobAbha, localUser.dob, abhaUser.dob)
        autoSelectIfSame(
            binding.rbGenderLocal,
            binding.rbGenderAbha,
            localUser.gender,
            abhaUser.gender
        )
        autoSelectIfSame(
            binding.rbAddressLocal,
            binding.rbAddressAbha,
            localUser.address,
            abhaUser.address
        )

        autoSelectIfSame(
            binding.rbPinCodeLocal,
            binding.rbPinCodeAbha,
            localUser.pinCode,
            abhaUser.pinCode
        )

        autoSelectIfSame(
            binding.rbAbhaAddressLocal,
            binding.rbAbhaAddressAbha,
            localUser.abhaAddress,
            abhaUser.abhaAddress
        )

        autoSelectIfSame(
            binding.rbAbhaNumberLocal,
            binding.rbAbhaNumberAbha,
            localUser.abhaNumber,
            abhaUser.abhaNumber
        )

        autoSelectIfSame(
            binding.rbPhoneNumberLocal,
            binding.rbPhoneNumberAbha,
            localUser.phoneNumber,
            abhaUser.phoneNumber
        )

        binding.btnConfirm.setOnClickListener {
            displayConfirmationDialog(patientDto, abhaProfileResponse)
        }

        binding.btnEdit.setOnClickListener {
            // Toast.makeText(this, "Edit Manually clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayConfirmationDialog(
        patientDto: PatientDTO,
        abhaProfileResponse: AbhaProfileResponse
    ) {
        val dialogUtils = DialogUtils()

        dialogUtils.showCommonDialog(
            this@CompareDataActivity,
            R.drawable.ic_close_patient,
            getString(R.string.save_data),
            getString(R.string.do_you_want_to_ahead_and_save_the_patient_s_data),
            false,
            getString(R.string.save),
            getString(R.string.cancel),
            object : DialogUtils.CustomDialogListener {
                override fun onDialogActionDone(action: Int) {
                    if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                        savePatientData(patientDto, abhaProfileResponse)
                    }
                }
            }
        )
    }

    private fun savePatientData(
        patientDto: PatientDTO,
        abhaProfileResponse: AbhaProfileResponse
    ) {
        val updatedPatientDto = validatePatientData(patientDto, abhaProfileResponse) ?: return

        val isUpdated = patientsDAO.updatePatientWithABHA(
            updatedPatientDto
        )

        if (isUpdated) {
            val result = AbdmResult(
                outcome = AbdmOutcomes.NAVIGATE_TO_PATIENT_DETAILS_SCREEN_WITH_EXISTING_PATIENT_AFTER_COMPARISON,
                accessToken = accessToken,
                xToken = xToken,
                otpResponse = null,
                abhaResponse = abhaProfileResponse
            )

            val resultIntent = Intent();
            resultIntent.putExtra(AbdmConstant.INTENT_ABDM_RESULT, result)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(
                this,
                getString(R.string.unable_to_update_the_patient_try_again_later),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validatePatientData(
        patientDto: PatientDTO,
        abhaProfileResponse: AbhaProfileResponse
    ): PatientDTO? {
        val selectedFName = getSelectedRadioText(binding.rgFName)
        val selectedLName = getSelectedRadioText(binding.rgLName)
        val selectedDob = getSelectedRadioText(binding.rgDob)
        val selectedGender = getSelectedRadioText(binding.rgGender)
        val selectedAddress = getSelectedRadioText(binding.rgAddress)
        val selectedPinCode = getSelectedRadioText(binding.rgPinCode)
        val selectedAbhaAddress = getSelectedRadioText(binding.rgAbhaAddress)
        val selectedAbhaNumber = getSelectedRadioText(binding.rgAbhaNumber)
        val selectedPhoneNumber = getSelectedRadioText(binding.rgPhoneNumber)

        if (selectedFName.isEmpty() || selectedLName.isEmpty() || selectedDob.isEmpty() || selectedGender.isEmpty() || selectedAddress.isEmpty() || selectedPinCode.isEmpty() || selectedAbhaAddress.isEmpty() || selectedAbhaNumber.isEmpty() || selectedPhoneNumber.isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.please_select_all_the_fields_to_continue), Toast.LENGTH_SHORT
            ).show()

            return null
        }

        patientDto.firstname = selectedFName
        patientDto.lastname = selectedLName
        patientDto.dateofbirth = selectedDob
        patientDto.gender = selectedGender
        AbdmUtils.bifurcateAddress(selectedAddress, patientDto)
        patientDto.postalcode = selectedPinCode
        patientDto.abhaNumber = abhaProfileResponse.abhaNumber
        patientDto.abhaAddress = selectedAbhaAddress
        patientDto.abhaNumber = selectedAbhaNumber
        patientDto.phonenumber = selectedPhoneNumber

        return patientDto
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    private fun autoSelectIfSame(
        localRb: RadioButton,
        abhaRb: RadioButton,
        firstValue: String,
        secondValue: String
    ) {
        abhaRb.isChecked = true
        abhaRb.isClickable = false
        abhaRb.isFocusable = false
        localRb.isClickable = false
        localRb.isFocusable = false
    }

    private fun getSelectedRadioText(rg: RadioGroup): String {
        val selectedId = rg.checkedRadioButtonId
        return if (selectedId != -1) findViewById<RadioButton>(selectedId).text.toString() else ""
    }
}
