package org.intelehealth.ezazi.ui.rtc.call;

import android.util.Log;
import android.widget.Toast;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.core.data.BaseDataSource;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.pushRequestApiCall.Attribute;
import org.intelehealth.ezazi.networkApiCalls.ApiClient;
import org.intelehealth.ezazi.networkApiCalls.ApiInterface;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.ui.password.listener.OnAPISuccessListener;
import org.intelehealth.ezazi.ui.rtc.data.RtcTokenDataSource;
import org.intelehealth.ezazi.ui.rtc.model.UserToken;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.utils.RemoteActionType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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

    public static LinkedList<SingChoiceItem> getDoctorsDetails(String patientUuid) {
        PatientsDAO patientsDAO = new PatientsDAO();
        LinkedList<SingChoiceItem> doctors;
        try {
            List<Attribute> patientAttributes = patientsDAO.getPatientAttributes(patientUuid);
            Log.e("CallInitializer", "getDoctorsDetails: " + patientAttributes.size());
            LinkedHashMap<String, SingChoiceItem> tempMap = new LinkedHashMap<>();
            for (int i = 0; i < patientAttributes.size(); i++) {
                String name = patientsDAO.getAttributesName(patientAttributes.get(i).getAttributeType());
                if (name.equalsIgnoreCase(PatientAttributesDTO.Columns.PRIMARY_DOCTOR.value)) {
                    String[] primary = splitString(patientAttributes.get(i));
                    tempMap.put(primary[0], buildItem(primary[0], primary[1], AppConstants.PRIMARY));
                }
                if (name.equalsIgnoreCase(PatientAttributesDTO.Columns.SECONDARY_DOCTOR.value)) {
                    String[] secondary = splitString(patientAttributes.get(i));
                    if (!secondary[0].equalsIgnoreCase(AppConstants.NOT_APPLICABLE)) {
                        tempMap.put(secondary[0], buildItem(secondary[0], secondary[1], AppConstants.SECONDARY));
                    }
                }
            }
            doctors = new LinkedList<>(tempMap.values());
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }


        return doctors;
    }

    private static String[] splitString(Attribute attribute) {
        return attribute.getValue().split("@#@");
    }

    private static SingChoiceItem buildItem(String uuid, String name, String type) {
        SingChoiceItem item = new SingChoiceItem();
        item.setItemId(uuid);
        item.setItem(name);
        item.setSecondaryName(type);
        return item;
    }
}
