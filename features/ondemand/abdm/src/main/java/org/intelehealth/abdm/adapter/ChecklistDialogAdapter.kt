package org.intelehealth.abdm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.abdm.databinding.LayoutCheckListBinding

class CheckListDialogAdapter(
    private val abhaAddressList: List<String>,
    private val preferredAbhaAddress: String
) : RecyclerView.Adapter<CheckListDialogAdapter.CheckListViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckListViewHolder {
        val binding = LayoutCheckListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CheckListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CheckListViewHolder,
        position: Int
    ) {
        val binding = holder.binding
        val abha = abhaAddressList[position]

        binding.mtvAbhaAddress.text = abha
        binding.llPreferredIcon.isVisible = (abha == preferredAbhaAddress)
        binding.mcbAbhaAddress.isChecked = position == selectedPosition

        binding.root.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = holder.bindingAdapterPosition

            if (oldPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPos)
            }
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = abhaAddressList.size

    fun getSelectedAbhaAddress(): String? =
        selectedPosition.takeIf { it != RecyclerView.NO_POSITION }
            ?.let { abhaAddressList[it] }

    inner class CheckListViewHolder(
        val binding: LayoutCheckListBinding
    ) : RecyclerView.ViewHolder(binding.root)
}