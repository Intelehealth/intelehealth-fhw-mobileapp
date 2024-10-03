package org.intelehealth.app.utilities

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.ArrayRes
import org.intelehealth.app.R
import org.intelehealth.app.activities.identificationActivity.model.Block

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

    fun <T> getObjectArrayAdapter(
        context: Context,
        list: List<T>
    ) = ArrayAdapter<T>(context, R.layout.ui2_custome_dropdown_item_view, list)

}