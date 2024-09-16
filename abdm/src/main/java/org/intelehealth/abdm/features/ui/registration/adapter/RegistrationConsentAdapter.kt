package org.intelehealth.abdm.features.ui.registration.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.abdm.databinding.ItemAbhaRegistrationConsentBinding
import org.intelehealth.abdm.domain.model.RegistrationConsent

@Suppress("DEPRECATION")
class RegistrationConsentAdapter(
    private val modelList: List<RegistrationConsent>,
    private val onCheckboxChecked: OnCheckboxChecked
) : RecyclerView.Adapter<RegistrationConsentAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAbhaRegistrationConsentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: RegistrationConsent = modelList[position]
        holder.binding.chkBox.text = Html.fromHtml(model.consentText)
        holder.binding.chkBox.isChecked = model.isChecked
        if (position == modelList.size - 1 || position == modelList.size - 2) {
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(LEFT_MARGIN, 0, 0, 0)
            holder.binding.chkBox.layoutParams = layoutParams
        }
        holder.binding.chkBox.setOnCheckedChangeListener { _, isChecked ->
            model.isChecked = isChecked
            onCheckboxChecked.onOptionChecked(model)
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    fun areAllItemsChecked(): Boolean {
        for (model in modelList) {
            if (!model.isChecked) {
                return false
            }
        }
        return true
    }

    inner class MyViewHolder(val binding: ItemAbhaRegistrationConsentBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnCheckboxChecked {
        fun onOptionChecked(model: RegistrationConsent)
    }

    companion object {
        const val LEFT_MARGIN = 50
    }
}
