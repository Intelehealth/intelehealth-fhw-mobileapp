package org.intelehealth.abdm.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.adapter.MultipleAccountsAdapter
import org.intelehealth.abdm.databinding.FragmentAccountSelectDialogBinding
import org.intelehealth.abdm.model.Account

class AccountSelectDialogFragment : BottomSheetDialogFragment() {

    private var selectedAccount: Account? = null
    private lateinit var accountsAdapter: MultipleAccountsAdapter
    private lateinit var accountList: ArrayList<Account>
    private lateinit var binding: FragmentAccountSelectDialogBinding
    private var onAccountSelection: OnAccountSelection? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAccountSelectDialogBinding.inflate(layoutInflater)
        setListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAccountList()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setListeners() {
        binding.submitABHAAccountBtn.setOnClickListener {
            selectedAccount?.let { it1 -> onAccountSelection?.continueOtp(it1) }
        }
    }

    private fun setAccountList() {
        accountsAdapter = MultipleAccountsAdapter(context, accountList) { account, isChecked ->
            selectedAccount = if (isChecked) {
                account
            } else null
        }
        binding.rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccounts.adapter = accountsAdapter
    }

    fun openAccountSelectionDialog(
        accountList: List<Account>,
        onAccountSelection: OnAccountSelection,
    ) {
        this.accountList = ArrayList()
        this.accountList.addAll(accountList)
        this.onAccountSelection = onAccountSelection
    }

    interface OnAccountSelection {
        fun continueOtp(account: Account)
    }
}