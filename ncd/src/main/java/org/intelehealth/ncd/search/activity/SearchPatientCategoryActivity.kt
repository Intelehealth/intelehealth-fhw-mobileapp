package org.intelehealth.ncd.search.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.intelehealth.ncd.R
import org.intelehealth.ncd.databinding.ActivitySearchPatientCategoryBinding

class SearchPatientCategoryActivity : AppCompatActivity() {

    private var binding: ActivitySearchPatientCategoryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchPatientCategoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
    }

    private fun setToolbar() {
        binding?.toolbar?.apply {
            setSupportActionBar(this)
            setTitleTextAppearance(this@SearchPatientCategoryActivity, R.style.ToolbarTheme)
            setTitleTextColor(Color.WHITE)
        }
    }
}