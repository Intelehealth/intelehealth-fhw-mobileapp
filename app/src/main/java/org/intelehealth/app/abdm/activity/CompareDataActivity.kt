package org.intelehealth.app.abdm.activity

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.R
import org.intelehealth.app.abdm.model.AbhaProfileResponse
import org.intelehealth.app.abdm.model.OTPVerificationResponse
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.databinding.ActivityCompareDataBinding
import org.intelehealth.app.models.Patient
import org.intelehealth.app.models.UserData
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.IntentKeys
import java.io.Serializable

class CompareDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareDataBinding
    var patientsDAO: PatientsDAO = PatientsDAO()
    private val disposables = CompositeDisposable()

    var accessToken = ""
    var xToken = ""
    var txnId = ""
    var firstRequestFulfilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_compare_data)


        val intentRx = this.intent

        if (intentRx != null) {
            accessToken = intentRx.getStringExtra("accessToken") ?: ""
            xToken = intentRx.getStringExtra("xToken") ?: ""
            txnId = intentRx.getStringExtra("txnId") ?: ""
            firstRequestFulfilled = intentRx.getBooleanExtra("firstRequestFulfilled", false)
        }

        val abhaProfileResponse =
            intent.getSerializableExtra(IntentKeys.ABHA_PATIENT) as AbhaProfileResponse
        val patientDto = intent.getSerializableExtra(IntentKeys.LOCAL_PATIENT) as PatientDTO

        val addressStringBuilder = StringBuilder()
        addressStringBuilder
            .append(if (!patientDto.address1.isNullOrEmpty()) patientDto.address1 else "")
            .append(if (!patientDto.address2.isNullOrEmpty()) ", " + patientDto.address2 else "")
        /*.append(if (!patientDto.cityvillage.isNullOrEmpty()) patientDto.cityvillage + ", " else "")
        .append(if (!patientDto.stateprovince.isNullOrEmpty()) patientDto.stateprovince + ", " else "")
        .append(if (!patientDto.postalcode.isNullOrEmpty()) patientDto.postalcode else "")*/

        val localUser = UserData(
            fName = patientDto.firstname,
            lName = patientDto.lastname,
            dob = patientDto.dateofbirth,
            gender = patientDto.gender,
            address = addressStringBuilder.toString().ifEmpty { "Not Found" },
            pinCode = (patientDto.postalcode ?: "").ifEmpty { "Not Found" },
            abhaAddress = (patientDto.abhaAddress ?: "").ifEmpty { "Not Found" },
            abhaNumber = (patientDto.abhaNumber ?: "").ifEmpty { "Not Found" }
        )

        val abhaUser = UserData(
            fName = abhaProfileResponse.firstName,
            lName = abhaProfileResponse.lastName,
            dob = "${abhaProfileResponse.yearOfBirth}-${abhaProfileResponse.monthOfBirth}-${abhaProfileResponse.dayOfBirth}",
            gender = abhaProfileResponse.gender,
            address = (abhaProfileResponse.address ?: "").ifEmpty { "Not Found" },
            pinCode = (abhaProfileResponse.pincode ?: "").ifBlank { "Not Found" },
            abhaAddress = (abhaProfileResponse.preferredAbhaAddress ?: "").ifBlank { "Not Found" },
            abhaNumber = (abhaProfileResponse.abhaNumber ?: "").ifEmpty { "Not Found" }
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
            this,
            R.drawable.close_patient_svg,
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
        SyncUtils().syncBackground()

        if (isUpdated) {
            Intent(this, PatientDetailActivity2::class.java).apply {
                putExtra("patientUuid", updatedPatientDto.uuid)
                putExtra(
                    "patientName",
                    updatedPatientDto.firstname + " " + updatedPatientDto.lastname
                )
                putExtra("tag", "newPatient")
                putExtra("hasPrescription", "false")

                val args = Bundle()
                args.putSerializable("patientDTO", updatedPatientDto as Serializable?)

                args.putString("accessToken", accessToken)
                args.putString("xToken", xToken)
                args.putString("txnId", txnId)
                putExtra("BUNDLE", args)
                startActivity(this)
                finish()
            }
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

        if (selectedFName.isEmpty() || selectedLName.isEmpty() || selectedDob.isEmpty() || selectedGender.isEmpty() || selectedAddress.isEmpty() || selectedPinCode.isEmpty() || selectedAbhaAddress.isEmpty() || selectedAbhaNumber.isEmpty()) {
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
        patientDto.address1 = selectedAddress
        patientDto.postalcode = selectedPinCode
        patientDto.abhaNumber = abhaProfileResponse.abhaNumber
        patientDto.abhaAddress = selectedAbhaAddress
        patientDto.abhaNumber = selectedAbhaNumber

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
        abhaRb.isEnabled = false
        localRb.isEnabled = false
    }

    private fun getSelectedRadioText(rg: RadioGroup): String {
        val selectedId = rg.checkedRadioButtonId
        return if (selectedId != -1) findViewById<RadioButton>(selectedId).text.toString() else ""
    }
}
