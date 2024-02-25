package org.intelehealth.ezazi.ui.prescription.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.activities.setupActivity.LocationArrayAdapter
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.app.IntelehealthApplication
import org.intelehealth.ezazi.database.dao.ProviderDAO
import org.intelehealth.ezazi.databinding.FragmentNewMedicineBinding
import org.intelehealth.ezazi.partogram.model.GetMedicineData
import org.intelehealth.ezazi.partogram.model.Medicine
import org.intelehealth.ezazi.ui.prescription.listener.TitleChangeListener
import org.intelehealth.ezazi.ui.prescription.viewmodel.PrescriptionViewModel
import org.intelehealth.ezazi.ui.shared.TextChangeListener
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.ezazi.utilities.exception.DAOException
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.util.Arrays

/**
 * Created by Vaghela Mithun R. on 22-02-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class NewMedicineFragment : Fragment(R.layout.fragment_new_medicine) {
    private lateinit var binding: FragmentNewMedicineBinding
    private var medicineDetailsList: List<Medicine>? = null
    private lateinit var titleChangeListener: TitleChangeListener
    private val viewMode: PrescriptionViewModel by lazy {
        ViewModelProvider(
            requireActivity(), ViewModelProvider.Factory.from(PrescriptionViewModel.initializer)
        )[PrescriptionViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewMedicineBinding.bind(view)
        buildAddNewMedicineDialog()
        validateMedicineFormInput()
        setupInputFilter()
        setActionClickListener()
        setData()
        if (::titleChangeListener.isInitialized) titleChangeListener.changeScreenTitle(
            getScreenTitle()
        )
    }

    private fun setData() {
        arguments?.let {
            NewMedicineFragmentArgs.fromBundle(it).medicine.let {
                binding.medicine = it
                binding.updatePosition = -1
            }
        }
    }

    private fun setActionClickListener() {
        binding.btnAddMedicineAdd.setOnClickListener { addMedicine() }
        binding.btnAddMedicineCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun addMedicine() {
        val medicine = validateMedicineFormInput()
        medicine.createdAt = DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT)
        if (medicine.isValidMedicine) {
            var updated: Int = -1
            if (binding.updatePosition != null) {
                updated = binding.updatePosition as Int
//                if (binding.medicine != null) {
//
//                }
            }
            if (updated > -1) viewMode.updateItem(updated, medicine) else {
                viewMode.addItem(medicine)
            }
            clearAddNewMedicineForm()
            findNavController().popBackStack(R.id.fragmentAdministered, false)
        }
    }

    private fun setupInputFilter() {
        setInputFilter(binding.autoCompleteMedicineForm)
        setInputFilter(binding.autoCompleteMedicineStrength)
        setInputFilter(binding.etMedicineDosage)
        setInputFilter(binding.autoCompleteMedicineDosageUnit)
        setInputFilter(binding.autoCompleteMedicineFrequency)
        setInputFilter(binding.autoCompleteOtherMedicine)
        setInputFilter(binding.autoCompleteMedicineRoute)
        setInputFilter(binding.autoCompleteMedicineDurationUnit)
        setInputFilter(binding.etMedicineDuration)
        setInputFilter(binding.etRemark)

        //binding.autoCompleteMedicineForm.setFilters(new InputFilter[]{new ValueInputFilter(formArray)});
    }

    private fun setInputFilter(editText: TextInputEditText) {
        editText.filters = arrayOf<InputFilter>(FirstLetterUpperCaseInputFilter())
    }

    private fun setInputFilter(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.filters = arrayOf<InputFilter>(FirstLetterUpperCaseInputFilter())
    }

    private fun validateMedicineFormInput(): Medicine {
        val medicine = Medicine()
        medicine.name = binding.autoCompleteOtherMedicine.text.toString()
        medicine.strength = binding.autoCompleteMedicineStrength.text.toString()
        medicine.dosage = binding.etMedicineDosage.text.toString()
        medicine.dosageUnit = binding.autoCompleteMedicineDosageUnit.text.toString()
        medicine.route = binding.autoCompleteMedicineRoute.text.toString()
        medicine.form = binding.autoCompleteMedicineForm.text.toString()
        medicine.frequency = binding.autoCompleteMedicineFrequency.text.toString()
        medicine.durationUnit = binding.autoCompleteMedicineDurationUnit.text.toString()
        medicine.duration = binding.etMedicineDuration.text.toString()
        medicine.remark = binding.etRemark.text.toString()
        medicine.type = binding.autoCompleteMedicineForm.text.toString()
        medicine.createdAt = DateTimeUtils.getCurrentDateWithDBFormat()
        //medicine.setCreatorName("You");
        try {
            medicine.creatorName =
                ProviderDAO().getCreatorGivenName(SessionManager(IntelehealthApplication.getAppContext()).providerID)
        } catch (e: DAOException) {
            throw RuntimeException(e)
        }
        binding.btnAddMedicineAdd.isEnabled = medicine.isValidMedicine
        return medicine
    }

    private fun clearAddNewMedicineForm() {
        binding.updatePosition = -1
        binding.autoCompleteOtherMedicine.setText("")
        binding.autoCompleteMedicineStrength.setText("")
        binding.etMedicineDosage.setText("")
        binding.autoCompleteMedicineForm.setText("")
        binding.autoCompleteMedicineDosageUnit.setText("")
        binding.autoCompleteMedicineRoute.setText("")
        binding.autoCompleteMedicineFrequency.setText("")
        binding.autoCompleteMedicineDurationUnit.setText("")
        binding.etMedicineDuration.setText("")
        binding.etRemark.setText("")
        validateMedicineFormInput()
    }

    private fun buildAddNewMedicineDialog() {
        setupMedicines()
        setupRoutes()
        setupDoseUnits()
        setupFrequency()
        setupDurationUnits()
        setFormArray()
        setStrength()
        binding.etMedicineDosage.addTextChangedListener(listener)
        binding.etMedicineDuration.addTextChangedListener(listener)
        binding.etRemark.addTextChangedListener(listener)
    }

    private val listener: TextChangeListener = object : TextChangeListener() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            validateMedicineFormInput()
        }
    }

    private fun setupMedicines() {
        medicineDetailsList = GetMedicineData().getMedicineDetails(requireActivity())
        medicineDetailsList?.let {
            val medicineItem = arrayOfNulls<String>(it.size)
            for (i in it.indices) {
                it[i].apply {
                    medicineFullName = "$form $name $strength"
                    medicineItem[i] = medicineFullName
                }
            }

            //String[] medicineItem = requireContext().getResources().getStringArray(R.array.medicines);
            val medicineDropDown: AutoCompleteTextView = binding.autoCompleteOtherMedicine
            val filteredItems = filterMedicines(medicineItem)
            if (filteredItems.size > 0) {
                val array = filteredItems.toTypedArray()
                setupAutoCompleteAdapter(array, medicineDropDown)
            } else setupAutoCompleteAdapter(medicineItem, medicineDropDown)
        }
    }

    private fun filterMedicines(medicineItem: Array<String?>): ArrayList<String?> {
        val filteredItems = ArrayList<String?>()
        if (viewMode.medicationItems.size > 0) {
            for (item in medicineItem) {
                for (medItem in viewMode.medicationItems) {
                    if (medItem is Medicine && medItem.name != item) {
                        filteredItems.add(item)
                    }
                }
            }
        }
        return filteredItems
    }

    private fun setupDoseUnits() {
        val routes = requireContext().resources.getStringArray(R.array.dose_units)
        val doseDropDown: AutoCompleteTextView =
            binding.autoCompleteMedicineDosageUnit
        setupAutoCompleteAdapter(routes, doseDropDown)
    }

    private fun setupRoutes() {
        val routes = requireContext().resources.getStringArray(R.array.medicine_routes)
        val routeDropDown: AutoCompleteTextView = binding.autoCompleteMedicineRoute
        setupAutoCompleteAdapter(routes, routeDropDown)
    }

    private fun setupFrequency() {
        val frequencies = requireContext().resources.getStringArray(R.array.medicine_frequencies)
        val frequenciesDropDown: AutoCompleteTextView =
            binding.autoCompleteMedicineFrequency
        setupAutoCompleteAdapter(frequencies, frequenciesDropDown)
    }

    private fun setupAutoCompleteAdapter(
        items: Array<String?>,
        autoCompleteTextView: AutoCompleteTextView
    ) {
        val adapter = LocationArrayAdapter(requireContext(), items.toList())
        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke)
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.setOnClickListener { v: View? ->
            autoCompleteTextView.requestFocus() // Request focus to ensure the dropdown appears
            autoCompleteTextView.showDropDown()
        }
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent: AdapterView<*>, view: View?, position: Int, id: Long ->
                if (autoCompleteTextView.id == binding.autoCompleteOtherMedicine.getId()) {
                    //autofill other fields
                    val selectedItem = parent.getItemAtPosition(position) as String
                    autoCompleteTextView.setSelection(selectedItem.length)
                    medicineDetailsList?.let {
                        for (medicine in it) {
                            if (selectedItem.equals(medicine.medicineFullName, ignoreCase = true)) {
                                binding.setMedicine(medicine)
                            }
                        }
                    }
                }
                validateMedicineFormInput()
            }
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun setupDurationUnits() {
        requireContext().resources.getStringArray(R.array.medicine_duration_units).apply {
            setupAutoCompleteAdapter(this, binding.autoCompleteMedicineDurationUnit)
        }
    }

    private fun setFormArray() {
        requireContext().resources.getStringArray(R.array.medicine_form).apply {
            setupAutoCompleteAdapter(this, binding.autoCompleteMedicineForm)
        }
    }

    private fun setStrength() {
        requireContext().resources.getStringArray(R.array.medicine_strength).apply {
            setupAutoCompleteAdapter(this, binding.autoCompleteMedicineStrength)
        }
    }

    private fun getScreenTitle(): Int {
        return viewMode.prescriptionArg?.let {
            return when (it.prescriptionType) {
                PrescriptionFragment.PrescriptionType.MEDICINE -> R.string.lbl_add_new_medicine
                else -> R.string.lbl_add
            }
        } ?: R.string.lbl_add
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TitleChangeListener) titleChangeListener = context
    }
}