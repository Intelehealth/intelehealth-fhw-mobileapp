package org.intelehealth.ezazi.ui.prescription.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.ViewModelInitializer
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.core.BaseViewModel
import org.intelehealth.ezazi.ui.prescription.data.PrescriptionRepository
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment
import org.intelehealth.ezazi.ui.prescription.model.PrescriptionArg
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:33.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionViewModel(private val repository: PrescriptionRepository) : BaseViewModel() {

    private var dataItems = MutableLiveData<LinkedList<ItemHeader>>()
    val liveItems: LiveData<LinkedList<ItemHeader>> get() = dataItems

    var medicationItems = LinkedList<ItemHeader>();

    var prescriptionArg: PrescriptionArg? = null


    fun getPrescriptions(
        visitId: String,
        type: PrescriptionFragment.PrescriptionType,
        allowAdminister: Boolean
    ) = executeLocalQuery {
        repository.fetchPrescription(visitId, type, allowAdminister)
    }.asLiveData()

    fun updateItem(position: Int, item: ItemHeader) {
        this.medicationItems[position] = item
        dataItems.postValue(this.medicationItems)
    }

    fun addItem(item: ItemHeader) {
        this.medicationItems.add(0, item)
        dataItems.postValue(this.medicationItems)
    }

    fun updateItemList(items: LinkedList<ItemHeader>) {
        this.medicationItems = items
        dataItems.postValue(this.medicationItems)
    }

    companion object {
        val initializer = ViewModelInitializer(PrescriptionViewModel::class.java) {
            return@ViewModelInitializer PrescriptionRepository(AppConstants.inteleHealthDatabaseHelper.readableDatabase).let {
                return@let PrescriptionViewModel(it)
            }
        }
    }
}