package org.intelehealth.abdm.presentation.abha_verify

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.ActivityAbhaVerifyBinding
import org.intelehealth.abdm.presentation.abha_create.AbhaCreateActivity
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.presentation.common.showAbdmSnackbar
import org.intelehealth.abdm.presentation.consent.ConsentDialog
import org.intelehealth.abdm.result.AbdmResult
import org.intelehealth.abdm.util.NetworkConnection

@AndroidEntryPoint
class AbhaVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbhaVerifyBinding
    private val viewModel: AbhaVerifyViewModel by viewModels()

    private var lastStep: AbhaVerifyStep? = null

    private val compareLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            @Suppress("DEPRECATION")
            val abdmResult = result.data?.getParcelableExtra<AbdmResult>(AbdmResult.EXTRA_ABDM_RESULT)
                ?: return@registerForActivityResult
            finishWithResult(abdmResult)
        }

    private val createLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            @Suppress("DEPRECATION")
            val abdmResult = result.data?.getParcelableExtra<AbdmResult>(AbdmResult.EXTRA_ABDM_RESULT)
                ?: return@registerForActivityResult
            finishWithResult(abdmResult)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAbhaVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()

        viewModel.seedPatientName(intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty())

        setupTabs()
        setupInputListeners()
        setupActions()
        registerDialogListeners()
        observeState()
        observeEvents()

        if (!NetworkConnection.isOnline(this)) showNoInternetDialog()
    }

    private fun applyInsets() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            if (ime.bottom > 0) v.findFocus()?.let { focused ->
                focused.post { focused.requestRectangleOnScreen(Rect(0, 0, focused.width, focused.height), false) }
            }
            insets
        }
    }

    private fun showNoInternetDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Abdm_MaterialAlertDialog)
            .setIcon(R.drawable.abdm_ic_alert)
            .setTitle(R.string.error_network)
            .setMessage(R.string.you_need_an_active_internet_connection_to_use_this_feature)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .show()
    }

    private fun setupTabs() {
        binding.btnTabAadhaar.setOnClickListener { viewModel.onMethodSelected(AbhaVerifyMethod.Aadhaar) }
        binding.btnTabMobile.setOnClickListener { viewModel.onMethodSelected(AbhaVerifyMethod.Mobile) }
        binding.btnTabAbha.setOnClickListener { viewModel.onMethodSelected(AbhaVerifyMethod.Abha) }
    }

    private fun setupInputListeners() {
        binding.etAbhaAddress.hint = getString(R.string.abdm_hint_abha_address, viewModel.abhaAddressSuffix)
        binding.etAadhaarNumber.doAfterTextChanged {
            viewModel.onInputChanged(AbhaVerifyInputField.Aadhaar, it?.toString().orEmpty())
        }
        binding.etMobileNumber.doAfterTextChanged {
            viewModel.onInputChanged(AbhaVerifyInputField.Mobile, it?.toString().orEmpty())
        }
        binding.etAbhaNumber.doAfterTextChanged {
            viewModel.onInputChanged(AbhaVerifyInputField.AbhaNumber, it?.toString().orEmpty())
        }
        binding.etAbhaAddress.doAfterTextChanged {
            viewModel.onInputChanged(AbhaVerifyInputField.AbhaAddress, it?.toString().orEmpty())
        }
        binding.otpView.doAfterTextChanged {
            viewModel.onInputChanged(AbhaVerifyInputField.Otp, it?.toString().orEmpty())
        }
        binding.cbTermsAndConditions.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onConsentChecked(isChecked)
            if (isChecked) showConsentDialog()
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvResend.setOnClickListener { viewModel.onResendOtpClicked() }
        binding.btnPrimary.setOnClickListener { viewModel.onPrimaryClicked() }
    }

    private fun registerDialogListeners() {
        AbhaOtpTypeDialogFragment.setResultListener(supportFragmentManager, this) {
            viewModel.onAuthTypeSelected(it)
        }
        AccountSelectDialogFragment.setResultListener(supportFragmentManager, this) {
            viewModel.onAccountSelected(it)
        }
    }

    private fun showConsentDialog() {
        ConsentDialog(viewModel.uiState.value.patientName).apply {
            setListeners(object : ConsentDialog.Clickable {
                override fun isChecked(isCheck: Boolean) {
                    binding.cbTermsAndConditions.isChecked = isCheck
                }
            })
        }.show(supportFragmentManager, ConsentDialog.ABHA_CONSENT)
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
                viewModel.events.collect { handleEvent(it) }
            }
        }
    }

    private fun render(state: AbhaVerifyUiState) {
        renderTabs(state.method)
        renderGroups(state)
        clearOtpOnStepChange(state.step)
        renderStepVisibility(state)
        renderPrimaryButton(state)
        renderInputsEnabled(state)
        renderFieldErrors(state)
        renderResend(state)
        renderOperation(state)
        lastStep = state.step
    }

    private fun renderTabs(method: AbhaVerifyMethod) {
        binding.btnTabAadhaar.setBackgroundResource(tabBg(method == AbhaVerifyMethod.Aadhaar))
        binding.btnTabMobile.setBackgroundResource(tabBg(method == AbhaVerifyMethod.Mobile))
        binding.btnTabAbha.setBackgroundResource(tabBg(method == AbhaVerifyMethod.Abha))
    }

    private fun tabBg(selected: Boolean): Int =
        if (selected) R.drawable.abdm_bg_tab_selected else R.drawable.abdm_bg_tab_unselected

    private fun renderGroups(state: AbhaVerifyUiState) {
        binding.groupAadhaar.isVisible = state.method == AbhaVerifyMethod.Aadhaar
        binding.groupMobile.isVisible = state.method == AbhaVerifyMethod.Mobile
        binding.groupAbha.isVisible = state.method == AbhaVerifyMethod.Abha
    }

    /** The shared OTP widget is cleared whenever we (re)enter the OTP step. */
    private fun clearOtpOnStepChange(step: AbhaVerifyStep) {
        if (step != lastStep && step == AbhaVerifyStep.EnterOtp) {
            binding.otpView.setText("")
        }
    }

    private fun renderStepVisibility(state: AbhaVerifyUiState) {
        val isOtpStep = state.step == AbhaVerifyStep.EnterOtp
        binding.otpView.isVisible = isOtpStep
        binding.tvRetriesLeft.isVisible = isOtpStep
    }

    private fun renderPrimaryButton(state: AbhaVerifyUiState) {
        val loading = state.operation is UiState.Loading
        binding.btnPrimary.text = when (state.step) {
            AbhaVerifyStep.EnterDetails -> when (state.method) {
                AbhaVerifyMethod.Mobile -> getString(R.string.abdm_action_search_profiles)
                else -> getString(R.string.abdm_action_send_otp)
            }

            AbhaVerifyStep.EnterOtp -> getString(R.string.abdm_action_verify)
        }
        binding.btnPrimary.isEnabled = !loading && when {
            state.method == AbhaVerifyMethod.Aadhaar && state.step == AbhaVerifyStep.EnterDetails ->
                state.isConsentChecked

            else -> true
        }
    }

    private fun renderInputsEnabled(state: AbhaVerifyUiState) {
        val enabled = state.operation !is UiState.Loading
        val detailsEditable = enabled && state.step == AbhaVerifyStep.EnterDetails
        binding.btnTabAadhaar.isEnabled = detailsEditable
        binding.btnTabMobile.isEnabled = detailsEditable
        binding.btnTabAbha.isEnabled = detailsEditable
        binding.etAadhaarNumber.isEnabled = detailsEditable
        binding.etMobileNumber.isEnabled = detailsEditable
        binding.etAbhaNumber.isEnabled = detailsEditable
        binding.etAbhaAddress.isEnabled = detailsEditable
        binding.cbTermsAndConditions.isEnabled = detailsEditable
        binding.otpView.isEnabled = enabled
    }

    private fun renderFieldErrors(state: AbhaVerifyUiState) {
        binding.tvAadhaarError.apply {
            isVisible = state.aadhaarError != null
            state.aadhaarError?.let { text = getString(it) }
        }
        binding.tvMobileError.apply {
            isVisible = state.mobileError != null
            state.mobileError?.let { text = getString(it) }
        }
        binding.tvAbhaError.apply {
            isVisible = state.abhaError != null
            state.abhaError?.let { text = getString(it) }
        }
        binding.tvOtpError.apply {
            isVisible = state.otpError != null
            state.otpError?.let { text = getString(it) }
        }
    }

    private fun renderResend(state: AbhaVerifyUiState) {
        val isOtpStep = state.step == AbhaVerifyStep.EnterOtp
        val hasAttemptsLeft = state.resendAttemptsRemaining > 0
        binding.layoutResend.isVisible = isOtpStep && hasAttemptsLeft

        binding.tvResend.apply {
            if (state.resendSecondsRemaining > 0) {
                isEnabled = false
                text = getString(R.string.abdm_label_resend_in, state.resendSecondsRemaining)
            } else {
                isEnabled = true
                text = getString(R.string.abdm_action_resend_otp)
            }
        }
        binding.tvRetriesLeft.text = if (hasAttemptsLeft) {
            getString(R.string.abdm_label_retries_left, state.resendAttemptsRemaining)
        } else {
            getString(R.string.max_resend_attempts_exceeded)
        }
    }

    private fun renderOperation(state: AbhaVerifyUiState) {
        val loading = state.operation is UiState.Loading
        binding.loadingOverlay.isVisible = loading
        if (loading) {
            binding.loadingMessage.text =
                getString(state.loadingMessageRes ?: R.string.abdm_loading_default)
        }
    }


    private fun handleEvent(event: AbhaVerifyEvent) {
        when (event) {
            is AbhaVerifyEvent.ShowAuthTypeSelection ->
                AbhaOtpTypeDialogFragment.show(supportFragmentManager, event.authMethods)

            is AbhaVerifyEvent.ShowAccountSelection ->
                AccountSelectDialogFragment.show(supportFragmentManager, event.accounts)

            is AbhaVerifyEvent.NavigateToCompare ->
                compareLauncher.launch(
                    AbhaCompareActivity.newIntent(
                        this,
                        event.localRecord,
                        event.abhaRecord,
                        event.xToken,
                        event.txnId,
                    ),
                )

            is AbhaVerifyEvent.CompleteWithResult -> finishWithResult(event.result)

            is AbhaVerifyEvent.PromptCreateAbha -> showCreateAbhaPrompt(event.messageRes)

            is AbhaVerifyEvent.ShowSnackbar -> {
                val text = event.message?.takeIf { it.isNotBlank() }
                    ?: event.messageRes?.let { getString(it) }
                if (text != null) binding.main.showAbdmSnackbar(text, event.isSuccess)
            }
        }
    }

    /** Legacy parity: no ABHA on record → offer to jump into the Create flow. */
    private fun showCreateAbhaPrompt(messageRes: Int) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Abdm_MaterialAlertDialog)
            .setIcon(R.drawable.abdm_ic_alert)
            .setMessage(messageRes)
            .setPositiveButton(R.string.ok) { _, _ ->
                createLauncher.launch(
                    Intent(this, AbhaCreateActivity::class.java)
                        .putExtra(AbhaCreateActivity.EXTRA_PATIENT_NAME, viewModel.uiState.value.patientName),
                )
            }
            .setNegativeButton(R.string.abdm_action_cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_PATIENT_NAME = "patientName"
    }
}
