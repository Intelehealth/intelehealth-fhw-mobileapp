package org.intelehealth.ezazi.ui.elcg.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ezazi.databinding.RowItemElcgStageHeaderBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.ui.elcg.adapter.viewholder.ELCGStageHeaderHolder
import org.intelehealth.ezazi.ui.elcg.model.CategoryHeader
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionAdapter
import org.intelehealth.klivekit.chat.model.ItemHeader
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:02.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
abstract class CategoryHeaderAdapter(context: Context, objectsList: LinkedList<ItemHeader>) :
    BaseRecyclerViewAdapter<ItemHeader>(context, objectsList) {

    override fun getItemViewType(position: Int): Int {
        return if(getItem(position).isHeader()) HEADER
        else super.getItemViewType(position)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER) {
            val binding = RowItemElcgStageHeaderBinding.inflate(inflater, parent, false)
            ELCGStageHeaderHolder(binding)
        } else super.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItem(position) is CategoryHeader && holder is ELCGStageHeaderHolder)
            holder.bind(getItem(position) as CategoryHeader)
        else super.bindViewHolder(holder, position)
    }

    companion object {
        const val HEADER = 1000
    }
}