package org.intelehealth.app.models

import android.content.Context
import org.intelehealth.app.R
import org.intelehealth.app.activities.IntroActivity.ViewType
import java.io.Serializable

/**
 * Created by Vaghela Mithun R. on 25-04-2024 - 11:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class IntroContent(
    val imageUrl: String? = null,
    val isResourceImage: Boolean,
    val resId: Int = 0,
    val title: String,
    val content: String
) : Serializable {
    companion object {
        @JvmStatic
        fun getContent(context: Context, viewType: ViewType) = when (viewType) {
            ViewType.ONE -> IntroContent(
                isResourceImage = true,
                resId = R.drawable.first,
                title = context.getString(R.string.who_we_are),
                content = context.getString(R.string.who_we_are_intro)
            )

            ViewType.TWO -> IntroContent(
                isResourceImage = true,
                resId = R.drawable.second,
                title = context.getString(R.string.take_patient_visits),
                content = context.getString(R.string.take_patient_visits_intro)
            )

            ViewType.THREE -> IntroContent(
                isResourceImage = true,
                resId = R.drawable.third,
                title = context.getString(R.string.provide_prescriptions),
                content = context.getString(R.string.provide_prescriptions_intro)
            )
        }
    }
}