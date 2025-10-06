package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R

class NcdHealthInfoAdapter(
    private val moduleItems: List<HealthModuleItem>, // your model list
    private val context: Context
) : RecyclerView.Adapter<NcdHealthInfoAdapter.ModuleViewHolder>() {

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutItem: RelativeLayout = itemView.findViewById(R.id.layout_item_exercise)
        val textView: TextView = itemView.findViewById(R.id.tv_exercise)
        val imageView: ImageView = itemView.findViewById(R.id.iv_exercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_health_info_item, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val item = moduleItems[position]

        if (item.url.isBlank()) {
            holder.layoutItem.visibility = View.GONE
        } else {
            holder.layoutItem.visibility = View.VISIBLE

            holder.textView.text = getLocalizedModuleName(context, item.moduleName)
            holder.imageView.setOnClickListener {
                val pdfDialog = ShowInfoModuleDialog(context, item.url, item.moduleName)
                pdfDialog.show()
            }
        }
    }


    override fun getItemCount(): Int = moduleItems.size

    private fun getLocalizedModuleName(context: Context, moduleName: String): String {
        return when (moduleName.lowercase()) {
            "decrease salt intake" -> context.getString(R.string.decrease_salt_intake)
            "exercise" -> context.getString(R.string.exercise)
            "alcohol and tobacco cessation" -> context.getString(R.string.alcohol_and_tobacco_cessation)

            else -> moduleName
        }
    }

}
