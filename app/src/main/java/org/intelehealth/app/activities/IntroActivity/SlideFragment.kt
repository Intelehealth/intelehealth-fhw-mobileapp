package org.intelehealth.app.activities.IntroActivity

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.intelehealth.app.R

/**
 * Created by Tanvir Hasan on 25-03-24
 * Email: mhasan@intelehealth.org
 */
private const val ARG_PARAM1 = "param1"

enum class ViewType{
    ONE,TWO,THREE
}
class SlideFragment : Fragment() {
    private var viewType: ViewType = ViewType.ONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PARAM1,ViewType::class.java)?:ViewType.ONE
            }else{
                (it.getSerializable(ARG_PARAM1) as ViewType)?:ViewType.ONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_slide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayout: LinearLayout = view.findViewById(R.id.view_container_lay)

        when(viewType){
            ViewType.ONE->{
                linearLayout.addView(LayoutInflater.from(activity).inflate(R.layout.layout_first_intro_screen_ui2, linearLayout, false))
            }

            ViewType.TWO->{
                linearLayout.addView(LayoutInflater.from(activity).inflate(R.layout.layout_second_intro_screen_ui2, linearLayout, false))
            }

            else -> {
                linearLayout.addView(LayoutInflater.from(activity).inflate(R.layout.layout_third_intro_screen_ui2, linearLayout, false))

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(introType: ViewType) =
            SlideFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, introType)
                }
            }
    }
}