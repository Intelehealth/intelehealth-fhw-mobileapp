package org.intelehealth.app.activities.notification.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.activities.notification.NotificationList
import org.intelehealth.app.activities.notification.listeners.CloudNotificationClickListener
import org.intelehealth.app.activities.notification.listeners.NotificationClickListener
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.NotificationModel

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
class NotificationCloudAdapter(
    private var patientDTOList: List<NotificationList>?,
    private val clickListener: CloudNotificationClickListener
) : RecyclerView.Adapter<NotificationCloudAdapter.MyHolderView>() {
    private lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolderView {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val row = inflater.inflate(R.layout.cd_notification_list_item, parent, false)
        return MyHolderView(row)
    }

    override fun onBindViewHolder(holder: MyHolderView, position: Int) {
        patientDTOList?.get(position)?.let { model ->

            model.description?.let {
                holder.tvDescription.text = it
            }
            model.title?.let {
                holder.tvTitle.text = it
            }

        }
        patientDTOList?.get(position)?.let { model ->


            holder.delete_imgview.setOnClickListener { _: View? ->
                clickListener.deleteNotification(model, holder.layoutPosition)
            }
            holder.scroll_relative.setOnClickListener { _: View? ->
                clickListener.updateReadStatus(model, holder.layoutPosition)
            }

            holder.scroll_relative.isSelected = model.isRead == NotificationDbConstants.UN_READ_STATUS
        }

    }

    override fun getItemCount(): Int {
        return patientDTOList?.size ?: 0
    }

    inner class MyHolderView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView
        var tvDescription: TextView
        var scroll_relative: RelativeLayout
        var delete_imgview: ImageView
        var fu_cardview_item: CardView

        init {
            tvTitle = itemView.findViewById(R.id.tvTitle)
            tvDescription = itemView.findViewById(R.id.tvDescription)
            scroll_relative = itemView.findViewById(R.id.scroll_relative)

            delete_imgview = itemView.findViewById(R.id.delete_imgview)

            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item)
        }
    }
}
