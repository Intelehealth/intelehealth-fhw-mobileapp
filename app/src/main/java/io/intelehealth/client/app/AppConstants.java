package io.intelehealth.client.app;


import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.network.ApiClient;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.SqliteDbCloseHelper;

public class AppConstants {

    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    public static ApiClient apiInterface = ApiClient.getApiClient().create(ApiClient.class);

    public static SqliteDbCloseHelper sqliteDbCloseHelper = new SqliteDbCloseHelper();

    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();

}
