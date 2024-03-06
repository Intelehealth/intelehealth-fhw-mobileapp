package org.intelehealth.ezazi.ui.visit.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.github.ajalt.timberkt.Timber
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.utilities.SessionManager

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class OutcomePendingVisitFragment : VisitStatusFragment() {
    private var isReceiverRegistered = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadOutcomePendingVisits()
    }

    override fun getEmptyDataMessage(): String = getString(
        R.string.no_data_message,
        getString(R.string.decision_pending_title)
    )

    override fun getEmptyDataIcon(): Int = R.drawable.ic_outcome_pending

    private fun loadOutcomePendingVisits() {
        val providerId = SessionManager(requireContext()).providerID
        viewMode.outcomePendingVisits(0, 20, providerId).observe(viewLifecycleOwner) {
            bindData(it)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isReceiverRegistered) {
            ContextCompat.registerReceiver(
                requireActivity(),
                visitOutOfTimeReceiver,
                IntentFilter(AppConstants.VISIT_DECISION_PENDING_ACTION),
                ContextCompat.RECEIVER_EXPORTED
            )
            isReceiverRegistered = true
        }
        loadOutcomePendingVisits()
    }

    companion object {
        fun newInstance(): OutcomePendingVisitFragment {
            return OutcomePendingVisitFragment()
        }
    }

    private val visitOutOfTimeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(AppConstants.VISIT_DECISION_PENDING_ACTION)) {
                loadOutcomePendingVisits()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            try {
                requireActivity().unregisterReceiver(visitOutOfTimeReceiver)
                isReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                Timber.e { "OutcomePendingError ${e.localizedMessage}" }
                e.printStackTrace()
            }
        }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.clRowVisitStatus) {
            val patient = view.tag as PatientDTO
            startStartTimelineActivity(patient)
        }
    }

    private fun startStartTimelineActivity(patient: PatientDTO) {
        Intent(requireContext(), TimelineVisitSummaryActivity::class.java).apply {
            putExtra("patientNameTimeline", patient.fullName)
            putExtra("patientUuid", patient.uuid)
            putExtra("visitUuid", patient.visitUuid)
            putExtra("providerID", SessionManager(requireContext()).providerID)
            putExtra("tag", "")
        }.also { startActivity(it) }
    }
}