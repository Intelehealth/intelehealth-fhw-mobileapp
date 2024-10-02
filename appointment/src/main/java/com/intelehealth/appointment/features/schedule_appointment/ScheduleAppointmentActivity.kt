package com.intelehealth.appointment.features.schedule_appointment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.intelehealth.appointment.R
import com.intelehealth.appointment.databinding.ActivityScheduleAppointmentBinding

class ScheduleAppointmentActivity : AppCompatActivity() {
    lateinit var binding: ActivityScheduleAppointmentBinding
    private lateinit var scheduleAppointmentViewModel: ScheduleAppointmentViewModel
    private var syncAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_schedule_appointment)
        binding.toolbarScheduleAppointments.tvScreenTitleCommon.text = getString(R.string.schedule_appointment)
        scheduleAppointmentViewModel = ViewModelProvider(this)[ScheduleAppointmentViewModel::class.java]
        binding.viewModel = scheduleAppointmentViewModel
        setupAnimator()
        binding.toolbarScheduleAppointments.imageviewIsInternetCommon.setOnClickListener {
            scheduleAppointmentViewModel.sync()
        }
    }

    private fun setupAnimator() {
        syncAnimator =
            ObjectAnimator.ofFloat<View>(binding.toolbarScheduleAppointments.imageviewIsInternetCommon, View.ROTATION, 0f, 359f).setDuration(1200)
        syncAnimator!!.repeatCount = ValueAnimator.INFINITE
        syncAnimator!!.interpolator = LinearInterpolator()
    }
}