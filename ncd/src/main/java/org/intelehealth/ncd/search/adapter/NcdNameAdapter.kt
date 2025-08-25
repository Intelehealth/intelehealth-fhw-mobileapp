package org.intelehealth.ncd.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.intelehealth.ncd.databinding.ListItemCategoryNameBinding

class NcdNameAdapter(
    private val diseaseList: List<String>,
    private val context: Context
) : RecyclerView.Adapter<NcdNameAdapter.NcdNameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NcdNameViewHolder {
        val binding: ListItemCategoryNameBinding = ListItemCategoryNameBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return NcdNameViewHolder(binding)
    }

    override fun getItemCount(): Int = diseaseList.size

    override fun onBindViewHolder(holder: NcdNameViewHolder, position: Int) {
        holder.setData(diseaseList[position])
    }

    class NcdNameViewHolder(
        private val binding: ListItemCategoryNameBinding
    ) : ViewHolder(binding.root) {

        fun setData(data: String) {
            binding.tvCategoryName.text = data
        }
    }
}