package org.intelehealth.ezazi.ui.elcg.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ezazi.databinding.RowItemElcgStageHeaderBinding
import org.intelehealth.ezazi.ui.elcg.adapter.viewholder.ELCGStageHeaderHolder
import org.intelehealth.ezazi.ui.elcg.model.StageHeader
import org.intelehealth.klivekit.chat.model.ItemHeader
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:02.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
abstract class StageHeaderAdapter(context: Context, objectsList: LinkedList<ItemHeader>) :
    BaseRecyclerViewAdapter<ItemHeader>(context, objectsList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowItemElcgStageHeaderBinding.inflate(inflater, parent, false)
        return ELCGStageHeaderHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItem(position) is StageHeader && holder is ELCGStageHeaderHolder)
            holder.bind(getItem(position) as StageHeader)
        else super.bindViewHolder(holder, position)
    }

    companion object {
        const val HEADER = 1000
    }
}