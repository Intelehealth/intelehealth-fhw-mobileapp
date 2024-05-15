package org.intelehealth.ncd.search.activity

import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.ncd.R
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.search.SearchDataSource
import org.intelehealth.ncd.data.search.SearchRepository
import org.intelehealth.ncd.databinding.ActivityNcdSearchBinding
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.search.viewmodel.SearchViewModel
import org.intelehealth.ncd.search.viewmodel.factory.SearchViewModelFactory
import org.intelehealth.ncd.utils.CategorySegregationUtils

class NcdSearchActivity : AppCompatActivity() {

    private var binding: ActivityNcdSearchBinding? = null
    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    private var viewModel: SearchViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdSearchBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
        setListeners()
        initializeData()
    }

    private fun setToolbar() {
        binding?.toolbar?.apply {
            setSupportActionBar(this)
            setTitleTextAppearance(this@NcdSearchActivity, R.style.ToolbarTheme)
            setTitleTextColor(Color.WHITE)
        }
    }

    private fun setListeners() {
        binding?.ivBack?.setOnClickListener {
            onBackPressedDispatcher.addCallback(backPressedCallback)
        }

        binding?.tilSearch?.setEndIconOnClickListener {
            binding?.etSearchText?.text?.clear()
        }

        binding?.etSearchText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText: String? = binding?.etSearchText?.text?.toString()
                if (!searchText.isNullOrEmpty()) {
                    performSearch(searchText)
                }
            }
            true
        }
    }

    private fun initializeData() {
        val patientDao: PatientDao =
            CategoryDatabase.getInstance(this@NcdSearchActivity).patientDao()

        val dataSource = SearchDataSource(patientDao)
        val repository = SearchRepository(dataSource)
        val utils = CategorySegregationUtils(resources)

        viewModel = ViewModelProvider(
            owner = this@NcdSearchActivity,
            factory = SearchViewModelFactory(repository, utils)
        )[SearchViewModel::class.java]
    }

    private fun performSearch(searchString: String) {
        viewModel?.queryPatientWithAttributesAndSearchString(
            Constants.OTHER_MEDICAL_HISTORY,
            searchString
        )
    }
}