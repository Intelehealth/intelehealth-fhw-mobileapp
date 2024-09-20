package org.intelehealth.app.activities.bill

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Window
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.models.Patient
import org.intelehealth.app.utilities.UuidDictionary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class VisitSummaryBillUtils(
    private val mContext: Context,
    private val patient: Patient,
    private val billModel: VisitSummaryBillModel
) {
    private val TAG = "VisitSummaryBillUtils"
    private var receiptNum: String? = null
    private var receiptDate: String? = null
    private var receiptPaymentStatus = "NA"

    fun checkForOldBill(): String {
        var billEncounterUuid = ""
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        val encounterDAO = EncounterDAO()
        val encounterIDSelection = "visituuid = ? AND voided = ?"
        val encounterIDArgs = arrayOf(billModel.visitUuid, "0")
        val encounterCursor =
            db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null)

        encounterCursor?.use {
            while (it.moveToNext()) {
                val encounterTypeUuid = "7030c68e-eecc-4656-bb0a-e465aea6195f"
                if (encounterTypeUuid == it.getString(it.getColumnIndexOrThrow("encounter_type_uuid"))) {
                    billEncounterUuid = it.getString(it.getColumnIndexOrThrow("uuid"))
                }
            }
        }

        return billEncounterUuid
    }

    fun fetchBillDetails(billEncounterUuid: String) {
        val selectedTests = ArrayList<String>()
        val columns = arrayOf("value", "conceptuuid")
        val visitSelection = "encounteruuid = ? and voided = ?"
        val visitArgs = arrayOf(billEncounterUuid, "0")
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        val visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null)

        visitCursor?.use {
            while (it.moveToNext()) {
                val dbConceptID = it.getString(it.getColumnIndex("conceptuuid"))
                val dbValue = it.getString(it.getColumnIndex("value"))
                if (dbValue != null && dbValue != "0") {
                    parseBillData(selectedTests, dbConceptID, dbValue)
                }
            }
        }

        passIntent(selectedTests)
    }

    private fun parseBillData(selectedTests: ArrayList<String>, conceptId: String, value: String) {
        when (conceptId) {
            UuidDictionary.BILL_NUM -> {
                receiptNum = value
            }

            UuidDictionary.BILL_DATE -> {
                receiptDate = value
            }

            UuidDictionary.BILL_PAYMENT_STATUS -> {
                receiptPaymentStatus = value
            }

            UuidDictionary.BILL_PRICE_BP_ID -> {
                selectedTests.add(mContext.getString(R.string.visit_summary_bp))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_non_fasting))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_random))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_post_prandial))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_fasting))
            }

            UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID -> {
                selectedTests.add(mContext.getString(R.string.haemoglobin))
            }

            UuidDictionary.BILL_PRICE_URIC_ACID_ID -> {
                selectedTests.add(mContext.getString(R.string.uric_acid))
            }

            UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID -> {
                selectedTests.add(mContext.getString(R.string.total_cholestrol))
            }

            else -> {
                Log.i(TAG, "parseData: $value")
            }
        }
    }

    fun showTestConfirmationCustomDialog(checkedTests: BooleanArray) {
        val selectedTests = ArrayList<String>()
        val testNames = arrayOf(
            mContext.getString(R.string.visit_summary_bp),
            mContext.getString(R.string.blood_glucose_non_fasting),
            mContext.getString(R.string.blood_glucose_fasting),
            mContext.getString(R.string.blood_glucose_post_prandial),
            mContext.getString(R.string.blood_glucose_random),
            mContext.getString(R.string.uric_acid),
            mContext.getString(R.string.total_cholestrol),
            mContext.getString(R.string.haemoglobin)
        )

        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirm_test_dialog)

        val glucoseFast = dialog.findViewById<CheckBox>(R.id.glucose_f_CB)
        val glucoseNonFast = dialog.findViewById<CheckBox>(R.id.glucose_nf_CB)
        val glucoseRand = dialog.findViewById<CheckBox>(R.id.glucose_ran_CB)
        val glucosePpn = dialog.findViewById<CheckBox>(R.id.glucose_ppn_CB)
        val bp = dialog.findViewById<CheckBox>(R.id.bp_CB)
        val haemoglobin = dialog.findViewById<CheckBox>(R.id.haemoglobin_CB)
        val uricAcid = dialog.findViewById<CheckBox>(R.id.uric_acid_CB)
        val cholesterol = dialog.findViewById<CheckBox>(R.id.cholesterol_CB)
        val okDialog = dialog.findViewById<TextView>(R.id.dialog_ok_button)
        val cancelDialog = dialog.findViewById<TextView>(R.id.dialog_cancel_button)

        glucoseNonFast.isChecked = checkedTests[1]
        glucoseFast.isChecked = checkedTests[2]
        glucosePpn.isChecked = checkedTests[3]
        glucoseRand.isChecked = checkedTests[4]
        uricAcid.isChecked = checkedTests[5]
        cholesterol.isChecked = checkedTests[6]
        haemoglobin.isChecked = checkedTests[7]

        bp.setOnClickListener { view ->
            checkedTests[0] = (view as CompoundButton).isChecked
        }

        okDialog.setOnClickListener {
            for (i in checkedTests.indices) {
                if (checkedTests[i]) selectedTests.add(testNames[i])
            }
            receiptNum = generateReceiptNum()
            receiptDate = fetchSystemDateForBill()
            passIntent(selectedTests)
        }

        cancelDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun passIntent(selectedTests: ArrayList<String>) {
        val intent = Intent(mContext, BillGenerationActivity::class.java)
        intent.putExtra("patientName", "${patient.first_name} ${patient.last_name}")
        intent.putExtra("patientOpenMRSID", patient.openmrs_id)
        intent.putExtra("patientPhoneNum", patient.phone_number)
        intent.putExtra("patientVillage", patient.city_village)
        intent.putExtra("patientHideVisitID", billModel.hideVisitUUID)
        intent.putExtra("patientVisitID", billModel.visitUuid)
        intent.putExtra("testsList", selectedTests)
        intent.putExtra("receiptNum", receiptNum)
        intent.putExtra("receiptDate", receiptDate)
        intent.putExtra("visitType", billModel.visitType)
        intent.putExtra("billType", receiptPaymentStatus)
        mContext.startActivity(intent)
    }

    private fun generateReceiptNum(): String {
        val rnd = Random()
        val number = rnd.nextInt(9999)
        val first = patient.first_name[0]
        val last = patient.last_name[0]
        return "$first$last$number"
    }

    private fun fetchSystemDateForBill(): String {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return currentDate.format(Date())
    }
}