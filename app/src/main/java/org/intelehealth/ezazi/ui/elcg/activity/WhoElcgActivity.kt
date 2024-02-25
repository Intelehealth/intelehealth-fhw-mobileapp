package org.intelehealth.ezazi.ui.elcg.activity

import android.os.Bundle
import android.util.Log
import androidx.core.util.Pair
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.ActivityElcgViewBinding
import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.ui.elcg.adapter.ELCGTabPagerAdapter
import org.intelehealth.ezazi.ui.elcg.data.ELCGDataSource
import org.intelehealth.ezazi.ui.elcg.data.ELCGRepository
import org.intelehealth.ezazi.ui.elcg.viewmodel.ELCGViewModel
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity

/**
 * Created by Vaghela Mithun R. on 23-11-2023 - 19:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class WhoElcgActivity : BaseActionBarActivity() {
    private var binding: ActivityElcgViewBinding? = null
    private var avatarAnimateStartPointY = 0f
    private var avatarCollapseAnimationChangeWeight = 0f
    private var isCalculated = false
    private var verticalToolbarAvatarMargin = 0f
    private var EXPAND_AVATAR_SIZE = 0f
    private var COLLAPSE_IMAGE_SIZE = 0f
    private var horizontalToolbarAvatarMargin = 0f
    private var cashCollapseState: Pair<Int, Int?>? = null
    var encounters: ArrayList<EncounterDTO>? = null

    private val viewModel: ELCGViewModel by lazy {
        ELCGViewModel(ELCGRepository(ELCGDataSource()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityElcgViewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        super.onCreate(savedInstanceState)
        setupActionBar()
        EXPAND_AVATAR_SIZE = resources.getDimension(R.dimen.patient_avatar_default_expand_size)
        COLLAPSE_IMAGE_SIZE = resources.getDimension(R.dimen.patient_avatar_default_collapsed_size)
        horizontalToolbarAvatarMargin = resources.getDimension(R.dimen.activity_horizontal_margin)

        viewModel.loadELCGData("ee56f134-7315-4baa-8851-5e3b809f060c")
        viewModel.elcgEncounterData.observe(this) {
            encounters = it
            setupTabs()
        }
    }

    private fun setupTabs() {
        Log.e(TAG, "setupTabs: ")
        val adapter = ELCGTabPagerAdapter(supportFragmentManager, lifecycle)
        binding!!.viewPagerLcg.adapter = adapter
        Log.e(TAG, "setupTabs: " + adapter.itemCount)
        TabLayoutMediator(
            binding!!.tabLayoutLcg,
            binding!!.viewPagerLcg
        ) { tab: TabLayout.Tab, position: Int -> tab.text = adapter.getTitle(position) }.attach()
    }

//    private fun setAppbarOffsetListener() {
//        binding!!.appBar.addOnOffsetChangedListener { appBarLayout: AppBarLayout, verticalOffset: Int ->
//            Log.e(TAG, "setAppbarOffsetListener: verticalOffset::$verticalOffset")
//            Log.e(TAG, "setAppbarOffsetListener: appBarLayout.getHeight()::" + appBarLayout.height)
//            Log.e(
//                TAG,
//                "setAppbarOffsetListener: binding.toolbar.getHeight()::" + binding!!.toolbar.height
//            )
//            Log.e(
//                TAG,
//                "setAppbarOffsetListener: appBarLayout.getTotalScrollRange()::" + appBarLayout.totalScrollRange
//            )
//            if (!isCalculated) {
//                avatarAnimateStartPointY =
//                    Math.abs((appBarLayout.height - (EXPAND_AVATAR_SIZE + horizontalToolbarAvatarMargin)) / appBarLayout.totalScrollRange)
//                avatarCollapseAnimationChangeWeight = 1 / (1 - avatarAnimateStartPointY)
//                verticalToolbarAvatarMargin = (binding!!.toolbar.height - COLLAPSE_IMAGE_SIZE) * 2
//                isCalculated = true
//                Log.e(
//                    TAG,
//                    "setAppbarOffsetListener: avatarAnimateStartPointY::$avatarAnimateStartPointY"
//                )
//                Log.e(
//                    TAG,
//                    "setAppbarOffsetListener: avatarCollapseAnimationChangeWeight::$avatarCollapseAnimationChangeWeight"
//                )
//                Log.e(
//                    TAG,
//                    "setAppbarOffsetListener: verticalToolbarAvatarMargin::$verticalToolbarAvatarMargin"
//                )
//            }
//            updateViews(Math.abs(verticalOffset / appBarLayout.totalScrollRange.toFloat()))
//        }
//    }

//    private fun updateViews(offset: Float) {
//        /* apply levels changes*/
//        if (offset >= 0.15f && offset <= 1) {
////            titleToolbarText.apply {
////                if (visibility != View.VISIBLE) visibility = View.VISIBLE
////                alpha = (1 - offset) * 0.35F
////            }
//        } else if (offset >= 0 && offset <= 0.15f) {
////            titleToolbarText.alpha = (1f)
////            ivUserAvatar.alpha = 1f
//        }
//        Log.e(TAG, "updateViews: offset::$offset")
//        val tempState: Pair<Int, Int>
//        tempState = if (offset < SWITCH_BOUND) {
//            Pair(
//                TO_EXPANDED,
//                if (cashCollapseState == null) WAIT_FOR_SWITCH else if (cashCollapseState!!.second == null) WAIT_FOR_SWITCH else cashCollapseState!!.second
//            )
//        } else Pair(
//            TO_COLLAPSED,
//            if (cashCollapseState == null) WAIT_FOR_SWITCH else if (cashCollapseState!!.second == null) WAIT_FOR_SWITCH else cashCollapseState!!.second
//        )
//        if (cashCollapseState !== tempState) {
//            if (tempState.first == TO_EXPANDED) {
//                binding!!.tvPatientAvatar.translationX = 0f
//            } else if (tempState.first == TO_COLLAPSED) {
//                //TODO add something here
//            }
//            cashCollapseState = Pair(tempState.first, SWITCHED)
//        } else cashCollapseState = Pair(tempState.first, WAIT_FOR_SWITCH)
//        if (offset > avatarAnimateStartPointY) {
//            val avatarCollapseAnimateOffset =
//                (offset - avatarAnimateStartPointY) * avatarCollapseAnimationChangeWeight
//            val avatarSize =
//                EXPAND_AVATAR_SIZE - (EXPAND_AVATAR_SIZE - COLLAPSE_IMAGE_SIZE) * avatarCollapseAnimateOffset
//            Log.e(TAG, "updateViews: avatarCollapseAnimateOffset::$avatarCollapseAnimateOffset")
//            Log.e(TAG, "updateViews: avatarSize::$avatarSize")
//            binding!!.tvPatientAvatar.layoutParams.height = Math.round(avatarSize)
//            binding!!.tvPatientAvatar.layoutParams.width = Math.round(avatarSize)
//            binding!!.tvPatientAvatar.translationX =
//                (binding!!.appBar.width - horizontalToolbarAvatarMargin - avatarSize) / 2 * avatarCollapseAnimateOffset
//            binding!!.tvPatientAvatar.translationX =
//                (binding!!.toolbar.height - verticalToolbarAvatarMargin - avatarSize) / 2 * avatarCollapseAnimateOffset
//        } else {
//            if (binding!!.tvPatientAvatar.layoutParams.height.toFloat() != EXPAND_AVATAR_SIZE) {
//                binding!!.tvPatientAvatar.layoutParams.height = EXPAND_AVATAR_SIZE.toInt()
//                binding!!.tvPatientAvatar.layoutParams.width = EXPAND_AVATAR_SIZE.toInt()
//                binding!!.tvPatientAvatar.translationX = 0f
//            }
//        }
//    }

    override fun getScreenTitle(): Int {
        return 0
    }

    companion object {
        private const val TAG = "WHOLCGDataActivity"
        private const val SWITCH_BOUND = 0.8f
        private const val TO_EXPANDED = 0
        private const val TO_COLLAPSED = 1
        private const val WAIT_FOR_SWITCH = 0
        private const val SWITCHED = 1
    }
}