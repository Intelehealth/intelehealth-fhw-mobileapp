package org.intelehealth.klivekit.chat.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Created by Vaghela Mithun R. on 14-08-2023 - 19:07.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class BaseRecyclerViewHolderAdapter<I, VH : ViewHolder>(
    protected val context: Context,
    protected var items: MutableList<I>
) : RecyclerView.Adapter<VH>() {

    protected val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = items.size

    fun addItem(item: I) {
        items.add(item)
        notifyItemRangeChanged(0, items.size)
    }

    fun addItemAt(position: Int, item: I) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun remove(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun remove(item: I) {
        remove(items.indexOf(item))
    }

    fun updateItems(newItems: MutableList<I>) {
        items = newItems;
        notifyItemRangeChanged(0, items.size)
    }

    fun getItem(position: Int) = items.get(position)
}