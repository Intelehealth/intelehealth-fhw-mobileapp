package org.intelehealth.abdm.presentation.abha_create

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogMobileOtpVerificationBinding
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.presentation.common.showAbdmSnackbar

/**
 * Legacy-parity dialog for verifying a mobile number during ABHA creation (ports
 * MobileNumberOtpVerificationDialog). Purely a view over the shared [AbhaCreateViewModel]: the OTP
 * is already sent when this opens, and verify/resend delegate straight to the view model.
 */
@AndroidEntryPoint
internal class MobileOtpVerificationDialogFragment : DialogFragment() {

    /** The module's Material components need a MaterialComponents theme; the host may not have one. */
    override fun getTheme(): Int = R.style.Theme_Abdm_Dialog

    private val viewModel: AbhaCreateViewModel by activityViewModels()
    private var binding: DialogMobileOtpVerificationBinding? = null
    private var shownErrorRes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DialogMobileOtpVerificationBinding.inflate(layoutInflater).also { this.binding = it }
        binding.otpView.doAfterTextChanged {
            viewModel.onInputChanged(InputField.MobileOtp, it?.toString().orEmpty())
        }
        binding.btnVerify.setOnClickListener { viewModel.onSubmitMobileOtpClicked() }
        binding.tvResend.setOnClickListener { viewModel.onResendOtpClicked() }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: AbhaCreateUiState) {
        val binding = binding ?: return
        binding.tvSubtitle.text = getString(R.string.abdm_mobile_otp_sent, state.mobileNumber)

        binding.tvOtpError.apply {
            isVisible = state.otpError != null
            state.otpError?.let { text = getString(it) }
        }

        val hasAttemptsLeft = state.resendAttemptsRemaining > 0
        binding.layoutResend.isVisible = hasAttemptsLeft
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

        val loading = state.operation is UiState.Loading
        binding.progress.isVisible = loading
        binding.btnVerify.isEnabled = !loading
        binding.otpView.isEnabled = !loading

        val operation = state.operation
        if (operation is UiState.Error) {
            if (operation.messageRes != shownErrorRes) {
                shownErrorRes = operation.messageRes
                binding.root.showAbdmSnackbar(getString(operation.messageRes), isSuccess = false)
            }
        } else {
            shownErrorRes = null
        }
    }

    /** Shows a coloured snackbar anchored to this dialog (used for OTP send/resend from the host). */
    fun showSnackbar(message: String, isSuccess: Boolean) {
        binding?.root?.showAbdmSnackbar(message, isSuccess)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val TAG = "MobileOtpVerificationDialog"
    }
}
