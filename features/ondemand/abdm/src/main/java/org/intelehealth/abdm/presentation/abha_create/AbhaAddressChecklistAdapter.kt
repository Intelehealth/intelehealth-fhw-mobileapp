package org.intelehealth.abdm.presentation.abha_create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.abdm.databinding.ItemAbhaAddressChecklistBinding

/** Single-select list of the patient's ABHA addresses, badging the preferred one. */
internal class AbhaAddressChecklistAdapter(
    private val abhaAddressList: List<String>,
    private val preferredAbhaAddress: String,
) : RecyclerView.Adapter<AbhaAddressChecklistAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAbhaAddressChecklistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val address = abhaAddressList[position]

        binding.mtvAbhaAddress.text = address
        binding.llPreferredIcon.isVisible = address == preferredAbhaAddress
        binding.mcbAbhaAddress.isChecked = position == selectedPosition

        binding.root.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = holder.adapterPosition
            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = abhaAddressList.size

    fun getSelectedAbhaAddress(): String? =
        selectedPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { abhaAddressList[it] }

    inner class ViewHolder(
        val binding: ItemAbhaAddressChecklistBinding,
    ) : RecyclerView.ViewHolder(binding.root)
}
