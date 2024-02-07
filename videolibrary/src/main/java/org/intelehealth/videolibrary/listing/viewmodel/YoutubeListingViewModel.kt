package org.intelehealth.videolibrary.listing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.intelehealth.videolibrary.listing.data.ListingDataSource
import org.intelehealth.videolibrary.listing.data.ListingRepository
import org.intelehealth.videolibrary.restapi.RetrofitProvider
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.restapi.response.Video
import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse

class YoutubeListingViewModel : ViewModel() {

    private var repository: ListingRepository

    private var _response: MutableLiveData<VideoLibraryResponse> = MutableLiveData()
    var response: LiveData<VideoLibraryResponse> = _response

    init {
        val service: VideoLibraryApiClient = RetrofitProvider.apiService
        val dataSource = ListingDataSource(service)
        repository = ListingRepository(dataSource)
    }

    fun fetchVideos(packageName: String, auth: String) {
        viewModelScope.launch {
            repository.fetchVideos(packageName, auth)
                .flowOn(Dispatchers.IO)
                .collect {
                    _response.postValue(it)
                }
        }
    }
}