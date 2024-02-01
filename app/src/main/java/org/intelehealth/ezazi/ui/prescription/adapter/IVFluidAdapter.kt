package org.intelehealth.ezazi.ui.prescription.adapter

import android.content.Context
import org.intelehealth.ezazi.partogram.adapter.MedicineAdapter
import org.intelehealth.ezazi.ui.elcg.adapter.StageHeaderAdapter
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class IVFluidAdapter(
    context: Context,
    items: LinkedList<ItemHeader>
) : MedicineAdapter(context, items) {

}