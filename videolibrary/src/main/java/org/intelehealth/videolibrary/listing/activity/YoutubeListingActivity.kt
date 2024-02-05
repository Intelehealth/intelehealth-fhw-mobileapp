package org.intelehealth.videolibrary.listing.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.videolibrary.R
import org.intelehealth.videolibrary.callbacks.VideoClickedListener
import org.intelehealth.videolibrary.constants.Constants
import org.intelehealth.videolibrary.databinding.ActivityYoutubeListingBinding
import org.intelehealth.videolibrary.listing.adapter.YoutubeListingAdapter
import org.intelehealth.videolibrary.player.activity.VideoPlayerActivity
import org.intelehealth.videolibrary.restapi.RetrofitProvider
import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse

class YoutubeListingActivity : AppCompatActivity(), VideoClickedListener {

    private var binding: ActivityYoutubeListingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeListingBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setToolbar()
        setVideoLibraryRecyclerView()
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
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                binding?.progressBar?.visibility = View.VISIBLE
            }

            val videoLibraryResponse: VideoLibraryResponse = RetrofitProvider
                .apiService
                .fetchVideosLibrary(
                    "org.intelehealth.ekalarogya",
                    "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MDcxNTc3OTksImRhdGEiOnsic2Vzc2lvbklkIjoiRTkwMTgwN0FBNzA1NEIyMzUwMkY0NEY5OUNFMDA4ODIiLCJ1c2VySWQiOiI0ODI3OTdhYi0wODEyLTRmNjQtYTMwOC1lYmY5NDI5N2M3NTAiLCJuYW1lIjoiYXJwYW5udXJzZSJ9LCJpYXQiOjE3MDcxNDEyODR9.Flb31TCnMm5CqyMCVpHmVsXDNJoi2jSAR02QGQOpTOCAfXDRtMLOrcJvQj3cz9Dp7W23MXrslvlvGf3dK1ao4F9zIiCeqlLQYMJ0e2ZkllLTni870H-AIBwK0xBAdxG4U-xs8LiUE1aXlywSfGqmGvA5qmT6C_moyQSrpEIgVRkE3g6cOsF-bkG2XiDQFgbCtaQkm9orqgkperjVs0RVW_j0Z6Qrw1pOPFvOr10Ao7MzSE-cQTSjTOG1_57zmZevoEbF_VYpokftGnKSpP4bxNBGQ5IYuHxF8xGf2FHN_GVLCm-rndYRD4Gib6X34dbqVTG9SSDkolFNcxyY4jyf4w"
                )

            if (videoLibraryResponse.success) {
                withContext(Dispatchers.Main) {
                    val adapter = YoutubeListingAdapter(
                        videoLibraryResponse.projectLibraryData.videos,
                        lifecycle,
                        this@YoutubeListingActivity
                    )

                    binding?.recyclerview?.let {
                        it.adapter = adapter
                        it.setHasFixedSize(true)
                        it.layoutManager = LinearLayoutManager(this@YoutubeListingActivity)
                    }

                    binding?.progressBar?.visibility = View.GONE
                }
            }
        }
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