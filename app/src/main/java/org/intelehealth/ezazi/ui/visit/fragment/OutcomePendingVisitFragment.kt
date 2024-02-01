package org.intelehealth.ezazi.ui.visit.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.app.IntelehealthApplication
import org.intelehealth.ezazi.core.Result
import org.intelehealth.ezazi.databinding.FragmentVisitStatusListBinding
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.ui.visit.adapter.DecisionPendingVisitsAdapter
import org.intelehealth.ezazi.ui.visit.viewmodel.VisitViewModel
import org.intelehealth.ezazi.utilities.SessionManager

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class OutcomePendingVisitFragment : Fragment(R.layout.fragment_visit_status_list) {
    private lateinit var binding: FragmentVisitStatusListBinding
    lateinit var recyclerView: RecyclerView

    private val viewMode: VisitViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.Factory.from(VisitViewModel.initializer)
        )[VisitViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVisitStatusListBinding.bind(view)
        loadOutcomePendingVisits()
    }

    private fun loadOutcomePendingVisits() {
        recyclerView = binding.rvVisitStatus
        viewMode.outcomePendingVisits(0, 20, SessionManager(requireContext()).providerID)
            .observe(viewLifecycleOwner) {
                viewMode.handleResponse(it) { visits ->
                    Timber.d { "Outcome Pending Visit ${Gson().toJson(visits)}" }
                    bindDataToUI(it)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            requireContext(),
            visitOutOfTimeReceiver,
            IntentFilter(AppConstants.VISIT_DECISION_PENDING_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        loadOutcomePendingVisits()
    }

    companion object {
        fun newInstance(): OutcomePendingVisitFragment {
            return OutcomePendingVisitFragment()
        }
    }

    private fun bindDataToUI(result: Result<List<PatientDTO>>) {
        val dataList = result.data
        recyclerView.adapter as? DecisionPendingVisitsAdapter
            ?: activity?.let {
                dataList?.let {
                    DecisionPendingVisitsAdapter(
                        dataList,
                        IntelehealthApplication.getAppContext()
                    ).apply {
                        recyclerView.adapter = this
                        notifyDataSetChanged()
                    }
                }
            }
    }

    private val visitOutOfTimeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(AppConstants.VISIT_DECISION_PENDING_ACTION)) {
                loadOutcomePendingVisits()
                // sync()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(visitOutOfTimeReceiver)
    }
}