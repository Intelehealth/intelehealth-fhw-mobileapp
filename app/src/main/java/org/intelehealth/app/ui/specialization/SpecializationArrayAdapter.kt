package org.intelehealth.app.ui.specialization

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.intelehealth.app.R
import org.intelehealth.config.room.entity.Specialization
import org.intelehealth.config.utility.ResUtils

/**
 * Created by Vaghela Mithun R. on 17-04-2024 - 14:50.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SpecializationArrayAdapter(
    private val context: Context,
    private val specializations: List<Specialization>
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    override fun getCount(): Int = specializations.size

    override fun getItem(position: Int): Specialization = specializations[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun getPosition(value: String): Int {
        specializations.forEachIndexed { index, specialization ->
            if (specialization.name == value) return index
        }
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var convertView: View? = view
        val holder: SpecializationViewHolder
        if (convertView == null) {
            convertView = inflater.inflate(
                android.R.layout.simple_spinner_dropdown_item,
                parent,
                false
            )

            holder = SpecializationViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as SpecializationViewHolder
        }

        holder.bindItem(context, getItem(position))
        return holder.getRootView()
    }
}

class SpecializationViewHolder(view: View) {
    val textView: TextView = view as TextView
    fun getRootView() = textView

    fun bindItem(context: Context, specialization: Specialization) {
        textView.setTag(R.id.speciality_spinner, specialization)
        textView.text = ResUtils.getStringResourceByName(context, specialization.sKey)
    }

}