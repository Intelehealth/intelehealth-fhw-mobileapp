package org.intelehealth.ezazi.ui.elcg.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import org.intelehealth.ezazi.databinding.RowItemElcgEncounterBinding
import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.ui.dialog.adapter.BaseSelectedRecyclerViewAdapter
import org.intelehealth.ezazi.ui.elcg.adapter.viewholder.ELCGEncounterHolder
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewHolderAdapter

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:02.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGEncounterAdapter(context: Context, objectsList: List<EncounterDTO>) :
    BaseRecyclerViewHolderAdapter<EncounterDTO, ELCGEncounterHolder>(
        context,
        objectsList.toMutableList()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ELCGEncounterHolder {
        val binding = RowItemElcgEncounterBinding.inflate(inflater, parent, false)
        return ELCGEncounterHolder(binding)
    }

    override fun onBindViewHolder(holder: ELCGEncounterHolder, position: Int) {
        holder.bind(getItem(position))
        holder.hideTopLine(position == 0)
        holder.hideBelowLine(position == getList().size - 1)
    }
}