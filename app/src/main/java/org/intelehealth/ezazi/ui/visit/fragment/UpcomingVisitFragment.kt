package org.intelehealth.ezazi.ui.visit.fragment

import android.os.Bundle
import android.view.View
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.ui.prescription.activity.PrescriptionActivity

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class UpcomingVisitFragment : VisitStatusFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUpcomingVisit()
    }

    override fun getEmptyDataMessage(): String = getString(
        R.string.no_data_message,
        getString(R.string.title_upcoming_visit)
    )

    override fun getEmptyDataIcon(): Int = R.drawable.ic_outcome_pending

    private fun loadUpcomingVisit() {
        viewMode.upcomingVisits().observe(viewLifecycleOwner) { bindData(it) }
    }

    companion object {
        fun newInstance(): UpcomingVisitFragment {
            return UpcomingVisitFragment()
        }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.btnViewUpcomingPrescription) {
//            PrescriptionActivity.startPrescriptionActivity()
        }
    }
}