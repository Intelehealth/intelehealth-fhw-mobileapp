package io.intelehealth.client.dao;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.ResponseDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PullDataDAO {

    private SessionManager sessionManager = null;
    public boolean pullData(final Context context) {
        sessionManager = new SessionManager(context);
        String url = "http://142.93.221.37:8080/EMR-Middleware/webapi/pull/pulldata/1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a/" + sessionManager.getPullExcutedTime();
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url);

        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {

                if (response.isSuccessful()) {
                    pullDataExecutedTime(response.body(), context);
                }
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPullExcutedTime(response.body().getData().getPullexecutedtime());
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
            }
        });

        return true;
    }

    public void pullDataExecutedTime(final ResponseDTO responseDTO, final Context context) {
        class dataInserted extends AsyncTask<Void, Void, Void> {
            private ProgressDialog dialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(Void... voids) {
                SyncDAO syncDAO = new SyncDAO();
                try {
                    syncDAO.SyncData(responseDTO);
                } catch (DAOException e) {
                    Logger.logE("Dao exception", "exception", e);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                this.dialog.setMessage("Please wait");
                this.dialog.setCancelable(false);
                this.dialog.show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
        dataInserted dataInserted = new dataInserted();
        dataInserted.execute();

    }
}




