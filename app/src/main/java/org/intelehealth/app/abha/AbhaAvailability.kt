package org.intelehealth.app.abha

import android.content.Context
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.intelehealth.abdm.api.AbhaFeature
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.config.room.entity.FeatureActiveStatus
import timber.log.Timber
import java.util.Optional

/**
 * The single place the host decides whether ABHA is available. Two independent switches:
 *
 * 1. Build time: the client flavor ships :features:ondemand:abdm (see `abhaClients` in
 *    app/build.gradle). When it does not, Hilt has no [AbhaFeature] binding and [feature] is null.
 * 2. Run time: the `abha_section` flag in the published config. It defaults to on, matching the
 *    config entity, so a device that has not synced config yet behaves as before.
 *
 * Call sites use [feature] when they only need the module (e.g. viewing an already-downloaded card),
 * and [enabledFeature] when they are starting a new ABHA interaction.
 */
object AbhaAvailability {

    /** The ABHA implementation, or null when this build was made without it. */
    @JvmStatic
    fun feature(context: Context): AbhaFeature? =
        EntryPointAccessors.fromApplication(context.applicationContext, AbhaEntryPoint::class.java)
            .abhaFeature()
            .orElse(null)

    /** The ABHA implementation when it is both shipped and switched on in config; otherwise null. */
    @JvmStatic
    fun enabledFeature(context: Context, status: FeatureActiveStatus?): AbhaFeature? =
        feature(context)?.takeIf { status?.activeStatusAbha ?: true }

    /** As [enabledFeature], reading the flag from the config database. Call off the main thread. */
    suspend fun enabledFeature(context: Context): AbhaFeature? {
        val feature = feature(context) ?: return null
        val status = runCatching {
            ConfigDatabase.getInstance(context.applicationContext)
                .featureActiveStatusDao().getAllRecord().firstOrNull()
        }.onFailure { Timber.w(it, "Could not read abha_section; treating ABHA as enabled") }
            .getOrNull()
        return feature.takeIf { status?.activeStatusAbha ?: true }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AbhaEntryPoint {
    fun abhaFeature(): Optional<AbhaFeature>
}

/** Declares AbhaFeature as optional, so the graph builds when the abdm module is not on the classpath. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AbhaOptionalModule {
    @BindsOptionalOf
    abstract fun abhaFeature(): AbhaFeature
}
