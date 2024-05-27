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
                resId = R.drawable.intro_image_1,
                title = context.getString(R.string.intro_title_1),
                content = context.getString(R.string.intro_tagline_1)
            )

            ViewType.TWO -> IntroContent(
                isResourceImage = true,
                resId = R.drawable.intro_image_2,
                title = context.getString(R.string.intro_title_2),
                content = context.getString(R.string.intro_tagline_2)
            )

            ViewType.THREE -> IntroContent(
                isResourceImage = true,
                resId = R.drawable.intro_image_3,
                title = context.getString(R.string.intro_title_3),
                content = context.getString(R.string.intro_tagline_3)
            )
        }
    }
}