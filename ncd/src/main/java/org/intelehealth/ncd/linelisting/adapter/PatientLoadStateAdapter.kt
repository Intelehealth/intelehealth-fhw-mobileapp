package org.intelehealth.ncd.linelisting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.databinding.LayoutLoadMoreItemsBinding
class PatientLoadStateAdapter(
    private val retry: () -> Unit,
    private val mainAdapterItemCount: () -> Int = { 0 },
) : LoadStateAdapter<PatientLoadStateAdapter.LoadStateVH>() {

    /**
     * Footer must not show append [LoadState.Loading] when the main list is empty — otherwise the
     * load-more row is the only visible content and looks like a stuck full-screen loader.
     */
    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        return when (loadState) {
            is LoadState.Error -> true
            is LoadState.Loading -> mainAdapterItemCount() > 0
            is LoadState.NotLoading -> false
        }
    }

    inner class LoadStateVH(private val binding: LayoutLoadMoreItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.errorMsg.isVisible = loadState is LoadState.Error
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.loadMoreText.isVisible = loadState is LoadState.Loading

            if (loadState is LoadState.Error) {
                binding.errorMsg.text = loadState.error.localizedMessage
            }

            binding.retryButton.setOnClickListener { retry.invoke() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutLoadMoreItemsBinding.inflate(inflater, parent, false)
        return LoadStateVH(binding)
    }

    override fun onBindViewHolder(holder: LoadStateVH, loadState: LoadState) {
        holder.bind(loadState)
    }
}
