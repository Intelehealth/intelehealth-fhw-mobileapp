package org.intelehealth.app.database.dao;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.queue.QueueListData;
import org.intelehealth.app.models.queue.QueueListResponse;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fetches the doctor/HW queue from the queue microservice
 * ({@code GET /api/queue/list}) and returns the parsed page.
 *
 * <p>Modelled on {@link SyncDAO#pullData(Context, String, int)}: it builds the
 * full URL, attaches the auth header, fires the Retrofit call asynchronously
 * with {@code enqueue(...)}, and delivers the Gson-parsed body back through a
 * {@link Callback callback}. Unlike the middleware pull it does not write to
 * the local DB — the queue list is display-only and lives in memory.
 */
public class QueueListDAO {

    private static final String TAG = "QueueListDAO";

    /**
     * Base URL of the queue microservice. The service runs on its own port
     * (3600) alongside the OpenMRS host, mirroring how the socket service uses
     * {@code :3004}. Adjust here if the deployment differs from host:3600.
     */
    private static final String QUEUE_API_BASE_URL = BuildConfig.SERVER_URL + ":3600";

    /** Delivers the queue page, or a failure reason, on the main thread. */
    public interface QueueListCallback {
        void onSuccess(@NonNull QueueListData data);

        void onError(String message);
    }

    /**
     * @param status        queue status filter, e.g. {@code WAITING}
     * @param sort          sort key, e.g. {@code priority}
     * @param includeEta    ask the server to compute ETA per row
     * @param includeScore  ask the server to include the priority score
     * @param limit         page size
     * @param offset        page offset
     */
    public static void fetchQueueList(Context context, String status, String sort,
                                      boolean includeEta, boolean includeScore,
                                      int limit, int offset,
                                      final QueueListCallback callback) {

        SessionManager sessionManager = new SessionManager(context);

        String url = QUEUE_API_BASE_URL + "/api/queue/list"
                + "?status=" + status
                + "&sort=" + sort
                + "&includeEta=" + includeEta
                + "&includeScore=" + includeScore
                + "&limit=" + limit
                + "&offset=" + offset;

        // The queue microservice authenticates with the JWT (same token the
        // appointment APIs use), not OpenMRS Basic auth. If your deployment
        // expects Basic instead, swap this for "Basic " + sessionManager.getEncoded().
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        CustomLog.d(TAG, "fetchQueueList url: " + url);

        Call<QueueListResponse> call = AppConstants.apiInterface.QUEUE_LIST_CALL(url, authHeader);
        call.enqueue(new Callback<QueueListResponse>() {
            @Override
            public void onResponse(@NonNull Call<QueueListResponse> call,
                                   @NonNull Response<QueueListResponse> response) {
                QueueListResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()
                        && body.getData() != null) {
                    callback.onSuccess(body.getData());
                } else {
                    String message = body != null && !TextUtils.isEmpty(body.getMessage())
                            ? body.getMessage()
                            : "Queue list request failed (HTTP " + response.code() + ")";
                    CustomLog.e(TAG, "fetchQueueList failed: " + message);
                    callback.onError(message);
                }
            }

            @Override
            public void onFailure(@NonNull Call<QueueListResponse> call, @NonNull Throwable t) {
                CustomLog.e(TAG, "fetchQueueList error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}
