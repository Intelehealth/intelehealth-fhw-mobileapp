package org.intelehealth.ezazi.ui.prescription.listener

import org.intelehealth.klivekit.chat.model.ItemHeader
import java.io.Serializable
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 25-02-2024 - 00:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface MedicineChangeListener : Serializable {
    fun onMedicineListChanged(updated: List<ItemHeader>)

    fun getExistingList(): LinkedList<ItemHeader>
}