package org.intelehealth.videolibrary.view.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.videolibrary.R
import org.intelehealth.videolibrary.databinding.ActivityYoutubeListingBinding
import org.intelehealth.videolibrary.model.callbacks.VideoClickedListener
import org.intelehealth.videolibrary.model.constants.Constants
import org.intelehealth.videolibrary.view.adapter.YoutubeListingAdapter

private val videoIDs: List<String> = listOf(
    "-98fnc4VAo8",
    "h2svwQmFwHE",
    "m5Kn9WmOCrw",
    "RDvs-FvgAC8"
)

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
        val adapter = YoutubeListingAdapter(
            videoIDs,
            lifecycle,
            this
        )

        binding?.recyclerview?.let {
            it.adapter = adapter
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(this@YoutubeListingActivity)
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