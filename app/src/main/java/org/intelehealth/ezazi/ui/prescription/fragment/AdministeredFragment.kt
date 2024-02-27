package org.intelehealth.ezazi.ui.prescription.fragment

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.FragmentAdministeredBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.partogram.model.Medicine
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionAdapter
import org.intelehealth.ezazi.ui.prescription.data.MedicineSingleton
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.*
import org.intelehealth.ezazi.ui.prescription.listener.TitleChangeListener
import org.intelehealth.ezazi.ui.prescription.viewmodel.PrescriptionViewModel
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.klivekit.utils.extensions.setupLinearView
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 22-02-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class AdministeredFragment : Fragment(R.layout.fragment_administered), MenuProvider,
    BaseViewHolder.ViewHolderClickListener {
    private lateinit var adapter: PrescriptionAdapter
    private lateinit var binding: FragmentAdministeredBinding
    private lateinit var titleChangeListener: TitleChangeListener

    private val viewMode: PrescriptionViewModel by lazy {
        ViewModelProvider(
            requireActivity(), ViewModelProvider.Factory.from(PrescriptionViewModel.initializer)
        )[PrescriptionViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdministeredBinding.bind(view)
        setupItemListView()
        observerItem()
        setActionClickListener()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        if (::titleChangeListener.isInitialized) titleChangeListener.changeScreenTitle(
            getScreenTitle()
        )
    }

    private fun setActionClickListener() {
        binding.btnAddMoreMedicine.setOnClickListener {
            viewMode.prescriptionArg?.let {
                if (it.prescriptionType == MEDICINE) moveToNewMedicine()
                else if (it.prescriptionType == PLAN || it.prescriptionType == ASSESSMENT) moveToNewPlan()
            }

        }
        binding.btnSaveMedicines.setOnClickListener {
            saveAndUpdateFinalListOfMedicines()
        }

        hideAddButton()
    }

    private fun moveToNewMedicine() {
        AdministeredFragmentDirections.actionNewMedicine(null).apply {
            findNavController().navigate(this)
        }
    }

    private fun moveToNewPlan() {
        AdministeredFragmentDirections.actionNewPlan(null).apply {
            findNavController().navigate(this)
        }
    }

    private fun observerItem() {
        viewMode.liveItems.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvEmptyListMessage.isVisible = it.isEmpty()
                adapter.updateItems(it)
                changeSaveButtonEnableState(it.size > 0)
            } ?: changeSaveButtonEnableState(false)
        }
    }

    private fun changeSaveButtonEnableState(enable: Boolean) {
        binding.btnSaveMedicines.isEnabled = enable
    }

    private fun setupItemListView() {
        adapter = PrescriptionAdapter(requireContext(), LinkedList())
        adapter.setClickListener(this)
        binding.rvMedicines.setupLinearView(adapter)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.prescription_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_view_prescription) {
            viewMode.prescriptionArg?.let {
                findNavController().navigate(R.id.fragmentPrescription, Bundle().apply {
                    putParcelable(PrescriptionFragment.EXT_PRESCRIPTION_ARG, it)
                }, null)
//                findNavController().currentDestination?.let { navDestination ->
//                    if (navDestination.id == R.id.fragmentAdministered) {
//                        AdministeredFragmentDirections.actionViewPrescription(it).apply {
//                            findNavController().navigate(this)
//
//                        }
//                    }
//                }
            }
        }
        return true
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        if (view == null) return
        if (view.id == R.id.clMedicineRowItemRoot) {
            adapter.setExpandedItemPosition(position)
        } else if (view.id == R.id.btnExpandCollapseIndicator) {
            adapter.setExpandedItemPosition(position)
        } else if (view.id == R.id.btnPrescriptionPlanViewMore) {
            val obs: ObsDTO = view.tag as ObsDTO
            obs.updateVisibleContentLine()
            adapter.notifyItemChanged(position)
        }
    }

    private fun saveAndUpdateFinalListOfMedicines() {
        viewMode.prescriptionArg?.let {
            when (it.prescriptionType) {
                PLAN -> MedicineSingleton.planListener?.onMedicineListChanged(adapter.getList())
                ASSESSMENT -> MedicineSingleton.assessmentListener?.onMedicineListChanged(adapter.getList())
                MEDICINE -> MedicineSingleton.medicineListener?.onMedicineListChanged(adapter.getList())
                OXYTOCIN -> MedicineSingleton.oxytocinListener?.onMedicineListChanged(adapter.getList())
                IV_FLUID -> MedicineSingleton.ivFluidListener?.onMedicineListChanged(adapter.getList())
                FULL -> {}
            }
            requireActivity().finish()
        }
    }

    private fun getScreenTitle(): Int {
        return viewMode.prescriptionArg?.let {
            return when (it.prescriptionType) {
                PLAN -> {
                    binding.administerType = getString(R.string.lbl_plan)
                    R.string.planned_by_you
                }

                ASSESSMENT -> {
                    binding.administerType = getString(R.string.lbl_assessment)
                    R.string.assess_by_you
                }

                MEDICINE -> {
                    binding.administerType = getString(R.string.lbl_medicine)
                    R.string.lbl_medicine_administration
                }

                OXYTOCIN -> {
                    binding.administerType = getString(R.string.lbl_oxytocin)
                    R.string.lbl_oxytocin_administration
                }

                IV_FLUID -> {
                    binding.administerType = getString(R.string.lbl_iv_fluid)
                    R.string.lbl_iv_fluid_administration
                }

                FULL -> R.string.administer
            }
        } ?: R.string.administer
    }

    private fun hideAddButton() {
        viewMode.prescriptionArg?.let {
            binding.btnAddMoreMedicine.isVisible =
                it.prescriptionType != OXYTOCIN && it.prescriptionType != IV_FLUID
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TitleChangeListener) titleChangeListener = context
    }
}