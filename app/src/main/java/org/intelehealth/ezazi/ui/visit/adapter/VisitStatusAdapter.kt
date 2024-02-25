package org.intelehealth.ezazi.ui.visit.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ezazi.databinding.ListItemVisitStatusPatientEzaziBinding
import org.intelehealth.ezazi.databinding.RowItemPrescriptionMedicationBinding
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.partogram.model.Medication
import org.intelehealth.ezazi.ui.elcg.adapter.CategoryHeaderAdapter
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionMedicationAdapter
import org.intelehealth.ezazi.ui.prescription.holder.PrescriptionMedicationHolder
import org.intelehealth.ezazi.ui.visit.viewholder.OutcomePendingViewHolder
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 25-02-2024 - 18:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VisitStatusAdapter(
    context: Context,
    item: LinkedList<ItemHeader>
) : CategoryHeaderAdapter(context, item) {

    override fun getItemViewType(position: Int): Int {
        val patient = getItem(position)
        if (patient.isHeader().not() && patient is PatientDTO) return OUTCOME_PENDING
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == OUTCOME_PENDING) {
            val binding = ListItemVisitStatusPatientEzaziBinding.inflate(inflater, parent, false)
            OutcomePendingViewHolder(binding)
        } else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item.isHeader().not() && item is PatientDTO && holder is OutcomePendingViewHolder) {
            viewHolderClickListener?.let {
                holder.setViewClickListener(it)
            }
            holder.bind(item)
        } else super.onBindViewHolder(holder, position)
    }

    companion object {
        const val OUTCOME_PENDING = 5001
    }
}