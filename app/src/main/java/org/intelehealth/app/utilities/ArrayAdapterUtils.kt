package org.intelehealth.app.utilities

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.ArrayRes
import androidx.annotation.LayoutRes
import org.intelehealth.app.R

/**
 * Created by Vaghela Mithun R. on 11-07-2024 - 11:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object ArrayAdapterUtils {
    fun getArrayAdapter(
        context: Context,
        @ArrayRes arrayResId: Int
    ) = ArrayAdapter.createFromResource(
        context,
        arrayResId, R.layout.ui2_custome_dropdown_item_view
    )

}