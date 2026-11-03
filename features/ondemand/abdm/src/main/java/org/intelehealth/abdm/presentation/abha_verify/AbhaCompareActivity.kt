package org.intelehealth.abdm.presentation.abha_verify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.databinding.ActivityAbhaCompareBinding
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.result.AbdmResult

/**
 * Field-by-field reconciliation of the on-device patient against the verified ABHA profile.
 * Selection is locked to the ABHA side, so confirming simply saves the ABHA record onto the
 * existing local row and returns the result.
 */
@AndroidEntryPoint
class AbhaCompareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbhaCompareBinding
    private val viewModel: AbhaCompareViewModel by viewModels()

    private lateinit var abhaRecord: LocalPatientRecord
    private lateinit var xToken: String
    private lateinit var txnId: String
    private var shownErrorRes: Int? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAbhaCompareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()

        val local = intent.getParcelableExtra<LocalPatientRecord>(EXTRA_LOCAL)
        val abha = intent.getParcelableExtra<LocalPatientRecord>(EXTRA_ABHA)
        if (local == null || abha == null) {
            finish()
            return
        }
        abhaRecord = abha
        xToken = intent.getStringExtra(EXTRA_X_TOKEN).orEmpty()
        txnId = intent.getStringExtra(EXTRA_TXN_ID).orEmpty()

        binding.rvFields.adapter = CompareFieldAdapter(buildFields(local, abha))

        binding.btnBack.setOnClickListener { finish() }
        binding.btnConfirm.setOnClickListener { confirmSave() }

        observeState()
        observeEvents()
    }

    private fun applyInsets() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }
    }

    private fun buildFields(local: LocalPatientRecord, abha: LocalPatientRecord): List<CompareField> = listOf(
        field(R.string.abdm_field_first_name, local.firstName, abha.firstName),
        field(R.string.abdm_field_last_name, local.lastName, abha.lastName),
        field(R.string.abdm_field_dob, local.dateOfBirth, abha.dateOfBirth),
        field(R.string.abdm_field_gender, local.gender, abha.gender),
        field(R.string.abdm_field_address, local.address, abha.address),
        field(R.string.abdm_field_pin_code, local.pinCode, abha.pinCode),
        field(R.string.abdm_field_abha_address, local.abhaAddress, abha.abhaAddress),
        field(R.string.abdm_field_abha_number, local.abhaNumber, abha.abhaNumber),
        field(R.string.abdm_field_phone_number, local.phoneNumber, abha.phoneNumber),
    )

    private fun field(labelRes: Int, localValue: String, abhaValue: String) = CompareField(
        labelRes = labelRes,
        localValue = localValue.ifBlank { getString(R.string.abdm_not_found) },
        abhaValue = abhaValue.ifBlank { getString(R.string.abdm_not_found) },
    )

    private fun confirmSave() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Abdm_MaterialAlertDialog)
            .setTitle(R.string.abdm_compare_save_title)
            .setMessage(R.string.abdm_compare_save_message)
            .setPositiveButton(R.string.abdm_action_save) { _, _ ->
                viewModel.save(abhaRecord, xToken, txnId)
            }
            .setNegativeButton(R.string.abdm_action_cancel, null)
            .show()
    }

    private fun finishWithResult(result: AbdmResult) {
        setResult(Activity.RESULT_OK, Intent().putExtra(AbdmResult.EXTRA_ABDM_RESULT, result))
        finish()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { finishWithResult(it) }
            }
        }
    }

    private fun render(operation: UiState<Unit>) {
        val loading = operation is UiState.Loading
        binding.loadingOverlay.isVisible = loading
        binding.btnConfirm.isEnabled = !loading
        if (operation is UiState.Error) {
            if (operation.messageRes != shownErrorRes) {
                shownErrorRes = operation.messageRes
                Snackbar.make(binding.main, getString(operation.messageRes), Snackbar.LENGTH_LONG).show()
            }
        } else {
            shownErrorRes = null
        }
    }

    companion object {
        private const val EXTRA_LOCAL = "extra_local_record"
        private const val EXTRA_ABHA = "extra_abha_record"
        private const val EXTRA_X_TOKEN = "extra_x_token"
        private const val EXTRA_TXN_ID = "extra_txn_id"

        fun newIntent(
            context: Context,
            localRecord: LocalPatientRecord,
            abhaRecord: LocalPatientRecord,
            xToken: String,
            txnId: String,
        ): Intent = Intent(context, AbhaCompareActivity::class.java)
            .putExtra(EXTRA_LOCAL, localRecord)
            .putExtra(EXTRA_ABHA, abhaRecord)
            .putExtra(EXTRA_X_TOKEN, xToken)
            .putExtra(EXTRA_TXN_ID, txnId)
    }
}
