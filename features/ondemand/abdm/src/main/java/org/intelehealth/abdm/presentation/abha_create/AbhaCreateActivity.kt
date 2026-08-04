package org.intelehealth.abdm.presentation.abha_create

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
import org.intelehealth.abdm.databinding.ActivityAbhaCreateBinding
import org.intelehealth.abdm.presentation.AbdmCardDownloader
import org.intelehealth.abdm.presentation.abha_suggestions.AbhaSuggestionsActivity
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.presentation.common.showAbdmSnackbar
import org.intelehealth.abdm.presentation.consent.ConsentDialog
import org.intelehealth.abdm.result.AbdmResult
import org.intelehealth.abdm.util.NetworkConnection

@AndroidEntryPoint
class AbhaCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbhaCreateBinding
    private val viewModel: AbhaCreateViewModel by viewModels()

    private var lastStep: AbhaCreateStep? = null
    private var shownErrorRes: Int? = null

    private val suggestionsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val chosen = result.data?.getStringExtra(AbhaSuggestionsActivity.EXTRA_CHOSEN_ADDRESS)
                ?: return@registerForActivityResult
            viewModel.onSuggestionsAddressChosen(chosen)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAbhaCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()

        setupInputListeners()
        setupActions()
        registerChecklistListener()
        observeState()
        observeEvents()

        seedPatientNameFromIntent()

        if (!NetworkConnection.isOnline(this)) {
            showNoInternetDialog()
        }
    }

    /** Legacy parity: block the flow when offline; OK dismisses this screen. */
    private fun showNoInternetDialog() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Abdm_MaterialAlertDialog)
            .setIcon(R.drawable.abdm_ic_alert)
            .setTitle(R.string.error_network)
            .setMessage(R.string.you_need_an_active_internet_connection_to_use_this_feature)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .show()
    }

    /** The name is collected up front by the host (AbdmLauncher); here we only read it back. */
    private fun seedPatientNameFromIntent() {
        val intentName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        if (intentName.isNotBlank() && viewModel.uiState.value.patientName.isBlank()) {
            viewModel.onPatientNameEntered(intentName)
        }
    }

    private fun registerChecklistListener() {
        AbhaAddressChecklistDialogFragment.setResultListener(
            supportFragmentManager,
            this,
            onAddressSelected = { viewModel.onAbhaAddressSelected(it) },
            onCreateNewRequested = { viewModel.onCreateNewAddressRequested() },
        )
    }

    private fun applyInsets() {
        val window = window
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

    private fun setupInputListeners() {
        binding.includeAadhaarEntry.etAadhaarNumber.doAfterTextChanged {
            viewModel.onInputChanged(InputField.Aadhaar, it?.toString().orEmpty())
        }
        binding.includeAadhaarEntry.cbTermsAndConditions.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onConsentChecked(isChecked)
            if (isChecked) showConsentDialog()
        }
        binding.otpView.doAfterTextChanged {
            val otp = it?.toString().orEmpty()
            val field = if (viewModel.uiState.value.step == AbhaCreateStep.EnterMobileOtp) {
                InputField.MobileOtp
            } else {
                InputField.AadhaarOtp
            }
            viewModel.onInputChanged(field, otp)
        }
        binding.etMobileNumber.doAfterTextChanged {
            viewModel.onInputChanged(InputField.Mobile, it?.toString().orEmpty())
        }
    }

    /**
     * Checking the consent box opens the official consent dialog. On dismiss the dialog reports
     * whether the user accepted; if they declined (or dismissed), the box is unchecked again.
     */
    private fun showConsentDialog() {
        ConsentDialog(viewModel.uiState.value.patientName).apply {
            setListeners(object : ConsentDialog.Clickable {
                override fun isChecked(isCheck: Boolean) {
                    binding.includeAadhaarEntry.cbTermsAndConditions.isChecked = isCheck
                }
            })
        }.show(supportFragmentManager, ConsentDialog.ABHA_CONSENT)
    }

    /** Hands the outcome back to the host (PrivacyNoticeActivity) and closes this screen. */
    private fun finishWithResult(result: AbdmResult) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AbdmResult.EXTRA_ABDM_RESULT, result),
        )
        finish()
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvResend.setOnClickListener { viewModel.onResendOtpClicked() }
        binding.btnSendOtp.setOnClickListener {
            when (viewModel.uiState.value.step) {
                AbhaCreateStep.EnterAadhaar -> viewModel.onSendAadhaarOtpClicked()
                AbhaCreateStep.EnterAadhaarOtp -> viewModel.onVerifyAadhaarOtpClicked()
                AbhaCreateStep.EnterMobileOtp -> viewModel.onSubmitMobileOtpClicked()
                AbhaCreateStep.Completed -> Unit
            }
        }
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


    private fun render(state: AbhaCreateUiState) {
        applyStepVisibility(state.step)
        clearOtpOnStepChange(state.step)
        renderButton(state)
        renderInputsEnabled(state)
        renderFieldErrors(state)
        renderResend(state)
        renderOperation(state)
        renderMobileOtpDialog(state.step)
        lastStep = state.step
    }

    private fun renderMobileOtpDialog(step: AbhaCreateStep) {
        val existing = supportFragmentManager
            .findFragmentByTag(MobileOtpVerificationDialogFragment.TAG) as? MobileOtpVerificationDialogFragment
        if (step == AbhaCreateStep.EnterMobileOtp) {
            if (existing == null) {
                MobileOtpVerificationDialogFragment()
                    .show(supportFragmentManager, MobileOtpVerificationDialogFragment.TAG)
            }
        } else {
            existing?.dismissAllowingStateLoss()
        }
    }

    private fun applyStepVisibility(step: AbhaCreateStep) {
        val isOtpStep = step == AbhaCreateStep.EnterAadhaarOtp

        binding.includeAadhaarEntry.root.isVisible =
            step == AbhaCreateStep.EnterAadhaar || step == AbhaCreateStep.EnterAadhaarOtp
        binding.otpView.isVisible = isOtpStep
        binding.tvRetriesLeft.isVisible = isOtpStep
        binding.tilMobile.isVisible = isOtpStep
        binding.tvMobileInfo.isVisible = step == AbhaCreateStep.EnterAadhaarOtp
    }

    /** The single OTP widget is reused for the mobile-OTP step; clear stale Aadhaar digits on entry. */
    private fun clearOtpOnStepChange(step: AbhaCreateStep) {
        if (step != lastStep && step == AbhaCreateStep.EnterMobileOtp) {
            binding.otpView.setText("")
        }
    }

    private fun renderButton(state: AbhaCreateUiState) {
        val loading = state.operation is UiState.Loading

        binding.btnSendOtp.text = when (state.step) {
            AbhaCreateStep.EnterAadhaar -> getString(R.string.abdm_action_send_otp)
            AbhaCreateStep.EnterAadhaarOtp -> getString(R.string.abdm_action_verify)
            AbhaCreateStep.EnterMobileOtp -> getString(R.string.abdm_action_submit)
            AbhaCreateStep.Completed -> getString(R.string.abdm_action_send_otp)
        }
        binding.btnSendOtp.isEnabled = !loading && when (state.step) {
            AbhaCreateStep.EnterAadhaar -> state.isConsentChecked
            else -> true
        }
        binding.btnSendOtp.isVisible = state.step != AbhaCreateStep.EnterMobileOtp
    }

    private fun renderInputsEnabled(state: AbhaCreateUiState) {
        val enabled = state.operation !is UiState.Loading
        val aadhaarEditable = enabled && !state.aadhaarLocked
        binding.includeAadhaarEntry.etAadhaarNumber.isEnabled = aadhaarEditable
        binding.includeAadhaarEntry.cbTermsAndConditions.isEnabled = aadhaarEditable
        binding.otpView.isEnabled = enabled
        binding.etMobileNumber.isEnabled = enabled && state.step != AbhaCreateStep.EnterMobileOtp
    }

    private fun renderFieldErrors(state: AbhaCreateUiState) {
        binding.includeAadhaarEntry.tvAadhaarError.apply {
            isVisible = state.aadhaarError != null
            state.aadhaarError?.let { text = getString(it) }
        }
        binding.tvOtpError.apply {
            isVisible = state.otpError != null
            state.otpError?.let { text = getString(it) }
        }
        binding.tvMobileError.apply {
            isVisible = state.mobileError != null
            state.mobileError?.let { text = getString(it) }
        }
    }

    private fun renderResend(state: AbhaCreateUiState) {
        val isOtpStep = state.step == AbhaCreateStep.EnterAadhaarOtp
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

    private fun renderOperation(state: AbhaCreateUiState) {
        val operation = state.operation

        val onMobileOtpStep = state.step == AbhaCreateStep.EnterMobileOtp
        val loading = operation is UiState.Loading
        binding.loadingOverlay.isVisible = loading && !onMobileOtpStep
        if (loading) {
            binding.loadingMessage.text =
                getString(state.loadingMessageRes ?: R.string.abdm_loading_default)
        }

        if (operation is UiState.Error && !onMobileOtpStep) {
            if (operation.messageRes != shownErrorRes) {
                shownErrorRes = operation.messageRes
                binding.main.showAbdmSnackbar(getString(operation.messageRes), isSuccess = false)
            }
        } else if (operation !is UiState.Error) {
            shownErrorRes = null
        }
    }


    private fun handleEvent(event: AbhaCreateEvent) {
        when (event) {
            is AbhaCreateEvent.NavigateToSuggestions -> {
                suggestionsLauncher.launch(
                    AbhaSuggestionsActivity.newIntent(
                        this,
                        event.session.txnId,
                        event.session.profile.phrAddresses.firstOrNull().orEmpty(),
                    )
                )
            }

            is AbhaCreateEvent.ShowAddressChecklist -> {
                AbhaAddressChecklistDialogFragment.show(
                    supportFragmentManager,
                    event.preferredAbhaAddress,
                    event.abhaAddresses,
                )
            }

            is AbhaCreateEvent.CompleteWithResult -> finishWithResult(event.result)

            is AbhaCreateEvent.InvalidateCachedCard ->
                AbdmCardDownloader.invalidate(this, event.abhaNumber)

            is AbhaCreateEvent.ShowSnackbar -> {
                val text = event.message?.takeIf { it.isNotBlank() }
                    ?: event.messageRes?.let { getString(it) }
                if (text != null) {
                    val dialog = supportFragmentManager
                        .findFragmentByTag(MobileOtpVerificationDialogFragment.TAG) as? MobileOtpVerificationDialogFragment
                    if (dialog?.isVisible == true) {
                        dialog.showSnackbar(text, event.isSuccess)
                    } else {
                        binding.main.showAbdmSnackbar(text, event.isSuccess)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_PATIENT_NAME = "patientName"
    }
}
