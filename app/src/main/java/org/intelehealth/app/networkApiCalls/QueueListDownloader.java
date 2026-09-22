package org.intelehealth.app.networkApiCalls;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.QueueDAO;
import org.intelehealth.app.models.queue.QueueItem;
import org.intelehealth.app.models.queue.QueueListResponse;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

import java.time.Instant;
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

    // TODO: TEMPORARY (testing only) — offset used to fake etaAt/connectedAt on
    // every fetched row so wait time / duration render with a known ~10 min value.
    // Generated relative to now (UTC) so it's correct regardless of device
    // timezone. Remove once the queue service sends real timestamps.
    private static final long TEST_OFFSET_SECONDS = 10 * 60;

    /**
     * Fetches the queue list with the same defaults as the sample request:
     * {@code status=WAITING, sort=priority, includeEta=true, includeScore=false,
     * limit=50, offset=0}.
     */
    public void fetchAndInsert(String locationUuid, String encoded) {
        fetchAndInsert(locationUuid, "WAITING", "priority", true, false, 50, 0, encoded);
    }

    public void fetchAndInsert(String locationUuid, String status, String sort,
                               boolean includeEta, boolean includeScore, int limit, int offset,
                               String encoded) {
        String url = BuildConfig.SERVER_URL + ":3600/api/queue/list";
        Call<QueueListResponse> queueListCall = AppConstants.apiInterface.QUEUE_LIST_CALL(
                url, locationUuid, status, sort, includeEta, includeScore,
                limit, offset, encoded, "123");

        Logger.logD(TAG, "queue list fetch");
        queueListCall.enqueue(new Callback<QueueListResponse>() {
            @Override
            public void onResponse(Call<QueueListResponse> call, Response<QueueListResponse> response) {
                Logger.logD(TAG, "queue list in success " + response.isSuccessful() + " -- " + call.request().url());
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getItems() != null) {
                    Logger.logD(TAG, "queue list data received ");

                    List<QueueItem> items = response.body().getData().getItems();

                    // TODO: TEMPORARY (testing only) — fake etaAt (~10 min ahead)
                    // and connectedAt (~10 min ago) relative to now, so wait time
                    // and duration show ~10:00. Remove once the service sends real
                    // timestamps.
                    Instant now = Instant.now();
                    String etaAtTest = now.plusSeconds(TEST_OFFSET_SECONDS).toString();
                    String connectedAtTest = now.minusSeconds(TEST_OFFSET_SECONDS).toString();
                    for (QueueItem item : items) {
                        item.setEtaAt(etaAtTest);
                        item.setConnectedAt(connectedAtTest);
                    }

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
