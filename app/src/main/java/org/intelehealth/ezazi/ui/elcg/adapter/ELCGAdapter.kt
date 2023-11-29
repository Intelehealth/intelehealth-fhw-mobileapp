package org.intelehealth.ezazi.ui.elcg.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.Timber
import org.intelehealth.ezazi.databinding.RowItemElcgDataBinding
import org.intelehealth.ezazi.ui.elcg.adapter.viewholder.ELCGDataHolder
import org.intelehealth.ezazi.ui.elcg.model.ELCGData
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:02.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGAdapter(context: Context, objectsList: LinkedList<ItemHeader>) :
    ELCGStageHeaderAdapter(context, objectsList) {

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).isHeader()) return HEADER
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Timber.d { "onCreateViewHolder viewType : $viewType" }
        return if (viewType != HEADER) {
            val binding = RowItemElcgDataBinding.inflate(inflater, parent, false)
            ELCGDataHolder(binding)
        } else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItem(position) is ELCGData && holder is ELCGDataHolder)
            holder.bind(getItem(position) as ELCGData)
        else super.onBindViewHolder(holder, position)
    }
}