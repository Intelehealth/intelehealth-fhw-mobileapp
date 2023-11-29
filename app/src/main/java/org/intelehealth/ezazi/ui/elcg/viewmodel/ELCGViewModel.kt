package org.intelehealth.ezazi.ui.elcg.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.ui.elcg.data.ELCGRepository
import org.intelehealth.ezazi.ui.elcg.model.ELCGGraph
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.ArrayList
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 23:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class ELCGViewModel constructor(private val repository: ELCGRepository) : ViewModel() {
    private var encounterLiveData = MutableLiveData<ArrayList<EncounterDTO>>()
    val elcgEncounterData: LiveData<ArrayList<EncounterDTO>> get() = encounterLiveData

    private var visitCompleteStatusData = MutableLiveData(false)
    val visitCompletedStatus: LiveData<Boolean> get() = visitCompleteStatusData

    private var elcgGraphData = MutableLiveData<LinkedList<ItemHeader>>()
    val elcgGraphLiveData: LiveData<LinkedList<ItemHeader>> get() = elcgGraphData

    fun loadELCGData(visitId: String) {
        viewModelScope.launch {
            val encounters = repository.fetchELCGData(visitId)
            encounterLiveData.postValue(encounters)
        }
    }

    fun checkVisitCompletedStatus(visitId: String) {
        viewModelScope.launch {
            visitCompleteStatusData.postValue(repository.isVisitCompleted(visitId))
        }
    }

    fun buildELCGGraph(
        encounters: ArrayList<EncounterDTO>,
        elcgGraph: ELCGGraph
    ) {
        viewModelScope.launch {
            val data = repository.buildELCGGraph(encounters, elcgGraph)
            elcgGraphData.postValue(data)
        }
    }
}