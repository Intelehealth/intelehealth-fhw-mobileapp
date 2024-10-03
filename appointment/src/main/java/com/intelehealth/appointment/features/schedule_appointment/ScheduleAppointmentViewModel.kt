package com.intelehealth.appointment.features.schedule_appointment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intelehealth.appointment.data.provider.WebClientProvider.getApiClient
import com.intelehealth.appointment.data.remote.response.SlotInfo
import com.intelehealth.appointment.data.remote.response.SlotInfoResponse
import com.intelehealth.appointment.data.repository.ScheduleAppointmentRepo
import com.intelehealth.appointment.utils.CommonKeys
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date


class ScheduleAppointmentViewModel : ViewModel() {
    private var scheduleAppointmentRepo = ScheduleAppointmentRepo(getApiClient())
    var appStartDate: String? = null
    var appStartTime: String? = null
    private val _isSlotNotAvailable = MutableLiveData<Boolean>()
    val isSlotNotAvailable: LiveData<Boolean> get() = _isSlotNotAvailable
    var mutableSlotList = MutableLiveData<Map<String,List<SlotInfo>>>()
    fun sync() {

    }
    fun getSlots(startDate: String, endDate: String, speciality: String,isRescheduled:Boolean) {
        viewModelScope.launch {
            scheduleAppointmentRepo.getSlots(startDate,endDate,speciality)
                .collect{ result->
                    if(result.isSuccess){
                        result.getOrNull()?.let { slotInfoResponse ->
                            val slotInfoList = slotInfoResponse.dates ?: emptyList()

                            val slotInfoMorningList = mutableListOf<SlotInfo>()
                            val slotInfoAfternoonList = mutableListOf<SlotInfo>()
                            val slotInfoEveningList = mutableListOf<SlotInfo>()

                            if (isRescheduled) {
                                removePreviousAppointmentDateTime(slotInfoResponse)
                            }

                            // Morning slot filter
                            slotInfoList.forEach { slotInfo ->
                                val splitedTime = slotInfo.slotTime?.split(" ")
                                if (splitedTime?.getOrNull(1)?.trim() == "AM") {
                                    slotInfoMorningList.add(slotInfo)
                                }
                            }

                            // Afternoon and evening slot filter
                            slotInfoList.forEach { slotInfo ->
                                val splitedTime = slotInfo.slotTime?.split(" ")
                                val appointmentTime: Double
                                if (splitedTime?.getOrNull(1)?.trim() == "PM") {
                                    val timeStr = splitedTime[0].replace(":", ".")
                                    appointmentTime = timeStr.toDoubleOrNull() ?: 0.0
                                    if ((appointmentTime in 1.0..6.0) || appointmentTime >= 12) {
                                        slotInfoAfternoonList.add(slotInfo)
                                    } else {
                                        slotInfoEveningList.add(slotInfo)
                                    }
                                }
                            }

                            // Sort the lists
                            sortByTime(slotInfoMorningList)
                            sortByTime(slotInfoAfternoonList)
                            sortByTime(slotInfoEveningList)

                            val map = HashMap<String,List<SlotInfo>>()
                            map[CommonKeys.MORNING] = slotInfoMorningList
                            map[CommonKeys.AFTERNOON] = slotInfoAfternoonList
                            map[CommonKeys.EVENING] = slotInfoEveningList
                            mutableSlotList.value = map.toMap()
                        }
                    }
                    else{
                        Log.d("EEEEE",result.toString())
                    }
                }
        }
    }

    private fun removePreviousAppointmentDateTime(slotInfoResponse: SlotInfoResponse) {
        val slots: MutableList<SlotInfo> =
            slotInfoResponse.dates
        slots.removeIf { slotInfo: SlotInfo ->
            slotInfo.slotDate
                .equals(appStartDate, ignoreCase = true) && slotInfo.slotTime
                .equals(appStartTime, ignoreCase = true)
        }
        slotInfoResponse.dates = slots
    }

    private fun sortByTime(slotInfoList: List<SlotInfo>) {
        Collections.sort(
            slotInfoList
        ) { t1, t2 ->
            val simpleDateFormat = SimpleDateFormat("hh:mm a")
            var x: Date? = null
            var y: Date? = null
            try {
                x = simpleDateFormat.parse(t1.getSlotTime())
                y = simpleDateFormat.parse(t2.getSlotTime())
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            x!!.compareTo(y)
        }
    }


}