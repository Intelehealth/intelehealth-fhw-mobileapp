package org.intelehealth.ezazi.ui.prescription.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ezazi.databinding.RowItemPrescriptionPlanBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.partogram.model.Medication
import org.intelehealth.ezazi.partogram.model.Medicine
import org.intelehealth.ezazi.ui.prescription.holder.PrescriptionPlanHolder
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionAdapter(
    context: Context,
    items: LinkedList<ItemHeader>
) : PrescriptionMedicationAdapter(context, items) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isHeader()) HEADER
        else if (getItem(position) is ObsDTO) PLAN
        else if (getItem(position) is Medicine) MEDICINE
        else if (getItem(position) is Medication) MEDICATION
        else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PLAN) {
            val binding = RowItemPrescriptionPlanBinding.inflate(inflater, parent, false)
            PrescriptionPlanHolder(binding)
        } else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item.isHeader().not() && item is ObsDTO && holder is PrescriptionPlanHolder) {
            if (clickListener != null) holder.setViewClickListener(clickListener)
            holder.bind(item)
        } else super.onBindViewHolder(holder, position)
    }

    companion object {
        const val PLAN = 10001
    }
}