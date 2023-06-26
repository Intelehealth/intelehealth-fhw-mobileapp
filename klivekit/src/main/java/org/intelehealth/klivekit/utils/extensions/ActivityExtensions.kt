package org.intelehealth.klivekit.utils.extensions

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 15:00.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun AppCompatActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}