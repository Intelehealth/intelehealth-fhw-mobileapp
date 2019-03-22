package io.intelehealth.client.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.intelehealth.client.api.retrofit.RestApi;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.network.ApiClient;
import io.intelehealth.client.network.models.PatientUUIDResponsemodel;
import io.intelehealth.client.network.visitModel.VisitModel;
import io.intelehealth.client.network.visitModels.VisitResponsemodel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class visitSummaryHelper {
    RestApi apiInterface;
    String Tag="visitsummary";
    private SessionManager sessionManger;

    //checking the patient visit or not
    public VisitModel isOpenmrsVisitExists(String patientuuid, final String visittimestamp) {

        sessionManger=new SessionManager(IntelehealthApplication.getAppContext());
        VisitModel visitmodel = new VisitModel();
        apiInterface = ApiClient.getApiClient().create(RestApi.class);

        Call<PatientUUIDResponsemodel> patientUUIDResponsemodelCall = apiInterface.GETPATIENT(patientuuid, "Basic "+sessionManger.getEncoded());
        try {

            PatientUUIDResponsemodel patientUUIDResponsemodel = patientUUIDResponsemodelCall.execute().body();
            if(patientUUIDResponsemodel!=null) {
                if (patientUUIDResponsemodel.getResults().size() == 0)
                    return visitmodel;
                for (int i = 0; i < patientUUIDResponsemodel.getResults().get(0).getLinks().size(); i++) {
                    visitmodel = secondVistCAll(patientUUIDResponsemodel.getResults().get(0).getLinks().get(i).getUri(), visittimestamp);
                    if (visitmodel.isVisitExists() == true) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return visitmodel;
    }


    public VisitModel secondVistCAll(String url,  final String visittime){
        sessionManger=new SessionManager(IntelehealthApplication.getAppContext());
        Log.d(Tag, "secondVistCAll:"+url +" visit time"+visittime);
        VisitModel visitmodel = new VisitModel();
        VisitResponsemodel visitResponsemodel = new VisitResponsemodel();
        apiInterface = ApiClient.getApiClient().create(RestApi.class);

        Call<VisitResponsemodel> visitResponsemodelCall = apiInterface.VISIT_RESPONSEMODEL_CALL(url,"Basic "+sessionManger.getEncoded());
        try {
            visitResponsemodel=visitResponsemodelCall.execute().body();

            String time=visitResponsemodel.getStartDatetime();
            String localtime=visittime.substring(0, visittime.length() - 9);
            String servetime=time.substring(0,time.length()-9);
            if(localtime.equalsIgnoreCase(servetime)){ Log.d(Tag,"second visit api call");

                Log.d(Tag, "match local"+localtime+" server "+servetime);
                visitmodel.setVisitExists(true);
                visitmodel.setVisituuid(visitResponsemodel.getUuid());
                return visitmodel;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return visitmodel;
    }



}
