package org.intelehealth.app.activities.IntroActivity

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentSlideBinding
import org.intelehealth.app.models.IntroContent

/**
 * Created by Tanvir Hasan on 25-03-24
 * Email: mhasan@intelehealth.org
 */
private const val ARG_PARAM1 = "param1"

enum class ViewType {
    ONE, TWO, THREE
}

class SlideFragment : Fragment(R.layout.fragment_slide) {
    private var viewType: ViewType = ViewType.ONE
    private lateinit var binding: FragmentSlideBinding

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            viewType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                it.getSerializable(ARG_PARAM1, ViewType::class.java) ?: ViewType.ONE
//            } else {
//                (it.getSerializable(ARG_PARAM1) as ViewType) ?: ViewType.ONE
//            }
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSlideBinding.bind(view)
        arguments?.let {
            val content = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PARAM1, IntroContent::class.java)
            } else {
                (it.getSerializable(ARG_PARAM1) as IntroContent)
            }

            binding.content = content
        }
//        val linearLayout: LinearLayout = view.findViewById(R.id.view_container_lay)
//
//        when (viewType) {
//            ViewType.ONE -> {
//                linearLayout.addView(
//                    LayoutInflater.from(activity)
//                        .inflate(R.layout.layout_first_intro_screen_ui2, linearLayout, false)
//                )
//            }
//
//            ViewType.TWO -> {
//                linearLayout.addView(
//                    LayoutInflater.from(activity)
//                        .inflate(R.layout.layout_second_intro_screen_ui2, linearLayout, false)
//                )
//            }
//
//            else -> {
//                linearLayout.addView(
//                    LayoutInflater.from(activity)
//                        .inflate(R.layout.layout_third_intro_screen_ui2, linearLayout, false)
//                )
//
//            }
//        }
    }

    companion object {
        @JvmStatic
        fun newInstance(introContent: IntroContent) =
            SlideFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, introContent)
                }
            }
    }
}