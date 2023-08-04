package org.intelehealth.ezazi.ui.rtc.call;

import android.util.Log;
import android.widget.Toast;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.core.data.BaseDataSource;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.pushRequestApiCall.Attribute;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.password.listener.OnAPISuccessListener;
import org.intelehealth.ezazi.ui.rtc.data.RtcTokenDataSource;
import org.intelehealth.ezazi.ui.rtc.model.UserToken;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.utils.RemoteActionType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 06-07-2023 - 14:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class CallInitializer {
    public interface OnCallInitializedListener {
        void onInitialized(RtcArgs args);
    }

    private final RtcArgs args;

    public CallInitializer(RtcArgs args) {
        this.args = args;
    }

    public void initiateVideoCall(OnCallInitializedListener listener) {
        String BASE_URL = "https://" + AppConstants.APP_URL + ":3000";
        ApiClient.changeApiBaseUrl(BASE_URL);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        new RtcTokenDataSource(apiService).getRtcToken(result -> {
            args.setToken(result.getToken());
            args.setActionType(RemoteActionType.VIDEO_CALL.name());
            args.setAppToken(result.getAppToken());
            listener.onInitialized(args);
        }, args);
    }

    public static HashMap<String, String> getDoctorsDetails(String patientUuid) {
        PatientsDAO patientsDAO = new PatientsDAO();
        LinkedHashMap<String, String> doctors = new LinkedHashMap<>();
        try {
            List<Attribute> patientAttributes = patientsDAO.getPatientAttributes(patientUuid);
            for (int i = 0; i < patientAttributes.size(); i++) {
                String name = patientsDAO.getAttributesName(patientAttributes.get(i).getAttributeType());
                if (name.equalsIgnoreCase("PrimaryDoctor")) {
                    String doctorUuid = patientAttributes.get(i).getValue().split("@#@")[0];
                    String doctorName = patientAttributes.get(i).getValue().split("@#@")[1];
                    doctors.put(doctorName, doctorUuid);
                }
                if (name.equalsIgnoreCase("SecondaryDoctor")) {
                    String doctorUuid = patientAttributes.get(i).getValue().split("@#@")[0];
                    String doctorName = patientAttributes.get(i).getValue().split("@#@")[1];
                    doctors.put(doctorName, doctorUuid);
                }
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        return doctors;
    }
}
