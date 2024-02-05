package org.intelehealth.videolibrary.listing.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.videolibrary.callbacks.VideoClickedListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import org.intelehealth.videolibrary.databinding.ItemVideoListingBinding

internal class YoutubeListingAdapter(
    private val videoIds: List<String>,
    private val lifecycle: Lifecycle,
    private val listener: VideoClickedListener
) : RecyclerView.Adapter<YoutubeListingAdapter.YoutubeListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YoutubeListingViewHolder {
        val binding: ItemVideoListingBinding = ItemVideoListingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return YoutubeListingViewHolder(
            binding,
            lifecycle = this@YoutubeListingAdapter.lifecycle,
            listener
        )
    }

    override fun getItemCount(): Int = videoIds.size

    override fun onBindViewHolder(holder: YoutubeListingViewHolder, position: Int) {
        holder.cueVideo(videoIds[position])

    }

    class YoutubeListingViewHolder(
        binding: ItemVideoListingBinding,
        lifecycle: Lifecycle,
        listener: VideoClickedListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private val youtubePlayerView = binding.youtubePlayerView
        private var youtubePlayer: YouTubePlayer? = null
        private var currentVideoId: String? = null

        init {
            lifecycle.addObserver(binding.youtubePlayerView)

            youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    this@YoutubeListingViewHolder.youtubePlayer = youTubePlayer
                    currentVideoId?.let { youTubePlayer.cueVideo(it, 0f) }
                }
            })

            binding.overlayView.setOnClickListener {
                currentVideoId?.let { id -> listener.onVideoClicked(id) }
            }
        }

        internal fun cueVideo(videoId: String) {
            currentVideoId = videoId
            youtubePlayer?.cueVideo(videoId, 0f)
        }
    }
}