package org.intelehealth.fcm.model

import android.content.Intent
import androidx.annotation.DrawableRes


/**
 * Created by Vaghela Mithun R. on 03-02-2023 - 19:11.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
data class NotificationBuilderParam(
    val intent: Intent,
    val title: String,
    val message: String,
    val action: String = "View",
    @DrawableRes val icon: Int
)
