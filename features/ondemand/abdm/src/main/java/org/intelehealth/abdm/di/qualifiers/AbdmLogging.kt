package org.intelehealth.abdm.di.qualifiers

import javax.inject.Qualifier

/**
 * Namespaces abdm's [okhttp3.logging.HttpLoggingInterceptor] binding. Unlike the module's other
 * provisions, this one is a third-party type, so an unqualified binding collides with any host that
 * also contributes one to the singleton component.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AbdmLogging
