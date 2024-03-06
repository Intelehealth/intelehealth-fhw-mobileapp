package org.intelehealth.ezazi.ui.visit.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.core.Result
import org.intelehealth.ezazi.databinding.FragmentCommenListviewBinding
import org.intelehealth.ezazi.ui.visit.adapter.VisitStatusAdapter
import org.intelehealth.ezazi.ui.visit.viewmodel.VisitViewModel
import org.intelehealth.ezazi.utilities.SessionManager
import org.intelehealth.klivekit.chat.model.ItemHeader
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
        binding.emptyMessage = getEmptyDataMessage()
        binding.emptyDataIcon = getEmptyDataIcon()
        initListView()
    }

    private fun initListView() {
        adapter = VisitStatusAdapter(requireContext(), LinkedList())
        adapter.viewHolderClickListener = this
        binding.rvPrescription.setupLinearView(adapter)
    }

    open fun bindData(result: Result<List<ItemHeader>>) {
        viewMode.handleResponse(result) { visits ->
            Timber.d { "Outcome Pending Visit old ${Gson().toJson(visits)}" }
            binding.prescriptionProgressBar.isVisible = false
            if (visits.isNotEmpty()) {
                binding.tvCallLogEmptyMessage.isVisible = false
                adapter.updateItems(visits.toMutableList())
            }
        }
    }

    abstract fun getEmptyDataMessage(): String
    abstract fun getEmptyDataIcon(): Int
}