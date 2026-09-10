package org.intelehealth.abdm.presentation.abha_verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAbhaOtpTypeBinding

/**
 * Lets the user pick the OTP channel (Aadhaar-OTP vs Mobile-OTP) for the ABHA option. Which
 * channels are offered depends on the auth-modes the server allows. Reports the choice via the
 * Fragment Result API.
 */
internal class AbhaOtpTypeDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.Theme_Abdm_BottomSheet

    private var binding: DialogAbhaOtpTypeBinding? = null
    private lateinit var authMethods: List<String>
    private var selected: String = ABHA_OTP_AADHAAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authMethods = requireArguments().getStringArrayList(ARG_METHODS).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding =
            DialogAbhaOtpTypeBinding.inflate(inflater, container, false).also { this.binding = it }

        val hasAadhaar = authMethods.contains(ABHA_OTP_AADHAAR)
        val hasMobile = authMethods.contains(ABHA_OTP_MOBILE)
        binding.btnAadhaar.visibility = if (hasAadhaar) View.VISIBLE else View.GONE
        binding.btnMobile.visibility = if (hasMobile) View.VISIBLE else View.GONE
        selected = if (hasAadhaar) ABHA_OTP_AADHAAR else ABHA_OTP_MOBILE
        applySelection(binding)

        binding.btnAadhaar.setOnClickListener {
            selected = ABHA_OTP_AADHAAR; applySelection(binding)
        }
        binding.btnMobile.setOnClickListener { selected = ABHA_OTP_MOBILE; applySelection(binding) }
        binding.btnContinue.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_AUTH_TYPE to selected)
            )
            dismiss()
        }
        return binding.root
    }

    private fun applySelection(binding: DialogAbhaOtpTypeBinding) {
        val selectedBg = org.intelehealth.abdm.R.drawable.abdm_bg_tab_selected
        val unselectedBg = org.intelehealth.abdm.R.drawable.abdm_bg_tab_unselected
        binding.btnAadhaar.setBackgroundResource(if (selected == ABHA_OTP_AADHAAR) selectedBg else unselectedBg)
        binding.btnMobile.setBackgroundResource(if (selected == ABHA_OTP_MOBILE) selectedBg else unselectedBg)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val TAG = "AbhaOtpTypeDialog"
        const val ABHA_OTP_AADHAAR = "AADHAAR_OTP"
        const val ABHA_OTP_MOBILE = "MOBILE_OTP"
        private const val REQUEST_KEY = "abdm_otp_type_request"
        private const val RESULT_AUTH_TYPE = "auth_type"
        private const val ARG_METHODS = "arg_methods"

        fun show(fragmentManager: FragmentManager, authMethods: List<String>) {
            AbhaOtpTypeDialogFragment().apply {
                arguments = bundleOf(ARG_METHODS to ArrayList(authMethods))
                isCancelable = false
            }.show(fragmentManager, TAG)
        }

        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onAuthTypeSelected: (String) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                bundle.getString(RESULT_AUTH_TYPE)?.let(onAuthTypeSelected)
            }
        }
    }
}
