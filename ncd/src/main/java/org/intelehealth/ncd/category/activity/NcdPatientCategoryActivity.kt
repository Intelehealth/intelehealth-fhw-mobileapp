package org.intelehealth.ncd.category.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.databinding.ActivityNcdPatientCategoryBinding
import org.intelehealth.ncd.search.activity.NcdSearchActivity

class NcdPatientCategoryActivity : AppCompatActivity() {

    private var binding: ActivityNcdPatientCategoryBinding? = null
    private var backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    private var isPrivacyNotice: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPatientCategoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fetchData()
        setToolbar()
        setListeners()
        setViewPager()
    }

    private fun fetchData() {
        isPrivacyNotice = intent.getBooleanExtra(Constants.IS_PRIVACY_NOTICE, false)
    }

    private fun setToolbar() {
        binding?.toolbar?.apply {
            setSupportActionBar(this)
            setTitleTextAppearance(this@NcdPatientCategoryActivity, R.style.ToolbarTheme)
            setTitleTextColor(Color.WHITE)
        }
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(backPressedCallback)

        binding?.ivBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding?.toolbarSearch?.setOnClickListener {
            val intent = Intent(this@NcdPatientCategoryActivity, NcdSearchActivity::class.java)
            intent.putExtra(Constants.IS_PRIVACY_NOTICE, isPrivacyNotice)
            startActivity(intent)
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