package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.utilities.NetworkConnection

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

            //holder.textView.text = getLocalizedModuleName(context, item.moduleName)
            holder.textView.text = item.displayName
            holder.imageView.setOnClickListener {
                if ((NetworkConnection.isOnline(context))) {
                    val pdfDialog = ShowInfoModuleDialog(context, item.url, item.moduleName)
                    pdfDialog.show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.could_not_connect_with_server),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }


    override fun getItemCount(): Int = moduleItems.size
}
