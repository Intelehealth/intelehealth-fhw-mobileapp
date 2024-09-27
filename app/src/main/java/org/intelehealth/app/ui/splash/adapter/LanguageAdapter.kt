package org.intelehealth.app.ui.splash.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.databinding.LanguageListItemViewUi2Binding
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.core.ui.adapter.BaseRecyclerViewAdapter
import org.intelehealth.core.ui.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 14:12.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class LanguageAdapter(context: Context, languages: List<ActiveLanguage>) :
    org.intelehealth.core.ui.adapter.BaseRecyclerViewAdapter<ActiveLanguage>(context, languages.toMutableList()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LanguageListItemViewUi2Binding.inflate(inflater, parent, false).let {
            LanguageViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LanguageViewHolder) {
            viewHolderClickListener?.let { holder.setViewClickListener(it) }
            holder.bind(getItem(position))
        }
    }

    fun select(position: Int, language: ActiveLanguage) {
        getList().forEach { it.selected = false }
        language.selected = true
        getList().toMutableList()[position] = language
        notifyItemRangeChanged(0, itemCount)
    }
}

class LanguageViewHolder(private val binding: LanguageListItemViewUi2Binding) :
    org.intelehealth.core.ui.viewholder.BaseViewHolder(binding.root) {

    fun bind(language: ActiveLanguage) {
        binding.layoutRbChooseLanguage.tag = language
        binding.layoutRbChooseLanguage.setOnClickListener(this)
        binding.layoutRbChooseLanguage.isSelected = language.selected
        binding.rbChooseLanguage.isChecked = language.selected
        binding.language = language
    }
}