package org.intelehealth.app.activities.visitSummaryActivity.model

import com.google.gson.annotations.SerializedName

/**
 * Created By Tanvir Hasan on 8/4/24 4:50 PM
 * Email: tanvirhasan553@gmail.com
 */

data class ReferralFacility(
    @SerializedName("referral_facility_list")
    var data: List<ReferralFacilityData>
)
