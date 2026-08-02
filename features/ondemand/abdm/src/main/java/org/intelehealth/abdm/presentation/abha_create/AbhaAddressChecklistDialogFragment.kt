package org.intelehealth.abdm.presentation.abha_create

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAbhaAddressChecklistBinding

/**
 * Shows the patient's existing ABHA addresses for single selection. Reports the user's choice
 * via the Fragment Result API: either a selected address, or a request to create a new one.
 * The "max 6 addresses" guard for the create-new path is handled inside the dialog.
 */
internal class AbhaAddressChecklistDialogFragment : DialogFragment() {

    /** The module's Material components need a MaterialComponents theme; the host may not have one. */
    override fun getTheme(): Int = R.style.Theme_Abdm_Dialog

    private var binding: DialogAbhaAddressChecklistBinding? = null
    private var adapter: AbhaAddressChecklistAdapter? = null

    private lateinit var preferredAbhaAddress: String
    private lateinit var abhaAddressList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        preferredAbhaAddress = args.getString(ARG_PREFERRED).orEmpty()
        abhaAddressList = args.getStringArrayList(ARG_ADDRESSES) ?: arrayListOf()
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DialogAbhaAddressChecklistBinding.inflate(layoutInflater).also { this.binding = it }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return

        adapter = AbhaAddressChecklistAdapter(abhaAddressList, preferredAbhaAddress).also {
            binding.rvAbhaAddress.adapter = it
            binding.rvAbhaAddress.layoutManager = LinearLayoutManager(requireContext())
            binding.rvAbhaAddress.setHasFixedSize(true)
        }

        binding.btnProceed.setOnClickListener {
            val selected = adapter?.getSelectedAbhaAddress()
            if (selected == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_select_an_abha_address_before_proceeding),
                    Toast.LENGTH_LONG,
                ).show()
                return@setOnClickListener
            }
            deliver(RESULT_SELECTED, selected)
        }

        binding.btnCreateNew.setOnClickListener {
            if (abhaAddressList.size >= MAX_ADDRESSES) {
                binding.tvError.text = getString(R.string.you_have_more_than_six_abha_addresses_error)
                binding.tvError.isVisible = true
                return@setOnClickListener
            }
            deliver(RESULT_CREATE_NEW, null)
        }
    }

    private fun deliver(type: String, address: String?) {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(RESULT_TYPE to type, RESULT_ADDRESS to address),
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        binding = null
    }

    companion object {
        const val TAG = "AbhaAddressChecklistDialog"
        private const val REQUEST_KEY = "abdm_checklist_request"
        private const val RESULT_TYPE = "type"
        private const val RESULT_ADDRESS = "address"
        private const val RESULT_SELECTED = "selected"
        private const val RESULT_CREATE_NEW = "create_new"
        private const val ARG_PREFERRED = "arg_preferred"
        private const val ARG_ADDRESSES = "arg_addresses"
        private const val MAX_ADDRESSES = 6

        fun show(
            fragmentManager: FragmentManager,
            preferredAbhaAddress: String,
            abhaAddresses: List<String>,
        ) {
            AbhaAddressChecklistDialogFragment().apply {
                arguments = bundleOf(
                    ARG_PREFERRED to preferredAbhaAddress,
                    ARG_ADDRESSES to ArrayList(abhaAddresses),
                )
            }.show(fragmentManager, TAG)
        }

        /** Delivers either the selected address, or a create-new request, to the host. */
        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onAddressSelected: (String) -> Unit,
            onCreateNewRequested: () -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                when (bundle.getString(RESULT_TYPE)) {
                    RESULT_SELECTED -> bundle.getString(RESULT_ADDRESS)?.let(onAddressSelected)
                    RESULT_CREATE_NEW -> onCreateNewRequested()
                }
            }
        }
    }
}
