package org.intelehealth.videolibrary.listing.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.videolibrary.R
import org.intelehealth.videolibrary.callbacks.VideoClickedListener
import org.intelehealth.videolibrary.constants.Constants
import org.intelehealth.videolibrary.data.PreferenceHelper
import org.intelehealth.videolibrary.databinding.ActivityYoutubeListingBinding
import org.intelehealth.videolibrary.listing.adapter.YoutubeListingAdapter
import org.intelehealth.videolibrary.listing.viewmodel.YoutubeListingViewModel
import org.intelehealth.videolibrary.player.activity.VideoPlayerActivity

class YoutubeListingActivity : AppCompatActivity(), VideoClickedListener {

    private var binding: ActivityYoutubeListingBinding? = null
    private var preferenceHelper: PreferenceHelper? = null
    private val viewmodel: YoutubeListingViewModel by viewModels()

    private var authKey: String? = null
    private var packageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeListingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setData()
        setToolbar()
        setObservers()
        setVideoLibraryRecyclerView()
    }

    private fun setObservers() {
        viewmodel.response.observe(this) {
            if (it.success) {
                val adapter = YoutubeListingAdapter(
                    it.projectLibraryData.videos,
                    lifecycle,
                    this@YoutubeListingActivity
                )

                binding?.recyclerview?.adapter = adapter
                binding?.recyclerview?.setHasFixedSize(true)
                binding?.recyclerview?.layoutManager =
                    LinearLayoutManager(this@YoutubeListingActivity)
                binding?.progressBar?.visibility = View.GONE
            }
        }
    }

    private fun setData() {
        preferenceHelper = PreferenceHelper(applicationContext)
        authKey = "Bearer ${preferenceHelper?.getJwtAuthToken()}"
        packageName = applicationContext.packageName
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
        viewmodel.fetchVideos(packageName!!, authKey!!)
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