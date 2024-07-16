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
import org.intelehealth.app.activities.notification.listeners.NotificationClickListener
import org.intelehealth.app.activities.notification.view.NotificationAdapter.MyHolderView
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.NotificationModel

/**
 * Created by Prajwal Waingankar on 27/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
class NotificationAdapter(
    private var patientDTOList: List<NotificationModel>?,
    private val clickListener: NotificationClickListener
) : RecyclerView.Adapter<MyHolderView>() {
    private var mContext: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolderView {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val row = inflater.inflate(R.layout.notification_list_item, parent, false)
        return MyHolderView(row)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolderView, position: Int) {
        patientDTOList?.get(position)?.let { model ->

            model.description?.let {
                holder.search_name.text = it
            }

            holder.open_presc_btn.setOnClickListener { _: View? ->
                clickListener.openNotification(
                    model, position
                )
            }
        }
        patientDTOList?.get(position)?.let { model ->

            model.description?.let {
                holder.search_name.text = it
            }
            holder.delete_imgview.setOnClickListener { _: View? ->
                clickListener.deleteNotification(model, holder.layoutPosition)
            }

            if(model.notification_type == NotificationDbConstants.FOLLOW_UP_NOTIFICATION){
                holder.buttonText.text = ContextCompat.getString(mContext!!,R.string.open_followup)
            }else{
                holder.buttonText.text = ContextCompat.getString(mContext!!,R.string.open_prescription)
            }
            holder.open_presc_btn.setOnClickListener {
                clickListener.openNotification(
                    model, position
                )
            }
        }

    }

    override fun getItemCount(): Int {
        return patientDTOList?.size ?: 0
    }

    inner class MyHolderView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var search_name: TextView
        var open_presc_btn: LinearLayout
        var buttonText: TextView
        var scroll_relative: RelativeLayout
        var delete_imgview: ImageView
        var fu_cardview_item: CardView

        init {
            search_name = itemView.findViewById(R.id.search_name)
            scroll_relative = itemView.findViewById(R.id.scroll_relative)
            buttonText = itemView.findViewById(R.id.button_text)
            delete_imgview = itemView.findViewById(R.id.delete_imgview)
            open_presc_btn = itemView.findViewById(R.id.open_presc_btn)
            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item)
        }
    }
}
