package org.intelehealth.videolibrary.listing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intelehealth.videolibrary.listing.data.ListingDataSource
import org.intelehealth.videolibrary.listing.data.ListingRepository
import org.intelehealth.videolibrary.model.Video
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse
import org.intelehealth.videolibrary.room.dao.LibraryDao
import org.intelehealth.videolibrary.utils.ResponseChecker
import retrofit2.Response

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

class YoutubeListingViewModel(service: VideoLibraryApiClient, dao: LibraryDao) : ViewModel() {

    private var repository: ListingRepository

    private var _tokenExpiredObserver: MutableLiveData<Boolean> = MutableLiveData(false)
    var tokenExpiredObserver: LiveData<Boolean> = _tokenExpiredObserver

    private var _emptyListObserver: MutableLiveData<Boolean> = MutableLiveData(false)
    var emptyListObserver: LiveData<Boolean> = _emptyListObserver

    init {
        val dataSource = ListingDataSource(service, dao)
        repository = ListingRepository(dataSource)
    }

    fun fetchVideosFromServer(packageName: String, auth: String) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(5000)
            repository.fetchVideosFromServer(packageName, auth)
                .collect { response ->
                    handleResponses(response)
                }
        }
    }

    private fun handleResponses(response: Response<VideoLibraryResponse?>) {
        val responseChecker = ResponseChecker(response)
        if (responseChecker.isNotAuthorized) {
            _tokenExpiredObserver.postValue(true)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                response.body()?.projectLibraryData?.videos?.let {
                    _emptyListObserver.postValue(it.isEmpty())
                    repository.insertVideos(it)
                }
            }
        }
    }

    fun fetchVideosFromDb() = repository.fetchVideosFromDb().asLiveData()

    fun areListsSame(list1: List<Video>?, list2: List<Video>?) = list1 == list2

    fun deleteAllVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}