package org.intelehealth.ncd.search.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import org.intelehealth.ncd.R
import org.intelehealth.ncd.databinding.ActivitySearchPatientCategoryBinding
import org.intelehealth.ncd.search.pager.CategoryPagerAdapter
import org.intelehealth.ncd.search.tabs.AnemiaFollowUpFragment
import org.intelehealth.ncd.search.tabs.AnemiaScreeningFragment
import org.intelehealth.ncd.search.tabs.DiabetesFollowUpFragment
import org.intelehealth.ncd.search.tabs.DiabetesScreeningFragment
import org.intelehealth.ncd.search.tabs.GeneralFragment
import org.intelehealth.ncd.search.tabs.HypertensionFollowUpFragment
import org.intelehealth.ncd.search.tabs.HypertensionScreeningFragment

class SearchPatientCategoryActivity : AppCompatActivity() {

    private var binding: ActivitySearchPatientCategoryBinding? = null
    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchPatientCategoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
        setListeners()
        setViewPager()
    }

    private fun setToolbar() {
        binding?.toolbar?.apply {
            setSupportActionBar(this)
            setTitleTextAppearance(this@SearchPatientCategoryActivity, R.style.ToolbarTheme)
            setTitleTextColor(Color.WHITE)
        }
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(backPressedCallback)

        binding?.ivBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setViewPager() {
        val fragmentList: List<Fragment> = listOf(
            AnemiaScreeningFragment(),
            AnemiaFollowUpFragment(),
            DiabetesScreeningFragment(),
            DiabetesFollowUpFragment(),
            HypertensionScreeningFragment(),
            HypertensionFollowUpFragment(),
            GeneralFragment()
        )

        val adapter = CategoryPagerAdapter(this, fragmentList)
        binding?.vpCategory?.adapter = adapter

        val tabTitles = listOf(
            getString(R.string.tab_anemia_screening),
            getString(R.string.tab_anemia_follow_up),
            getString(R.string.tab_diabetes_screening),
            getString(R.string.tab_diabetes_follow_up),
            getString(R.string.tab_hypertension_screening),
            getString(R.string.tab_hypertension_follow_up),
            getString(R.string.tab_general)
        )

        TabLayoutMediator(binding?.tlCategory!!, binding?.vpCategory!!) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}