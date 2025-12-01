package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ItemNcdVitalReadingBinding
import org.intelehealth.app.models.NCDReading

/**
 * Adapter for displaying NCD readings using ViewBinding and ListAdapter
 *
 * Features:
 * - ViewBinding for type-safe view access
 * - DiffUtil for efficient updates
 * - Conditional styling based on data
 * - Optional item click support
 * - Last item divider handling
 */
class NCDReadingAdapter(
    private val onItemClick: ((NCDReading) -> Unit)? = null
) : ListAdapter<NCDReading, NCDReadingAdapter.NCDReadingViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NCDReadingViewHolder {
        val binding = ItemNcdVitalReadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NCDReadingViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: NCDReadingViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    /**
     * ViewHolder with ViewBinding
     */
    class NCDReadingViewHolder(
        private val binding: ItemNcdVitalReadingBinding,
        private val onItemClick: ((NCDReading) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reading: NCDReading, isLastItem: Boolean) {
            binding.apply {
                // Bind date
                tvDate.text = reading.date

                // Bind BP
                bindValue(
                    text = reading.bp,
                    textView = tvBp,
                    normalColor = R.color.colorAccent
                )

                // Bind HB
                bindValue(
                    text = reading.hb,
                    textView = tvHb,
                    normalColor = R.color.colorAccent
                )

                // Bind RBS with conditional coloring
                bindRbsValue(reading.rbs)

                // Handle divider visibility
              /*  bindivider.visibility = if (isLastItem) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }*/

                // Set click listener if provided
                setupClickListener(reading)
            }
        }

        /**
         * Bind a text value with color handling
         */
        private fun ItemNcdVitalReadingBinding.bindValue(
            text: String?,
            textView: android.widget.TextView,
            normalColor: Int
        ) {
            if (text.isNullOrBlank()) {
                textView.text = NCDReading.NA
                textView.setTextColor(
                    ContextCompat.getColor(root.context, R.color.gray)
                )
            } else {
                textView.text = text
                textView.setTextColor(
                    ContextCompat.getColor(root.context, normalColor)
                )
            }
        }

        /**
         * Bind RBS value with conditional coloring
         * Green for normal values (≤ 140), Orange for high values (> 140)
         */
        private fun ItemNcdVitalReadingBinding.bindRbsValue(value: String?) {
            if (value.isNullOrBlank()) {
                tvRbs.text = NCDReading.NA
                tvRbs.setTextColor(
                    ContextCompat.getColor(root.context, R.color.gray)
                )
            } else {
                tvRbs.text = value

                // Determine color based on RBS value
                val rbsValue = value.toIntOrNull() ?: 0
                val colorRes = when {
                    rbsValue > 140 -> R.color.red
                    else -> R.color.colorAccent
                }

                tvRbs.setTextColor(
                    ContextCompat.getColor(root.context, colorRes)
                )
            }
        }

        /**
         * Setup click listener with ripple effect
         */
        private fun ItemNcdVitalReadingBinding.setupClickListener(reading: NCDReading) {
            onItemClick?.let { clickListener ->
                root.setOnClickListener {
                    clickListener(reading)
                }

                // Add ripple effect
                val outValue = android.util.TypedValue()
                root.context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true
                )
                root.setBackgroundResource(outValue.resourceId)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class DiffCallback : DiffUtil.ItemCallback<NCDReading>() {
        override fun areItemsTheSame(oldItem: NCDReading, newItem: NCDReading): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: NCDReading, newItem: NCDReading): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Extension functions for easier adapter usage
 */
fun NCDReadingAdapter.updateReadings(readings: List<NCDReading>) {
    submitList(readings.toList()) // Create new list to trigger DiffUtil
}

fun NCDReadingAdapter.clearReadings() {
    submitList(emptyList())
}