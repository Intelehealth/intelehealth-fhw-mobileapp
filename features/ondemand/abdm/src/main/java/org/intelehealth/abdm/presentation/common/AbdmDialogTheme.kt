package org.intelehealth.abdm.presentation.common

import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import org.intelehealth.abdm.R

/**
 * Inflater bound to the module's own dialog theme.
 *
 * Views built inside [androidx.fragment.app.DialogFragment.onCreateDialog] must not use the
 * fragment's `layoutInflater`: the dialog does not exist yet at that point, so DialogFragment hands
 * back the *host activity's* inflater. Every Material component in this module requires a
 * `Theme.MaterialComponents` descendant, and a host is under no obligation to use one — for example
 * Android-Mobile-Client's `AppTheme` is `Theme.AppCompat.Light.DarkActionBar` — so inflating against
 * the host theme fails with `IllegalArgumentException` from Material's `ThemeEnforcement`.
 *
 * Dialogs that inflate in `onCreateView` do not need this; overriding `getTheme()` is enough there,
 * because the dialog is already built and its inflater is cloned into the themed context.
 */
internal fun Fragment.abdmDialogInflater(): LayoutInflater =
    LayoutInflater.from(ContextThemeWrapper(requireContext(), R.style.Theme_Abdm_Dialog))
