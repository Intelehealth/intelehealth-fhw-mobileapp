package org.intelehealth.app.activities.notification;

import android.content.Context;
/**
 * Created by Prajwal Waingankar on 29/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.models.dto.PatientDTO;

import java.util.List;

public interface NotificationInterface {

    void deleteItem(List<NotificationModel> list, int position);
}
