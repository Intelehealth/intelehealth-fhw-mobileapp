package app.intelehealth.client.activities.homeActivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import app.intelehealth.client.database.dao.SyncDAO;

/**
 * Created by Prajwal Waingankar
 * on 07-Jan-2021.
 * Github: prajwalmw
 */

public class HomeViewModel extends AndroidViewModel {

    LiveData<String> stringLiveData;
    SyncDAO syncDAO;

    public HomeViewModel(@NonNull Application application) {
        super(application);

        syncDAO = new SyncDAO();
        stringLiveData = syncDAO.pullData_Background(application);

    }

    public LiveData<String> getStringLiveData() {
        return stringLiveData;
    }
}
