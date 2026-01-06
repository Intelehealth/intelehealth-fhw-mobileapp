package org.intelehealth.ncd.linelisting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.databinding.LayoutLoadMoreItemsBinding
class PatientLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<PatientLoadStateAdapter.LoadStateVH>() {

    inner class LoadStateVH(private val binding: LayoutLoadMoreItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.errorMsg.isVisible = loadState is LoadState.Error
            binding.retryButton.isVisible = loadState is LoadState.Error

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
