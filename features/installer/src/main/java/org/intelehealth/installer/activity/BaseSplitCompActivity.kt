package org.intelehealth.installer.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat

/**
 * Created by Vaghela Mithun R. on 27-09-2024 - 12:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class BaseSplitCompActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

}