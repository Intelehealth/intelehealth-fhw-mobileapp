package org.intelehealth.abdm.presentation.abha_verify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.abdm.databinding.ItemCompareFieldBinding

/**
 * Renders the compare rows. Selection is fixed to the ABHA side (locked in the layout), so this
 * only binds the label and the two values — there is no per-row state to track.
 */
internal class CompareFieldAdapter(
    private val fields: List<CompareField>,
) : RecyclerView.Adapter<CompareFieldAdapter.FieldViewHolder>() {

    inner class FieldViewHolder(
        private val binding: ItemCompareFieldBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(field: CompareField) {
            binding.tvLabel.setText(field.labelRes)
            binding.rbLocal.text = field.localValue
            binding.rbAbha.text = field.abhaValue
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val binding = ItemCompareFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FieldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        holder.bind(fields[position])
    }

    override fun getItemCount(): Int = fields.size
}
