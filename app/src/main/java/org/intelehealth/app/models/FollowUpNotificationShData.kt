package org.intelehealth.app.models

/**
 * Created By Tanvir Hasan on 6/4/24 12:15 AM
 * Email: tanvirhasan553@gmail.com
 */
data class FollowUpNotificationShData(var visitUuid:String,var scheduleDateTime:String){
    constructor():this("","")
}
