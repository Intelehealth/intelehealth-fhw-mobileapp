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
abstract class BaseRecyclerViewAdapter<I>(
    private val ctx: Context,
    private var lists: MutableList<I>
) : BaseRecyclerViewHolderAdapter<I, ViewHolder>(ctx, lists)