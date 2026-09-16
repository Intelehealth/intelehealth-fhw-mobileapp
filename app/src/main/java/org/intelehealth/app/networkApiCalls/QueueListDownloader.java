package org.intelehealth.app.networkApiCalls;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.QueueDAO;
import org.intelehealth.app.models.queue.QueueItem;
import org.intelehealth.app.models.queue.QueueListResponse;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fetches the queue page from the standalone {@code /api/queue/list} microservice
 * and inserts it into {@code tbl_queue}, exactly like the pull-sync path does with
 * {@code queueDAO.insertQueue(responseDTO.getData().getQueuelist())} in
 * {@link org.intelehealth.app.database.dao.SyncDAO#SyncData}.
 *
 * <p>Temporary standalone fetch until the queue list arrives inside the middleware
 * pull-sync response. The service runs on a different host/port than
 * {@code BuildConfig.SERVER_URL}, so the absolute URL is passed via Retrofit
 * {@code @Url}.
 */
public class QueueListDownloader {

    private static final String TAG = "QueueListDownloader";

    /**
     * Fetches the queue list with the same defaults as the sample request:
     * {@code status=WAITING, sort=priority, includeEta=true, includeScore=false,
     * limit=50, offset=0}.
     */
    public void fetchAndInsert() {
        fetchAndInsert("WAITING", "priority", true, false, 50, 0);
    }

    public void fetchAndInsert(String status, String sort, boolean includeEta,
                               boolean includeScore, int limit, int offset) {
        String url = BuildConfig.SERVER_URL + "/api/queue/list";
        Call<QueueListResponse> queueListCall = AppConstants.apiInterface.QUEUE_LIST_CALL(
                url, status, sort, includeEta, includeScore, limit, offset);

        Logger.logD(TAG, "queue list fetch");
        queueListCall.enqueue(new Callback<QueueListResponse>() {
            @Override
            public void onResponse(Call<QueueListResponse> call, Response<QueueListResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getItems() != null) {

                    List<QueueItem> items = response.body().getData().getItems();

                    // Push the DB write off the main thread, mirroring the pull-sync insert.
                    Single.fromCallable(() -> insertQueue(items))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(inserted -> {
                                // no-op: insert already logged below
                            }, throwable -> Logger.logD(TAG,
                                    "insertQueue error " + throwable.getMessage()));
                } else {
                    Logger.logD(TAG, "queue list failed: http " + response.code()
                            + " url=" + call.request().url());
                }
            }

            @Override
            public void onFailure(Call<QueueListResponse> call, Throwable t) {
                Logger.logD(TAG, "queue list fetch failed url=" + call.request().url()
                        + " : " + t.getMessage());
            }
        });
    }

    private boolean insertQueue(List<QueueItem> items) throws DAOException {
        QueueDAO queueDAO = new QueueDAO();
        queueDAO.insertQueueItems(items);
        Logger.logD(TAG, "insertQueue = " + items.size());
        return true;
    }
}
