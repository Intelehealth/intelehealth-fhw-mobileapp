package org.intelehealth.klivekit.chat.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.Timber
import org.intelehealth.klivekit.chat.model.CMessage
import org.intelehealth.klivekit.chat.model.ItemHeader
import org.intelehealth.klivekit.chat.model.MessageStatus
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.ReceiverViewHolder
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.SenderViewHolder
import org.intelehealth.klivekit.databinding.RowMsgItemReceiverBinding
import org.intelehealth.klivekit.databinding.RowMsgItemSenderBinding
import org.intelehealth.klivekit.utils.Constants

/**
 * Created by Vaghela Mithun R. on 14-08-2023 - 18:52.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatMessageAdapter(context: Context, list: MutableList<ItemHeader>) :
    DayHeaderAdapter(context, list) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isHeader()) DateHeaderAdapter.DATE_HEADER
        else if (getItem(position) is CMessage) {
            val message = getItem(position) as CMessage
            message.layoutType
        } else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.LEFT_ITEM_DOCT -> {
                val binding = RowMsgItemReceiverBinding.inflate(inflater, parent, false)
                ReceiverViewHolder(binding)
            }

            Constants.RIGHT_ITEM_HW -> {
                val binding = RowMsgItemSenderBinding.inflate(inflater, parent, false)
                SenderViewHolder(binding)
            }

            else -> super.onCreateViewHolder(parent!!, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItem(position) is CMessage) {
            val message = getItem(position) as CMessage
            if (holder is ReceiverViewHolder && message.layoutType == Constants.LEFT_ITEM_DOCT) {
                holder.bind(message)
            } else if (holder is SenderViewHolder && message.layoutType == Constants.RIGHT_ITEM_HW) {
                holder.bind(message)
            }
        } else super.onBindViewHolder(holder, position)
    }


    fun markMessageAsRead(id: Int) {
        for (i in items.indices) {
            if (items.get(i) is CMessage) {
                val cMessage = items.get(i) as CMessage
                //                if (id == chatMessage.getId()) {
                cMessage.isRead = true
                cMessage.messageStatus = MessageStatus.READ.value
                notifyItemChanged(i)
                //                    break;
//                }
            }
        }
    }

    fun markMessageAsDelivered(id: Int) {
        for (i in items.indices) {
            if (items.get(i) is CMessage) {
                val cMessage = items.get(i) as CMessage
                if (id == cMessage.messageId) {
                    cMessage.isRead = false
                    cMessage.messageStatus = MessageStatus.DELIVERED.value
                    Timber.e { "markMessageAsDelivered: " + cMessage.message }
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    fun updatedMessage(message: CMessage) {
        for (i in items.indices) {
            if (items.get(i) is CMessage) {
                val cMessage = items.get(i) as CMessage
                if (message.message == cMessage.message) {
                    cMessage.messageId = message.messageId
                    cMessage.messageStatus = message.messageStatus
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }
}