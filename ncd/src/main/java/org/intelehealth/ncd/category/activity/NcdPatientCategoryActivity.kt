package org.intelehealth.ncd.category.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import org.intelehealth.ncd.R
import org.intelehealth.ncd.category.pager.CategoryPagerAdapter
import org.intelehealth.ncd.category.tabs.AnemiaFollowUpFragment
import org.intelehealth.ncd.category.tabs.AnemiaScreeningFragment
import org.intelehealth.ncd.category.tabs.DiabetesFollowUpFragment
import org.intelehealth.ncd.category.tabs.DiabetesScreeningFragment
import org.intelehealth.ncd.category.tabs.GeneralFragment
import org.intelehealth.ncd.category.tabs.HypertensionFollowUpFragment
import org.intelehealth.ncd.category.tabs.HypertensionScreeningFragment
import org.intelehealth.ncd.category.viewmodel.CommonSearchViewModel
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.databinding.ActivityNcdPatientCategoryBinding

class NcdPatientCategoryActivity : AppCompatActivity() {

    private var binding: ActivityNcdPatientCategoryBinding? = null
    private lateinit var adapter: CategoryPagerAdapter
    private var isPrivacyNotice: Boolean = false
    val searchViewModel: CommonSearchViewModel by viewModels()

    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPatientCategoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fetchData()
        setListeners()
        setViewPager()
        setupSearchListener()
    }

    private fun fetchData() {
        isPrivacyNotice = intent.getBooleanExtra(Constants.IS_PRIVACY_NOTICE, false)
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(backPressedCallback)

        binding?.backbtn?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setViewPager() {
        val fragmentList: List<Fragment> = listOf(
            AnemiaScreeningFragment(),
            AnemiaFollowUpFragment(),
            //DiabetesScreeningFragment(),
            //DiabetesFollowUpFragment(),
            HypertensionScreeningFragment(),
            HypertensionFollowUpFragment(),
            GeneralFragment()
        )

        adapter = CategoryPagerAdapter(this, fragmentList)
       binding?.vpCategory?.adapter = adapter
        binding?.vpCategory?.apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            isUserInputEnabled = true
        }




        /*val tabTitles = listOf(
            getString(R.string.tab_anemia_screening),
            getString(R.string.tab_anemia_follow_up),
            getString(R.string.tab_diabetes_screening),
            getString(R.string.tab_diabetes_follow_up),
            getString(R.string.tab_hypertension_screening),
            getString(R.string.tab_hypertension_follow_up),
            getString(R.string.tab_general)
        )*/
        val tabTitles = listOf(
            getString(R.string.tab_anemia_screening),
            getString(R.string.tab_anemia_follow_up),
            getString(R.string.tab_hypertension_screening),
            getString(R.string.tab_hypertension_follow_up),
            getString(R.string.tab_all)
        )

        TabLayoutMediator(binding?.tlCategory!!, binding?.vpCategory!!) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setupSearchListener() {
        val editText = binding?.searchTxtEnter ?: return
        val ivSearch = binding?.iconSearch ?: return
        val ivClear = binding?.iconClear ?: return

        // Initial visibility
        ivSearch.visibility = View.VISIBLE
        ivClear.visibility = View.GONE

        // Listen to text changes
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.isNullOrEmpty()

                ivSearch.visibility = if (hasText) View.GONE else View.VISIBLE
                ivClear.visibility = if (hasText) View.VISIBLE else View.GONE

                // Emit search text
                searchViewModel.updateSearchTextNew(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Clear button click
        ivClear.setOnClickListener {
            if (editText.text?.isNotEmpty() == true) {
                editText.text?.clear()
                // No need to manually toggle icons here – text watcher handles it
            }
        }
    }


}