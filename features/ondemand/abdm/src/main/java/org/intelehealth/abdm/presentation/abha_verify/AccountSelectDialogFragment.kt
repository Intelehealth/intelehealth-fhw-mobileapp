package org.intelehealth.abdm.presentation.abha_verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAccountSelectBinding

/**
 * Lets the user pick one ABHA account among several (mobile search, or a post-verify multi-account
 * response). Reports the chosen [AccountChoice] via the Fragment Result API.
 */
internal class AccountSelectDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.Theme_Abdm_BottomSheet

    private var binding: DialogAccountSelectBinding? = null
    private lateinit var accounts: List<AccountChoice>
    private var selected: AccountChoice? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accounts = requireArguments().getParcelableArrayList<AccountChoice>(ARG_ACCOUNTS).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DialogAccountSelectBinding.inflate(inflater, container, false).also { this.binding = it }

        binding.rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccounts.adapter = AccountSelectAdapter(accounts) { selected = it }

        binding.btnContinue.setOnClickListener {
            val choice = selected ?: return@setOnClickListener
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_ACCOUNT to choice))
            dismiss()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val TAG = "AccountSelectDialog"
        private const val REQUEST_KEY = "abdm_account_select_request"
        private const val RESULT_ACCOUNT = "account"
        private const val ARG_ACCOUNTS = "arg_accounts"

        fun show(fragmentManager: FragmentManager, accounts: List<AccountChoice>) {
            AccountSelectDialogFragment().apply {
                arguments = bundleOf(ARG_ACCOUNTS to ArrayList(accounts))
            }.show(fragmentManager, TAG)
        }

        @Suppress("DEPRECATION")
        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onAccountSelected: (AccountChoice) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                bundle.getParcelable<AccountChoice>(RESULT_ACCOUNT)?.let(onAccountSelected)
            }
        }
    }
}
