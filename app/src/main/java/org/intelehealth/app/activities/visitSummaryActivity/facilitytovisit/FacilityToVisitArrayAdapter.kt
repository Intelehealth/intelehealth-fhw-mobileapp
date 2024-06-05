package org.intelehealth.app.activities.visitSummaryActivity.facilitytovisit
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.intelehealth.app.R
import org.intelehealth.config.room.entity.Specialization
import org.intelehealth.config.utility.ResUtils


class FacilityToVisitArrayAdapter(
    private val context: Context,
    private val model: List<FacilityToVisitModel>
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    override fun getCount(): Int = model.size

    override fun getItem(position: Int): FacilityToVisitModel = model[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun getPosition(value: String): Int {
        model.forEachIndexed { index, specialization ->
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

    fun bindItem(context: Context, model: FacilityToVisitModel) {
        textView.setTag(R.id.speciality_spinner, model)
        textView.text = model.name
    }

}