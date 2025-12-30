package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo


import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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
 * - ViewBinding for type-safe view access
 * - DiffUtil for efficient updates
 * - Conditional styling based on data
 * - Optional item click support
 * - Last item divider handling
 */
class NCDReadingAdapter(
    private val isMale: Boolean,
    private val onItemClick: ((NCDReading) -> Unit)? = null
) : ListAdapter<NCDReading, NCDReadingAdapter.NCDReadingViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NCDReadingViewHolder {
        val binding = ItemNcdVitalReadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NCDReadingViewHolder(binding,isMale, onItemClick)
    }

    override fun onBindViewHolder(holder: NCDReadingViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    /**
     * ViewHolder with ViewBinding
     */
    class NCDReadingViewHolder(
        private val binding: ItemNcdVitalReadingBinding,
        private val isMale: Boolean,
        private val onItemClick: ((NCDReading) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reading: NCDReading, isLastItem: Boolean) {
            binding.apply {
                // Bind date
                tvDate.text = reading.date

                // Bind BP
                bindBpValue(
                   reading.bp

                )

                // Bind HB
                bindHbValue(
                    reading.hb
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

        private fun ItemNcdVitalReadingBinding.bindHbValue(value: String?) {
            if (value?.trim().isNullOrBlank()) {
                tvHb.text = NCDReading.NA
                tvHb.setTextColor(
                    ContextCompat.getColor(root.context, R.color.gray_4_1)
                )
            } else {
                tvHb.text = value

                val hbValue = value.toFloatOrNull() ?: 0f

                // Hb category color logic
                val colorRes = when {
                    // Normal
                    (isMale && hbValue >= 13f) || (!isMale && hbValue >= 12f) ->
                        R.color.green

                    // Mild
                    (isMale && hbValue in 11.0f..12.9f) ||
                            (!isMale && hbValue in 11.0f..11.9f) ->
                        R.color.yellow

                    // Moderate
                    hbValue in 8.0f..10.9f ->
                        R.color.orange

                    // Severe
                    hbValue < 8f ->
                        R.color.red

                    else ->
                        R.color.gray_4_1
                }

                tvHb.setTextColor(
                    ContextCompat.getColor(root.context, colorRes)
                )
            }
        }


        /**
         * Bind a text value with color handling
         */
        private fun ItemNcdVitalReadingBinding.bindBpValue(
            bpText: String?
        ) {
            var sysText: String? = null
            var diaText: String? = null

            if(!bpText.isNullOrBlank()){
                val bpArray = bpText.split("/")
                if(bpArray.size == 2){
                    sysText = bpArray[0]
                    diaText = bpArray[1]
                }
            }

            if (sysText.isNullOrBlank() || diaText.isNullOrBlank()) {
                tvBp.text = NCDReading.NA
                tvBp.setTextColor(
                    ContextCompat.getColor(root.context, R.color.gray_4_1)
                )
                return
            }

            val sys = sysText.toIntOrNull() ?: 0
            val dia = diaText.toIntOrNull() ?: 0

            val context = root.context

            // Default Colors
            var sysColor = ContextCompat.getColor(context, R.color.ui2_bp_default_ekal)
            var diaColor = ContextCompat.getColor(context, R.color.ui2_bp_default_ekal)

            // SYS logic
            sysColor = when {
                sys in 90..119 -> ContextCompat.getColor(context, R.color.ui2_sys1_ekal)
                sys in 120..139 -> ContextCompat.getColor(context, R.color.ui2_sys2_ekal)
                else -> sysColor
            }

            // DIA logic
            diaColor = when {
                dia < 80 -> ContextCompat.getColor(context, R.color.ui2_dia1_ekal)
                dia in 80..99 -> ContextCompat.getColor(context, R.color.ui2_dia2_ekal)
                else -> diaColor
            }

            // BP text (e.g., "120/80")
            val bpString = "$sysText/$diaText"

            // Apply two-color spannable
            val spannable = SpannableString(bpString).apply {
                setSpan(
                    ForegroundColorSpan(sysColor),
                    0,
                    sysText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                setSpan(
                    ForegroundColorSpan(diaColor),
                    sysText.length + 1,
                    bpString.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            tvBp.text = spannable
        }


        /**
         * Bind RBS value with conditional coloring
         * Green for normal values (≤ 140), Orange for high values (> 140)
         */
        private fun ItemNcdVitalReadingBinding.bindRbsValue(value: String?) {
            if (value.isNullOrBlank()) {
                tvRbs.text = NCDReading.NA
                tvRbs.setTextColor(
                    ContextCompat.getColor(root.context, R.color.gray_4_1)
                )
            } else {
                tvRbs.text = value

                // Determine color based on RBS value
                val rbsValue = value.toIntOrNull() ?: 0
                val colorRes = when {
                    rbsValue < 70 -> R.color.red          // Hypoglycemia
                    rbsValue in 70..139 -> R.color.green  // Normal
                    rbsValue in 140..199 -> R.color.yellow // Pre-Diabetes
                    rbsValue >= 200 -> R.color.orange2      // Suspected Diabetes
                    else -> R.color.gray_4_1                   // Fallback for invalid values
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