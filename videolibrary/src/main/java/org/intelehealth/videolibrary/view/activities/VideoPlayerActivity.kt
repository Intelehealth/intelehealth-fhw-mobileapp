package org.intelehealth.videolibrary.view.activities

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.videolibrary.model.constants.Constants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import org.intelehealth.videolibrary.R
import org.intelehealth.videolibrary.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {

    private var binding: ActivityVideoPlayerBinding? = null
    private var youtubePlayer: YouTubePlayer? = null

    private var isFullScreen: Boolean = false
    private var videoId: String? = null

    private var onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFullScreen) {
                youtubePlayer?.toggleFullscreen()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        videoId = intent.getStringExtra(Constants.VIDEO_ID)
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        binding?.youtubePlayerView?.let {
            lifecycle.addObserver(it)
            it.enableAutomaticInitialization = false
        }

        setToolbar()
        playYouTubeVideo()
        addFullScreenListener()
    }

    private fun setToolbar() {
        binding?.toolbar?.apply {
            setSupportActionBar(this)
            setTitleTextAppearance(this@VideoPlayerActivity, R.style.ToolbarTheme)
            setTitleTextColor(Color.WHITE)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.video_library)
        }
    }

    private fun playYouTubeVideo() {
        videoId?.let {
            binding?.youtubePlayerView?.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    this@VideoPlayerActivity.youtubePlayer = youTubePlayer
                    youTubePlayer.loadVideo(it, 0f)
                }
            }, iFramePlayerOptions)
        }
    }

    private fun addFullScreenListener() {
        binding?.youtubePlayerView?.addFullscreenListener(object : FullscreenListener {
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                isFullScreen = true
                binding?.youtubePlayerView?.visibility = View.GONE
                binding?.fullScreenViewContainer?.visibility = View.VISIBLE
                binding?.fullScreenViewContainer?.addView(fullscreenView)
                binding?.appBarLayout?.visibility = View.GONE
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            override fun onExitFullscreen() {
                isFullScreen = false
                binding?.youtubePlayerView?.visibility = View.VISIBLE
                binding?.fullScreenViewContainer?.visibility = View.GONE
                binding?.fullScreenViewContainer?.removeAllViews()
                binding?.appBarLayout?.visibility = View.VISIBLE
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        })
    }

    companion object {
        private val iFramePlayerOptions: IFramePlayerOptions =
            IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(1)
                .build()
    }
}