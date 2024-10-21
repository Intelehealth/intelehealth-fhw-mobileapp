package org.intelehealth.app.shared.builder;

import org.intelehealth.app.utilities.CustomLog;

/**
 * Created by Vaghela Mithun R. on 25-06-2023 - 10:58.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class VisitQueryBuilder extends QueryBuilder {
    public static final String TAG = "PatientQueryBuilder";


    public String visitCompletedByCreatorCount(String providerId) {
        String query = select(" COUNT(DISTINCT visituuid) as completed ")
                .from(" tbl_encounter ")
                .where(" encounter_type_uuid = '629a9d0b-48eb-405e-953d-a5964c88dc30' AND provider_uuid = '" + providerId + "' ")
                .build();
        CustomLog.e(TAG, "outcomePendingPatientCountQuery => " + query);
        return query;
    }

//    select (substr(enddate, 9, 4)||(CASE substr(enddate,1, 3) WHEN 'Jan' then '01' WHEN 'Feb' then '02' WHEN 'Mar'  then '03' WHEN 'Apr'  then '04' WHEN 'May'  then '05'
//    WHEN 'Jun'  then '06' WHEN 'Jul'  then '07' WHEN 'Aug'  then '08' WHEN 'Sep'  then '09' WHEN 'Oct'  then '10' WHEN 'Nov'  then '11' WHEN 'Dec'  then '12' END)||substr(enddate,5, 2)) as mdate,
//    enddate from tbl_visit where enddate is not null
}
