package org.intelehealth.unicef.activities.notification;

/**
 * Created by Prajwal Waingankar on 29/09/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
import org.intelehealth.unicef.models.DocumentObject;
import org.intelehealth.unicef.models.NotificationModel;

        import java.util.List;

public interface AdapterInterface {

    void deleteNotifi_Item(List<NotificationModel> list, int position);

    void deleteAddDoc_Item(List<DocumentObject> list, int position);
}
