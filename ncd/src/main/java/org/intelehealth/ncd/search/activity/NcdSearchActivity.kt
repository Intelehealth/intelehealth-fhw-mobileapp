package org.intelehealth.ncd.search.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.ncd.R
import org.intelehealth.ncd.databinding.ActivityNcdSearchBinding

class NcdSearchActivity : AppCompatActivity() {

    private var binding: ActivityNcdSearchBinding? = null
    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdSearchBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
        setListeners()
    }

    private fun setListeners() {
        binding?.ivBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setToolbar() {

    }


}