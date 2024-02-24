package org.intelehealth.videolibrary.listing.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.videolibrary.R
import org.intelehealth.videolibrary.callbacks.VideoClickedListener
import org.intelehealth.videolibrary.constants.Constants
import org.intelehealth.videolibrary.data.PreferenceHelper
import org.intelehealth.videolibrary.databinding.ActivityYoutubeListingBinding
import org.intelehealth.videolibrary.listing.adapter.YoutubeListingAdapter
import org.intelehealth.videolibrary.listing.viewmodel.LibraryViewModelFactory
import org.intelehealth.videolibrary.listing.viewmodel.YoutubeListingViewModel
import org.intelehealth.videolibrary.model.Video
import org.intelehealth.videolibrary.player.activity.VideoPlayerActivity
import org.intelehealth.videolibrary.restapi.RetrofitProvider
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.room.VideoLibraryDatabase
import org.intelehealth.videolibrary.room.dao.LibraryDao

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

class YoutubeListingActivity : AppCompatActivity(), VideoClickedListener {

    private var binding: ActivityYoutubeListingBinding? = null
    private var preferenceHelper: PreferenceHelper? = null
    private var viewmodel: YoutubeListingViewModel? = null

    private var authKey: String? = null
    private var packageName: String? = null
    private var videoList: List<Video>? = null
    private var isCallToServer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeListingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
        setSwipeToRefreshLayout()
        initializeData()
        setObservers()
        setVideoLibraryRecyclerView()
    }

    private fun setSwipeToRefreshLayout() {
        binding?.swipeToRefresh?.setOnRefreshListener {
            fetchVideosFromServer()
        }
    }

    private fun setObservers() {

        // used for fetching data from the db
        viewmodel?.fetchVideosFromDb()?.observe(this) {

            // to fetch the videos from the server in case the db is empty
            if (it.isEmpty() && !isCallToServer) {
                isCallToServer = false
                binding?.progressBar?.visibility = View.VISIBLE
                fetchVideosFromServer()
                return@observe
            }

            // to check if the same videos are being set or not
            // this is to prevent flickering issue as Flows are constantly updating our listing
            if (viewmodel?.areListsSame(videoList, it) == true) {
                binding?.progressBar?.checkAndHideProgressBar()
                binding?.swipeToRefresh?.checkAndHideProgressBar()
                return@observe
            }

            // caching the list of videos fetched to maintain a record and prevent constant updates
            videoList = it
            updateRecyclerView(it)

            // Hiding the progress bars here is being handled by extension functions
            binding?.progressBar?.checkAndHideProgressBar()
            binding?.swipeToRefresh?.checkAndHideProgressBar()
        }

        // used for detecting if the JWT token is expired
        viewmodel?.tokenExpiredObserver?.observe(this) {
            if (it) {
                setResult(Constants.JWT_TOKEN_EXPIRED)
                finish()
            }
        }

        // used for handling scenarios where there are no videos from the server
        viewmodel?.emptyListObserver?.observe(this) {
            if (it) {
                Toast.makeText(
                    this@YoutubeListingActivity,
                    getString(R.string.no_videos_found_on_server),
                    Toast.LENGTH_LONG
                ).show()

                // Hiding the progress bars here is being handled by extension functions
                binding?.swipeToRefresh?.checkAndHideProgressBar()
                binding?.progressBar?.checkAndHideProgressBar()

                updateRecyclerView(emptyList())
            }
        }
    }

    private fun updateRecyclerView(it: List<Video>) {
        val adapter = YoutubeListingAdapter(it, lifecycle, this@YoutubeListingActivity)
        binding?.recyclerview?.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(this@YoutubeListingActivity)
        }
    }

    private fun initializeData() {
        preferenceHelper = PreferenceHelper(applicationContext)
        authKey = "Bearer ${preferenceHelper?.getJwtAuthToken()}"
        packageName = applicationContext.packageName

        val service: VideoLibraryApiClient = RetrofitProvider.apiService
        val dao: LibraryDao =
            VideoLibraryDatabase.getInstance(this@YoutubeListingActivity).libraryDao()

        viewmodel = ViewModelProvider(
            owner = this@YoutubeListingActivity, factory = LibraryViewModelFactory(
                service = service, dao = dao
            )
        )[YoutubeListingViewModel::class.java]
    }

    private fun setToolbar() {
        binding?.toolbar?.apply {
            setSupportActionBar(this)
            setTitleTextAppearance(this@YoutubeListingActivity, R.style.ToolbarTheme)
            setTitleTextColor(Color.WHITE)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.video_library)
        }
    }

    private fun fetchVideosFromServer() {
        isCallToServer = true
        viewmodel?.fetchVideosFromServer(packageName!!, authKey!!)
    }

    private fun setVideoLibraryRecyclerView() {
        binding?.progressBar?.visibility = View.VISIBLE
        viewmodel?.fetchVideosFromDb()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId: Int = item.itemId
        if (itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onVideoClicked(videoId: String) {
        val intent = Intent(this@YoutubeListingActivity, VideoPlayerActivity::class.java).also {
            it.putExtra(Constants.VIDEO_ID, videoId)
        }
        startActivity(intent)
    }
}