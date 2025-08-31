package org.intelehealth.app.activities.user

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.intelehealth.app.user.UserSessionRepository


class SessionTimerViewModel(
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _elapsedSeconds = MutableLiveData<Long>()
    val elapsedSeconds: LiveData<Long> = _elapsedSeconds

    private val _elapsedTimeFormatted = MutableLiveData<String>()
    val elapsedTimeFormatted: LiveData<String> = _elapsedTimeFormatted

    private var sessionStartTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            val elapsed = (System.currentTimeMillis() - sessionStartTime) / 1000
            _elapsedSeconds.postValue(elapsed)
            _elapsedTimeFormatted.postValue(formatTime(elapsed))
            handler.postDelayed(this, 1000)
        }
    }

    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        userSessionRepository.startSession()
        handler.post(updateRunnable)
    }

    fun endSession() {
        handler.removeCallbacks(updateRunnable)
        userSessionRepository.endSession()
    }

    override fun onCleared() {
        handler.removeCallbacks(updateRunnable)
        super.onCleared()
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return String.format("%02dh %02dm", hours, minutes)
    }
}

/*
class SessionTimerViewModel(
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    // This will store the formatted time directly
    private val _elapsedTime = MutableLiveData<String>()
    val elapsedTime: LiveData<String> = _elapsedTime

    private var sessionStartTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            val elapsedSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60

            // Format as 00h 00m
            val formatted = String.format("%02dh %02dm", hours, minutes)
            _elapsedTime.postValue(formatted)

            handler.postDelayed(this, 1000) // update every second
        }
    }

    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        userSessionRepository.startSession()
        handler.post(updateRunnable)
    }

    fun endSession() {
        handler.removeCallbacks(updateRunnable)
        userSessionRepository.endSession()
    }

    override fun onCleared() {
        handler.removeCallbacks(updateRunnable)
        super.onCleared()
    }
}
*/
