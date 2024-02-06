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
import org.intelehealth.videolibrary.data.SessionManager
import org.intelehealth.videolibrary.databinding.ActivityYoutubeListingBinding
import org.intelehealth.videolibrary.listing.adapter.YoutubeListingAdapter
import org.intelehealth.videolibrary.player.activity.VideoPlayerActivity
import org.intelehealth.videolibrary.restapi.RetrofitProvider
import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse

class YoutubeListingActivity : AppCompatActivity(), VideoClickedListener {

    private var binding: ActivityYoutubeListingBinding? = null
    private var sessionManager: SessionManager? = null

    private var authKey: String? = null
    private var packageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeListingBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setData()
        setToolbar()
        setVideoLibraryRecyclerView()
    }

    private fun setData() {
        sessionManager = SessionManager(applicationContext)
        authKey = sessionManager?.getJwtAuthToken()
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
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                binding?.progressBar?.visibility = View.VISIBLE
            }

            val videoLibraryResponse: VideoLibraryResponse = RetrofitProvider
                .apiService
                .fetchVideosLibrary(
                    packageName = packageName!!,
                    auth = "Bearer $authKey"
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