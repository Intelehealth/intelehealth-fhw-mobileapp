package org.intelehealth.ezazi.ui.prescription.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.app.IntelehealthApplication
import org.intelehealth.ezazi.database.dao.ProviderDAO
import org.intelehealth.ezazi.databinding.FragmentCommenListviewBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.partogram.PartogramConstants
import org.intelehealth.ezazi.partogram.model.Medication
import org.intelehealth.ezazi.partogram.model.Medicine
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionAdapter
import org.intelehealth.ezazi.ui.prescription.listener.TitleChangeListener
import org.intelehealth.ezazi.ui.prescription.model.PrescriptionArg
import org.intelehealth.ezazi.ui.prescription.viewmodel.PrescriptionViewModel
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.klivekit.utils.DateTimeUtils
import org.intelehealth.klivekit.utils.extensions.setupLinearView
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 22-02-2024 - 13:30.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionFragment : Fragment(R.layout.fragment_commen_listview),
    BaseViewHolder.ViewHolderClickListener {
    private lateinit var binding: FragmentCommenListviewBinding
    private lateinit var adapter: PrescriptionAdapter
    private lateinit var args: PrescriptionArg
    private lateinit var titleChangeListener: TitleChangeListener

    //
    enum class PrescriptionType {
        FULL, PLAN, ASSESSMENT, MEDICINE, OXYTOCIN, IV_FLUID
    }

    //
    private val viewMode: PrescriptionViewModel by lazy {
        ViewModelProvider(
            requireActivity(), ViewModelProvider.Factory.from(PrescriptionViewModel.initializer)
        )[PrescriptionViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommenListviewBinding.bind(view)
        setupPrescriptionView()
    }

    private fun setupPrescriptionView() {
        adapter = PrescriptionAdapter(requireContext(), LinkedList())
        binding.rvPrescription.setupLinearView(adapter)
        arguments?.let { bundle ->
            BundleCompat.getParcelable(
                bundle, EXT_PRESCRIPTION_ARG, PrescriptionArg::class.java
            )?.let {
                args = it
            }
            loadPrescriptions()
            if (::titleChangeListener.isInitialized) titleChangeListener.changeScreenTitle(
                getScreenTitle()
            )
        }

        observeState()
    }

    private fun observeState() {
        viewMode.loading.observe(viewLifecycleOwner) {
            binding.prescriptionProgressBar.apply {
                visibility = if (it) View.VISIBLE else View.GONE
            }
        }
        viewMode.errorDataResult.observe(viewLifecycleOwner) {}
        viewMode.failDataResult.observe(viewLifecycleOwner) {
            binding.tvCallLogEmptyMessage.visibility = View.VISIBLE
        }
    }

    private fun loadPrescriptions() {
        viewMode.getPrescriptions(args.visitId, args.prescriptionType)
            .observe(viewLifecycleOwner) {
                viewMode.handleResponse(it) { items ->
                    Timber.d { "Prescriptions => ${Gson().toJson(items)}" }
                    binding.tvCallLogEmptyMessage.visibility = View.GONE
                    if (items.isNotEmpty()) {
                        adapter.setAccessMode(args.accessMode)
                        adapter.setAllowAdminister(args.allowAdminister)
                        adapter.updateItems(items.toMutableList())
                        adapter.setClickListener(this)
                    } else viewMode.updateFailResult(getString(R.string.no_prescription))
                }
            }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.btnExpandCollapseIndicator) {
            adapter.setExpandedItemPosition(position)
        } else if (view.id == R.id.btnPrescriptionPlanViewMore) {
            val obs: ObsDTO = view.tag as ObsDTO
            obs.updateVisibleContentLine()
            adapter.notifyItemChanged(position)
        } else if (view.id == R.id.btnMedicationAdminister) {
            val medication = view.tag as Medication
            medication.infusionStatus = getInfusionStatus(medication.infusionStatus)
            medication.createdAt = DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT)
            medication.creatorName =
                ProviderDAO().getCreatorGivenName(SessionManager(requireContext()).providerID)
            viewMode.addItem(medication)
            findNavController().popBackStack()
        } else if (view.id == R.id.btnMedicineAdminister) {
            val medicine = view.tag as Medicine
            medicine.createdAt = DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT)
            val directions = PrescriptionFragmentDirections.actionNewMedicine(medicine)
            findNavController().navigate(directions)
        }
    }

    private fun getInfusionStatus(status: String): String {
        PartogramConstants.infusionStatus.forEach {
            if (it.contains(status)) return it
        }
        return status
    }

    private fun getScreenTitle(): Int {
        return viewMode.prescriptionArg?.let {
            return when (it.prescriptionType) {
                PrescriptionType.PLAN -> R.string.planned_from_doctor
                PrescriptionType.ASSESSMENT -> R.string.assess_from_doctor
                else -> R.string.prescription
            }
        } ?: R.string.prescription
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TitleChangeListener) titleChangeListener = context
    }

    companion object {
        const val EXT_VISIT_ID = "ext_visit_id"
        const val EXT_PRESCRIPTION_ARG = "prescription_arg"

        @JvmStatic
        fun getInstance(prescriptionArg: PrescriptionArg) = PrescriptionFragment().apply {
            Bundle().apply {
                putParcelable(EXT_PRESCRIPTION_ARG, prescriptionArg)
            }.also { arguments = it }
        }
    }
}