package org.intelehealth.ezazi.ui.elcg.adapter.viewholder

import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ajalt.timberkt.Timber
import org.intelehealth.ezazi.databinding.RowItemElcgDataBinding
import org.intelehealth.ezazi.ui.elcg.adapter.ELCGEncounterAdapter
import org.intelehealth.ezazi.ui.elcg.model.ELCGData
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:03.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGDataHolder(private val binding: RowItemElcgDataBinding) : BaseViewHolder(
    binding.root
) {
    fun bind(data: ELCGData) {
        Timber.d { "ELCGDataHolder ${data.hour}" }
        binding.tvElcgHour.text = "${data.hour}"
        binding.rvEncounters.layoutManager = LinearLayoutManager(binding.rvEncounters.context)
        binding.rvEncounters.adapter =
            ELCGEncounterAdapter(binding.rvEncounters.context, data.encounters)
    }
}