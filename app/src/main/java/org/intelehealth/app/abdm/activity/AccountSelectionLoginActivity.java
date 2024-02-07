package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.adapter.MultipleAccountsAdapter;
import org.intelehealth.app.abdm.model.ABHAProfile;
import org.intelehealth.app.abdm.model.AbhaProfileRequestBody;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.Account;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.activities.visit.adapter.PastVisitListingAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAccountSelectionLoginBinding;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.WindowsUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AccountSelectionLoginActivity extends AppCompatActivity {
    private ActivityAccountSelectionLoginBinding binding;
    public static final String TAG = AccountSelectionLoginActivity.class.getSimpleName();
    private Context context = AccountSelectionLoginActivity.this;
    private MultipleAccountsAdapter accountsAdapter;
    private List<Account> accountList;
    private String txnId, X_TOKEN, accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSelectionLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AccountSelectionLoginActivity.this);  // changing status bar color

        Intent intent = getIntent();
        txnId = intent.getStringExtra("payload");
        X_TOKEN = intent.getStringExtra("X_TOKEN");
        accessToken = intent.getStringExtra("accessToken");

        accountList = new ArrayList<>();
        accountsAdapter = new MultipleAccountsAdapter(context, accountList, new MultipleAccountsAdapter.OnItemClick() {
            @Override
            public void OnItemSelected(Account account) {
                Log.d(TAG, "OnItemSelected: " + account.toString());
                // pass this account payload to fetch details api and move to Identification screen...
                callFetchUserProfileAPI(account.getABHANumber(), txnId, X_TOKEN);
            }
        });

        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(context));
        binding.rvAccounts.setAdapter(accountsAdapter);
    }

    private void callFetchUserProfileAPI(String abhaNumber, String txnId, String xToken) {
//        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
//        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload - start
        String url = UrlModifiers.getABHAProfileUrl();
        AbhaProfileRequestBody requestBody = new AbhaProfileRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setAbhaNumber(abhaNumber);
        // payload - end

        Single<AbhaProfileResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.PUSH_ABHA_PROFILE(url, accessToken, xToken, requestBody);
        new Thread(new Runnable() {
            @Override
            public void run() {
                abhaProfileResponseSingle
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<AbhaProfileResponse>() {
                            @Override
                            public void onSuccess(AbhaProfileResponse abhaProfileResponse) {
                                Log.d("callFetchUserProfileAPI", "onSuccess: " + abhaProfileResponse);
                                Intent intent;
                                // ie. only 1 account exists.
                                intent = new Intent(context, IdentificationActivity_New.class);
                                intent.putExtra("mobile_payload", abhaProfileResponse);
                                intent.putExtra("accessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("callFetchUserProfileAPI", "onError: " + e.toString());
                            }
                        });
            }
        }).start();

    }

}