package com.intelehealth.coyamore.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext


/**
 * Created by Vaghela Mithun R. on 30-03-2023 - 18:22.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
@InstallIn(ActivityComponent::class)
@Module
class ActivityModule {
    @Provides
    fun provideActivityResultRegistry(@ActivityContext activity: Context) =
        (activity as? AppCompatActivity)?.activityResultRegistry
            ?: throw IllegalArgumentException("You must use AppCompatActivity")
}