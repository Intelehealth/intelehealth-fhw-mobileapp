package org.intelehealth.app.activities.notification;

import org.intelehealth.app.models.dto.PatientDTO;

import java.util.List;

public interface NotificationInterface {

    public void deleteItem(List<PatientDTO> list, int position);
}
