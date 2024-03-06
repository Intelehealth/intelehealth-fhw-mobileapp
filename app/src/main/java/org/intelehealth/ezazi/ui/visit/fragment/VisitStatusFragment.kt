package org.intelehealth.ezazi.ui.visit.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.databinding.FragmentCommenListviewBinding
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.ui.visit.adapter.VisitStatusAdapter
import org.intelehealth.ezazi.ui.visit.viewmodel.VisitViewModel
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.klivekit.utils.extensions.setupLinearView
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class VisitStatusFragment : Fragment(R.layout.fragment_commen_listview),
    BaseViewHolder.ViewHolderClickListener {
    protected lateinit var binding: FragmentCommenListviewBinding
    protected lateinit var adapter: VisitStatusAdapter

    protected val viewMode: VisitViewModel by lazy {
        ViewModelProvider(
            this, ViewModelProvider.Factory.from(VisitViewModel.initializer)
        )[VisitViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommenListviewBinding.bind(view)
        binding.emptyMessage = getString(
            R.string.no_data_message,
            getString(R.string.decision_pending_title)
        )
        binding.emptyDataIcon = R.drawable.ic_outcome_pending
        initListView()
        loadOutcomePendingVisits()
    }

    private fun initListView() {
        adapter = VisitStatusAdapter(requireContext(), LinkedList())
        adapter.viewHolderClickListener = this
        binding.rvPrescription.setupLinearView(adapter)
    }

    private fun loadOutcomePendingVisits() {
        val providerId = SessionManager(requireContext()).providerID
        viewMode.outcomePendingVisits(0, 20, providerId).observe(viewLifecycleOwner) {
            viewMode.handleResponse(it) { visits ->
                Timber.d { "Outcome Pending Visit old ${Gson().toJson(visits)}" }
                binding.prescriptionProgressBar.isVisible = false
                if (visits.isNotEmpty()) {
                    binding.tvCallLogEmptyMessage.isVisible = false
                    adapter.updateItems(visits.toMutableList())
                }
            }
        }
    }
}