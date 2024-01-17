package org.intelehealth.ezazi.ui.visit.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.FragmentVisitStatusListBinding
import org.intelehealth.ezazi.ui.visit.viewmodel.VisitViewModel
import org.intelehealth.ezazi.utilities.SessionManager

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class OutcomePendingVisitFragment : Fragment(R.layout.fragment_visit_status_list) {
    private lateinit var binding: FragmentVisitStatusListBinding
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
        val sessionManager = SessionManager(requireContext())
        viewMode.outcomePendingVisits(0, 20, sessionManager.providerID)
            .observe(viewLifecycleOwner) {
                viewMode.handleResponse(it) { visits ->
                    Timber.d { "Outcome Pending Visit ${Gson().toJson(visits)}" }
                }
            }
    }

    companion object {
        fun newInstance(): OutcomePendingVisitFragment {
            return OutcomePendingVisitFragment()
        }
    }
}