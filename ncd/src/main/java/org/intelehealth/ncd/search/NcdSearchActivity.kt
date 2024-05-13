package org.intelehealth.ncd.search

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.ncd.R
import org.intelehealth.ncd.databinding.ActivityNcdSearchBinding

class NcdSearchActivity : AppCompatActivity() {

    private var binding: ActivityNcdSearchBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdSearchBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
    }

    private fun setToolbar() {

    }
}