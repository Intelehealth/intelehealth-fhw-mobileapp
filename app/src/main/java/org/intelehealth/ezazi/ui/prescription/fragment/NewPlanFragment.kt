package org.intelehealth.ezazi.ui.prescription.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.database.dao.ProviderDAO
import org.intelehealth.ezazi.databinding.FragmentNewPlanBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.ui.prescription.listener.TitleChangeListener
import org.intelehealth.ezazi.ui.prescription.viewmodel.PrescriptionViewModel
import org.intelehealth.ezazi.ui.shared.TextChangeListener
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.ezazi.utilities.StringUtils
import org.intelehealth.klivekit.utils.DateTimeUtils
import kotlin.math.roundToInt

/**
 * Created by Vaghela Mithun R. on 22-02-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class NewPlanFragment : Fragment(R.layout.fragment_new_plan) {
    private lateinit var binding: FragmentNewPlanBinding
    private lateinit var titleChangeListener: TitleChangeListener
    private val viewMode: PrescriptionViewModel by lazy {
        ViewModelProvider(
            requireActivity(), ViewModelProvider.Factory.from(PrescriptionViewModel.initializer)
        )[PrescriptionViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewPlanBinding.bind(view)
        setActionClickListener()
        setInputFilter(binding.etNewPlan)
        binding.etNewPlan.addTextChangedListener(listener)
        setData()
        if (::titleChangeListener.isInitialized) titleChangeListener.changeScreenTitle(
            getScreenTitle()
        )
    }

    private fun setData() {
        arguments?.let {
            NewPlanFragmentArgs.fromBundle(it).plan.let {
                binding.plan = it
                binding.updatePosition = -1
            }
        }
    }

    private fun setActionClickListener() {
        binding.btnAddPlanAdd.setOnClickListener { addPlan() }
        binding.btnAddPlanCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setInputFilter(editText: TextInputEditText) {
        editText.filters = arrayOf<InputFilter>(FirstLetterUpperCaseInputFilter())
    }

    private val listener: TextChangeListener = object : TextChangeListener() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            validatePlanFormInput()
        }
    }

    private fun validatePlanFormInput(): ObsDTO {
        val plan = ObsDTO()
        plan.value = binding.etNewPlan.text.toString()
        binding.btnAddPlanAdd.isEnabled = plan.isValidPlan
        return plan
    }

    private fun clearAddNewMedicineForm() {
        binding.updatePosition = -1
        binding.etNewPlan.setText("")
        validatePlanFormInput()
    }

    private fun addPlan() {
        val plan = validatePlanFormInput()
        val providerId = SessionManager(requireContext()).providerID
        plan.name = ProviderDAO().getCreatorGivenName(providerId)
        plan.setCreatedDate(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT))
        if (plan.isValidPlan) {
            var updated = -1
            if (binding.updatePosition != null) {
                updated = binding.updatePosition ?: -1
                plan.uuid = binding.plan?.uuid
            }

            plan.calculateLine()
            if (updated > -1) viewMode.updateItem(updated, plan)
            else viewMode.addItem(plan)

            clearAddNewMedicineForm()
            findNavController().popBackStack(R.id.fragmentAdministered, false)
        }
    }

    private fun getScreenTitle(): Int {
        return viewMode.prescriptionArg?.let {
            return when (it.prescriptionType) {
                PrescriptionFragment.PrescriptionType.PLAN -> {
                    binding.etNewPlan.hint = getString(R.string.hint_add_new_plan)
                    R.string.lbl_add_new_plan
                }

                PrescriptionFragment.PrescriptionType.ASSESSMENT -> {
                    binding.etNewPlan.hint = getString(R.string.hint_add_new_assessment)
                    R.string.lbl_add_new_assessment
                }

                else -> R.string.lbl_add
            }
        } ?: R.string.lbl_add
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TitleChangeListener) titleChangeListener = context
    }
}