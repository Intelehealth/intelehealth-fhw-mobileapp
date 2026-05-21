package org.intelehealth.app.ui.patient.activity.filter

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.activities.filterPatientActivity.FilterResultAdapter
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.DialogUtils

class MatchResultActivity : BaseActivity(), FilterResultAdapter.AdapterClickListener{
    private var patientDTO: PatientDTO? = null

    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var loadingDialog: AlertDialog
    private lateinit var filterSuccessLayout: LinearLayout
    private var patientList = mutableListOf<PatientSearchResult>()
    private var firstName=""
    private var lastName=""
    private var phone=""
    private var dob=""
    private var gender=""
    private lateinit var patientAdapter: FilterResultAdapter
   // private val patientAdapter = FilterResultAdapter(patientList, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_result)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        patientDTO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "patientDTO",
                PatientDTO::class.java
            )
        } else {
            intent.getSerializableExtra("patientDTO") as? PatientDTO
        }
        patientList =
            intent.getSerializableExtra(
                "finalPatientList"
            ) as ArrayList<PatientSearchResult>

       patientAdapter =
           FilterResultAdapter(patientList, this)

       filterRecyclerView = findViewById(R.id.filter_patient_container)

       filterRecyclerView.layoutManager =
           LinearLayoutManager(this@MatchResultActivity)

       filterRecyclerView.adapter = patientAdapter

        filterSuccessLayout = findViewById(R.id.filter_patient_success_ll)

       patientAdapter.notifyDataSetChanged()
       filterSuccessLayout =
           findViewById(R.id.filter_patient_success_ll)

       // changing status bar color
       window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
       window.statusBarColor = Color.WHITE
        loadingDialog = DialogUtils().showCommonLoadingDialog(
            this@MatchResultActivity,
            getString(R.string.loading),
            getString(R.string.please_wait),
        ).apply {
            dismiss()
        }

        findViewById<ImageView>(R.id.iv_back_arrow)?.setOnClickListener {
            finish()
        }
    }

    override fun onItemClick(selectedItem: Any) {
        TODO("Not yet implemented")
    }
}