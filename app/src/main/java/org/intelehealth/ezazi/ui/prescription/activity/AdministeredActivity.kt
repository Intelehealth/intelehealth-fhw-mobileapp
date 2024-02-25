package org.intelehealth.ezazi.ui.prescription.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IntegerRes
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.ActivityAdministeredBinding
import org.intelehealth.ezazi.ui.prescription.data.MedicineSingleton
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.ASSESSMENT
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.FULL
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.IV_FLUID
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.MEDICINE
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.OXYTOCIN
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType.PLAN
import org.intelehealth.ezazi.ui.prescription.listener.TitleChangeListener
import org.intelehealth.ezazi.ui.prescription.model.PrescriptionArg
import org.intelehealth.ezazi.ui.prescription.viewmodel.PrescriptionViewModel
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity


/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:26.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class AdministeredActivity : BaseActionBarActivity(), TitleChangeListener {
    private lateinit var binding: ActivityAdministeredBinding

    private val viewMode: PrescriptionViewModel by lazy {
        ViewModelProvider(
            this, ViewModelProvider.Factory.from(PrescriptionViewModel.initializer)
        )[PrescriptionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdministeredBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        setupActionBar()
        setupAdministeredView()
    }

    private fun setupAdministeredView() {
        hasArgs {
            viewMode.prescriptionArg = it
            getExtractItems(it)
        }
    }

    private fun hasArgs(block: (PrescriptionArg) -> Unit) {
        if (intent.hasExtra(PrescriptionFragment.EXT_PRESCRIPTION_ARG)) {
            val args = IntentCompat.getParcelableExtra(
                intent, PrescriptionFragment.EXT_PRESCRIPTION_ARG,
                PrescriptionArg::class.java
            )
            args?.let { block.invoke(it) }
        }
    }

    private fun getExtractItems(arg: PrescriptionArg) {
        when (arg.prescriptionType) {
            PLAN -> MedicineSingleton.planListener?.let {
                viewMode.updateItemList(it.getExistingList())
            }

            ASSESSMENT -> MedicineSingleton.assessmentListener?.let {
                viewMode.updateItemList(it.getExistingList())
            }

            MEDICINE -> MedicineSingleton.medicineListener?.let {
                viewMode.updateItemList(it.getExistingList())
            }

            OXYTOCIN -> MedicineSingleton.oxytocinListener?.let {
                viewMode.updateItemList(it.getExistingList())
            }

            IV_FLUID -> MedicineSingleton.ivFluidListener?.let {
                viewMode.updateItemList(it.getExistingList())
            }

            FULL -> {}
        }

    }


    override fun getScreenTitle(): Int = R.string.administer

    companion object {
        private const val EXT_ADMINISTERED_ITEM = "ext_administered_item"

        @JvmStatic
        fun startAdministeredActivity(
            context: Context,
            prescriptionArg: PrescriptionArg
        ) {
            Intent(context, AdministeredActivity::class.java).apply {
                putExtra(PrescriptionFragment.EXT_PRESCRIPTION_ARG, prescriptionArg)
            }.also { context.startActivity(it) }
        }
    }

    @SuppressLint("ResourceType")
    override fun changeScreenTitle(@IntegerRes titleResId: Int) {
        supportActionBar?.let {
            it.title = getString(titleResId)
        }
    }
}