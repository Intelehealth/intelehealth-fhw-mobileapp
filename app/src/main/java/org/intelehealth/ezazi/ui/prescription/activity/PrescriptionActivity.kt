package org.intelehealth.ezazi.ui.prescription.activity

import android.os.Bundle
import org.intelehealth.ezazi.databinding.ActivityPrescriptionEzaziBinding
import org.intelehealth.ezazi.ui.shared.BaseActivity

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:26.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionActivity : BaseActivity() {
    private lateinit var binding: ActivityPrescriptionEzaziBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionEzaziBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}