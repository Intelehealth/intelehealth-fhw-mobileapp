package org.intelehealth.abdm.presentation.consent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import org.intelehealth.abdm.R

/** Renders the consent checklist. Behaviour ported verbatim from the legacy module. */
internal class CheckboxAdapter(
    private val context: Context,
    private val modelList: List<CheckBoxRecyclerModel>,
    private val onCheckboxChecked: (CheckBoxRecyclerModel) -> Unit,
) : RecyclerView.Adapter<CheckboxAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_consent_checkbox, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = modelList[position]
        holder.chkBox.text = HtmlCompat.fromHtml(model.checkboxText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.chkBox.isChecked = model.isChecked
        holder.chkBox.isEnabled = model.isCheckboxEnabled
        holder.chkBox.setTextColor(
            if (model.isCheckboxEnabled) context.getColor(R.color.text_heading)
            else context.getColor(R.color.text_secondary)
        )

        if (position == modelList.size - 1 || position == modelList.size - 2) {
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            )
            layoutParams.setMargins(LEFT_MARGIN, 0, 0, 0)
            holder.chkBox.layoutParams = layoutParams
        }

        if (model.isCheckboxEnabled) {
            holder.chkBox.setOnCheckedChangeListener { _, isChecked ->
                model.isChecked = isChecked
                onCheckboxChecked(model)
            }
        }
    }

    override fun getItemCount(): Int = modelList.size

    fun areAllItemsChecked(): Boolean =
        modelList.none { it.isCheckboxEnabled && !it.isChecked }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chkBox: MaterialCheckBox = itemView.findViewById(R.id.chkBox)
    }

    companion object {
        const val LEFT_MARGIN = 50
    }
}
