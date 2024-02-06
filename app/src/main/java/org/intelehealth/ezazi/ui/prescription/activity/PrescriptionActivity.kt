package org.intelehealth.ezazi.ui.prescription.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.ActivityPrescriptionEzaziBinding
import org.intelehealth.ezazi.partogram.PartogramConstants
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionAdapter
import org.intelehealth.ezazi.ui.prescription.viewmodel.PrescriptionViewModel
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.klivekit.utils.extensions.setupLinearView
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:26.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionActivity : BaseActionBarActivity(), BaseViewHolder.ViewHolderClickListener {
    private lateinit var binding: ActivityPrescriptionEzaziBinding
    private lateinit var adapter: PrescriptionAdapter
    private val viewMode: PrescriptionViewModel by lazy {
        ViewModelProvider(
            this, ViewModelProvider.Factory.from(PrescriptionViewModel.initializer)
        )[PrescriptionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPrescriptionEzaziBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        setupPrescriptionView()
    }

    private fun setupPrescriptionView() {
        adapter = PrescriptionAdapter(this, LinkedList())
        binding.contentPrescription.rvPrescription.setupLinearView(adapter)
        loadPrescriptions()
        observeState()
    }

    private fun observeState() {
        viewMode.loading.observe(this) {
            binding.contentPrescription.prescriptionProgressBar.apply {
                visibility = if (it) View.VISIBLE else View.GONE
            }
        }
        viewMode.errorDataResult.observe(this) {}
        viewMode.failDataResult.observe(this) {
            binding.contentPrescription.tvCallLogEmptyMessage.visibility = View.VISIBLE
        }
    }

    private fun loadPrescriptions() {
        hasVisitId {
            val creatorId = SessionManager(this).creatorID
            viewMode.getPrescriptions(it, creatorId).observe(this@PrescriptionActivity) {
                viewMode.handleResponse(it) { items ->
                    Timber.d { "Prescriptions => ${Gson().toJson(items)}" }
                    binding.contentPrescription.tvCallLogEmptyMessage.visibility = View.GONE
                    if (items.isNotEmpty()) {
                        adapter.updateItems(items.toMutableList())
                        adapter.setClickListener(this)
                        adapter.setAccessMode(PartogramConstants.AccessMode.READ)
                    } else viewMode.updateFailResult(getString(R.string.no_prescription))
                }
            }
        }
    }

    private fun hasVisitId(block: (String) -> Unit) {
        if (intent.hasExtra(EXT_VISIT_ID)) {
            intent.getStringExtra(EXT_VISIT_ID)?.let { block.invoke(it) }
        }
    }

    override fun getScreenTitle(): Int = R.string.prescription

    companion object {
        private const val EXT_VISIT_ID = "ext_visit_id"

        @JvmStatic
        fun startPrescriptionActivity(context: Context, visitId: String) {
            Intent(context, PrescriptionActivity::class.java).apply {
                putExtra(EXT_VISIT_ID, visitId)
            }.also { context.startActivity(it) }
        }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.btnExpandCollapseIndicator) {
            adapter.setExpandedItemPosition(position)
        }
    }
}