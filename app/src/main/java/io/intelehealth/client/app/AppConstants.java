package io.intelehealth.client.app;


import io.intelehealth.client.utils.SessionManager;

public class AppConstants {

    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
    public static SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    public static RestApi apiInterface = ApiClient.getApiClient().create(RestApi.class);

    public static SqliteDbCloseHelper sqliteDbCloseHelper = new SqliteDbCloseHelper();

    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();

}
