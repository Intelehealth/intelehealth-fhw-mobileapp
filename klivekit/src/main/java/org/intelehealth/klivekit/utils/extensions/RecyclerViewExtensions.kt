package org.intelehealth.klivekit.utils.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Vaghela Mithun R. on 04-02-2024 - 00:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun RecyclerView.setupLinearView(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    layoutManager = LinearLayoutManager(this.context)
    this.adapter = adapter
}