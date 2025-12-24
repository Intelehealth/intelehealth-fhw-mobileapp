package org.intelehealth.app.widget.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.app.R
import org.intelehealth.app.adapter.CheckListDialogAdapter
import org.intelehealth.app.databinding.DialogCheckboxBinding
import org.intelehealth.app.utilities.DialogUtils

class ChecklistDialogFragment : DialogFragment() {

    private lateinit var binding: DialogCheckboxBinding
    private lateinit var adapter: CheckListDialogAdapter
    private lateinit var listener: DialogUtils.TextSelectedListener
    private lateinit var preferredAbhaAddress: String
    private lateinit var abhaAddressList: List<String>

    companion object {
        const val TAG = "ChecklistDialogFragment"
        const val ARG_PREFERRED_ABHA_ADDRESS: String = "arg_preferred_abha_address"
        const val ARG_ABHA_LIST: String = "arg_abha_list"

        fun newInstance(
            preferredAbhaAddress: String,
            abhaAddressList: ArrayList<String>,
            listener: DialogUtils.TextSelectedListener,
        ): ChecklistDialogFragment {
            return ChecklistDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PREFERRED_ABHA_ADDRESS, preferredAbhaAddress)
                    putStringArrayList(ARG_ABHA_LIST, abhaAddressList);
                }
                this.listener = listener
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        preferredAbhaAddress = args.getString(ARG_PREFERRED_ABHA_ADDRESS) ?: ""
        abhaAddressList = args.getStringArrayList(ARG_ABHA_LIST) ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogCheckboxBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.positiveBtn.setOnClickListener {
            val selectedAbhaAddress: String? = getSelectedAbhaAddress()

            if (selectedAbhaAddress == null) {
                Toast.makeText(
                    context,
                    getString(R.string.please_select_an_abha_address_before_proceeding),
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            listener.onDialogActionDone(
                DialogUtils.TextSelectedListener.POSITIVE_CLICK,
                selectedAbhaAddress
            )
        }

        binding.negativeBtn.setOnClickListener {
            listener.onDialogActionDone(DialogUtils.TextSelectedListener.NEGATIVE_CLICK)
        }
    }

    fun displayError(errorMessage: String) {
        binding.tvError.text = errorMessage
    }

    fun shouldShowErrorMessage(shouldShow: Boolean) {
        binding.tvError.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = CheckListDialogAdapter(abhaAddressList, preferredAbhaAddress)
        binding.rvAbhaAddress.apply {
            adapter = this@ChecklistDialogFragment.adapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun getSelectedAbhaAddress(): String? {
        return adapter.getSelectedAbhaAddress()
    }
}