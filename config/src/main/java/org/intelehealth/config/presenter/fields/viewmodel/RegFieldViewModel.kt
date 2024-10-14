package org.intelehealth.config.presenter.fields.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.room.entity.PatientRegistrationFields
import org.intelehealth.config.utility.FieldGroup
import org.intelehealth.core.ui.viewmodel.BaseViewModel

/**
 * Created by Vaghela Mithun R. on 12-04-2024 - 12:56.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class RegFieldViewModel(private val repository: RegFieldRepository) : BaseViewModel() {

    private var personalSectionFieldsData = MutableLiveData<List<PatientRegistrationFields>>()
    val personalSectionFieldsLiveData: LiveData<List<PatientRegistrationFields>> get() = personalSectionFieldsData

    private var addressSectionFieldsData = MutableLiveData<List<PatientRegistrationFields>>()
    val addressSectionFieldsLiveData: LiveData<List<PatientRegistrationFields>> get() = addressSectionFieldsData

    private var otherSectionFieldsData = MutableLiveData<List<PatientRegistrationFields>>()
    val otherSectionFieldsLiveData: LiveData<List<PatientRegistrationFields>> get() = otherSectionFieldsData

    fun fetchEnabledPersonalRegFields() = repository.getAllEnabledGroupField(FieldGroup.PERSONAL)

    fun fetchEnabledAddressRegFields() = repository.getAllEnabledGroupField(FieldGroup.ADDRESS)

    fun fetchEnabledOtherRegFields() = repository.getAllEnabledGroupField(FieldGroup.OTHER)

    fun fetchEnabledAllRegFields() = repository.getAllEnabledLiveFields()

    fun fetchPersonalRegFields() {
        viewModelScope.launch {
            val personalFields = repository.getGroupFields(FieldGroup.PERSONAL)
            personalSectionFieldsData.postValue(personalFields)
        }
    }

    fun fetchAddressRegFields() {
        viewModelScope.launch {
            val addressFields = repository.getGroupFields(FieldGroup.ADDRESS)
            addressSectionFieldsData.postValue(addressFields)
        }
    }

    fun fetchOtherRegFields() {
        viewModelScope.launch {
            val otherFields = repository.getGroupFields(FieldGroup.OTHER)
            otherSectionFieldsData.postValue(otherFields)
        }
    }
}