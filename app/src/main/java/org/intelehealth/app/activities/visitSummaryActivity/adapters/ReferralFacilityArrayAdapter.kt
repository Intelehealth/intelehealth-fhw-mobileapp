package org.intelehealth.app.activities.visitSummaryActivity.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.intelehealth.app.R
import org.intelehealth.app.activities.visitSummaryActivity.model.ReferralFacilityData
import org.intelehealth.app.enums.ReferralFacilityDataFormatType
import org.intelehealth.app.utilities.LanguageUtils


class ReferralFacilityArrayAdapter(
    private val context: Context,
    private val dataList: List<ReferralFacilityData>
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    override fun getCount(): Int = dataList.size

    override fun getItem(position: Int): ReferralFacilityData = dataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun getPosition(value: String): Int {
        dataList.forEachIndexed { index, ref ->
            if (ref.facilityName == value) return index
        }
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var convertView: View? = view
        val holder: ReferralFacilityViewHolder
        if (convertView == null) {
            convertView = inflater.inflate(
                android.R.layout.simple_spinner_dropdown_item,
                parent,
                false
            )

            holder = ReferralFacilityViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ReferralFacilityViewHolder
        }

        holder.bindItem(context, getItem(position))
        return holder.getRootView()
    }
}

class ReferralFacilityViewHolder(view: View) {
    val textView: TextView = view as TextView
    fun getRootView() = textView

    fun bindItem(context: Context, referralFacilityData: ReferralFacilityData) {
        textView.setTag(R.id.speciality_spinner, referralFacilityData)
        textView.text = LanguageUtils.getReferralFacilityDataByLanguage(referralFacilityData,ReferralFacilityDataFormatType.VIEW)
    }

}