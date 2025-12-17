package org.intelehealth.ncd.linelisting

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import org.intelehealth.ncd.R
import org.intelehealth.ncd.category.pager.CategoryPagerAdapter
import org.intelehealth.ncd.category.tabs.GeneralFragment
import org.intelehealth.ncd.category.viewmodel.CommonSearchViewModel
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.databinding.ActivityNcdPatientCategoryBinding
import org.intelehealth.ncd.linelisting.fragments.ProtocolScreenFragment

class NcdPatientCategoryActivityNew : AppCompatActivity() {

    private lateinit var binding: ActivityNcdPatientCategoryBinding
    private lateinit var adapter: CategoryPagerAdapter
    val searchViewModel: CommonSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdPatientCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackPress()
        setupViewPager()
        setupSearchListener()
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
        binding.backbtn.setOnClickListener { finish() }
    }

    private fun setupViewPager() {

        val fragmentList = listOf(
            ProtocolScreenFragment.newInstance(Constants.ANEMIA_SCREENING, age = Constants.ANEMIA_EXCLUSION_AGE),
            ProtocolScreenFragment.newInstance(Constants.ANEMIA_FOLLOW_UP, age = Constants.ANEMIA_EXCLUSION_AGE),
            ProtocolScreenFragment.newInstance(Constants.DIABETES_SCREENING, age = Constants.DIABETES_EXCLUSION_AGE_LINE_LISTING),
            ProtocolScreenFragment.newInstance(Constants.HYPERTENSION_SCREENING, age =  Constants.HYPERTENSION_EXCLUSION_AGE),
            ProtocolScreenFragment.newInstance(Constants.HYPERTENSION_FOLLOW_UP, age =  Constants.HYPERTENSION_EXCLUSION_AGE),
            GeneralFragment()
        )

        adapter = CategoryPagerAdapter(this, fragmentList)
        binding.vpCategory.adapter = adapter
        binding.vpCategory.isUserInputEnabled = true
        binding.vpCategory.overScrollMode = View.OVER_SCROLL_NEVER

        val tabTitles = listOf(
            getString(R.string.tab_anemia_screening),
            getString(R.string.tab_anemia_follow_up),
            getString(R.string.tab_diabetes_screening),
            getString(R.string.tab_hypertension_screening),
            getString(R.string.tab_hypertension_follow_up),
            getString(R.string.tab_all)
        )

        TabLayoutMediator(binding.tlCategory, binding.vpCategory) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setupSearchListener() {
        val editText = binding.searchTxtEnter
        val ivSearch = binding.iconSearch
        val ivClear = binding.iconClear

        ivSearch.visibility = View.VISIBLE
        ivClear.visibility = View.GONE

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.isNullOrEmpty()

                ivSearch.visibility = if (hasText) View.GONE else View.VISIBLE
                ivClear.visibility = if (hasText) View.VISIBLE else View.GONE

                // propagate search to ALL fragments via shared VM
                searchViewModel.updateSearchTextNew(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ivClear.setOnClickListener {
            if (editText.text?.isNotEmpty() == true) editText.text?.clear()
        }
    }
}