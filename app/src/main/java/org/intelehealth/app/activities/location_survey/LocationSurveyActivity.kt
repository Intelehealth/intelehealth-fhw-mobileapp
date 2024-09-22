package org.intelehealth.app.activities.location_survey

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityLocationSurveyBinding

class LocationSurveyActivity : AppCompatActivity() {

    private var binding: ActivityLocationSurveyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationSurveyBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}