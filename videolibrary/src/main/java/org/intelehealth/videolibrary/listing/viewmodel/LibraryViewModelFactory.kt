package org.intelehealth.videolibrary.listing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.room.dao.LibraryDao

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

@Suppress("UNCHECKED_CAST")
class LibraryViewModelFactory(
    private val service: VideoLibraryApiClient,
    private val dao: LibraryDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return YoutubeListingViewModel(service, dao) as T
    }
}