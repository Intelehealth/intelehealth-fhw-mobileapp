package org.intelehealth.videolibrary.listing.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import org.intelehealth.videolibrary.player.activity.VideoPlayerActivity
import org.intelehealth.videolibrary.restapi.RetrofitProvider
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.room.VideoLibraryDatabase
import org.intelehealth.videolibrary.room.dao.LibraryDao

class YoutubeListingActivity : AppCompatActivity(), VideoClickedListener {

    private var binding: ActivityYoutubeListingBinding? = null
    private var preferenceHelper: PreferenceHelper? = null
    private var viewmodel: YoutubeListingViewModel? = null

    private var authKey: String? = null
    private var packageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeListingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setToolbar()
        initializeData()
        setObservers()
        setVideoLibraryRecyclerView()
    }

    private fun setObservers() {
        viewmodel?.fetchVideosFromDb()?.observe(this) {
            val adapter = YoutubeListingAdapter(it, lifecycle, this@YoutubeListingActivity)

            binding?.recyclerview?.apply {
                this.adapter = adapter
                this.layoutManager = LinearLayoutManager(this@YoutubeListingActivity)
            }
            binding?.progressBar?.visibility = View.GONE
        }

        viewmodel?.tokenExpiredObserver?.observe(this) {

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
            owner = this@YoutubeListingActivity,
            factory = LibraryViewModelFactory(
                service = service,
                dao = dao
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

    private fun setVideoLibraryRecyclerView() {
        binding?.progressBar?.visibility = View.VISIBLE
        viewmodel?.fetchVideos(packageName!!, authKey!!)
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