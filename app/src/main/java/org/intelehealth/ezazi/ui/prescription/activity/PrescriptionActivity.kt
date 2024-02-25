package org.intelehealth.ezazi.ui.prescription.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.ActivityPrescriptionEzaziBinding
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment
import org.intelehealth.ezazi.ui.prescription.model.PrescriptionArg
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:26.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionActivity : BaseActionBarActivity() {
    private lateinit var binding: ActivityPrescriptionEzaziBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPrescriptionEzaziBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        setupActionBar()
        setupPrescriptionView()
    }

    private fun setupPrescriptionView() {
        hasVisitId {
            PrescriptionArg(it, PrescriptionFragment.PrescriptionType.FULL, false).apply {
                supportFragmentManager.beginTransaction().replace(
                    binding.prescriptionContainer.id,
                    PrescriptionFragment.getInstance(this)
                ).commit()
            }
        }
    }

    private fun hasVisitId(block: (String) -> Unit) {
        if (intent.hasExtra(PrescriptionFragment.EXT_VISIT_ID)) {
            intent.getStringExtra(PrescriptionFragment.EXT_VISIT_ID)?.let { block.invoke(it) }
        }
    }

    override fun getScreenTitle(): Int = R.string.prescription

    companion object {

        @JvmStatic
        fun startPrescriptionActivity(
            context: Context,
            visitId: String
        ) {
            Intent(context, PrescriptionActivity::class.java).apply {
                putExtra(PrescriptionFragment.EXT_VISIT_ID, visitId)
            }.also { context.startActivity(it) }
        }
    }
}