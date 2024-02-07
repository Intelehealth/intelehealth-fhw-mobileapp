package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.adapter.MultipleAccountsAdapter;
import org.intelehealth.app.abdm.model.ABHAProfile;
import org.intelehealth.app.abdm.model.AbhaProfileRequestBody;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.Account;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.activities.visit.adapter.PastVisitListingAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAccountSelectionLoginBinding;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
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
    private MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse;
    private Account selectedAccount;
    SnackbarUtils snackbarUtils;
    SessionManager sessionManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSelectionLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AccountSelectionLoginActivity.this);  // changing status bar color
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);

        Intent intent = getIntent();
        if (intent != null) {
            mobileLoginOnOTPVerifiedResponse = (MobileLoginOnOTPVerifiedResponse) intent.getSerializableExtra("payload");
            if (mobileLoginOnOTPVerifiedResponse != null)
                txnId = mobileLoginOnOTPVerifiedResponse.getTxnId();
            X_TOKEN = intent.getStringExtra("X_TOKEN");
            accessToken = intent.getStringExtra("accessToken");
        }

      /*  // Adding account in the list.
        accountList = new ArrayList<>();
        Account account = new Account();
        account.setPreferredAbhaAddress("prajwal@sbx");
        account.setName("Prajwal Maruti Waingankar");
        accountList.add(account);

        account = new Account();
        account.setPreferredAbhaAddress("aparna@sbx");
        account.setName("Aparna Maruti Waingankar");
        accountList.add(account);

        account = new Account();
        account.setPreferredAbhaAddress("maruti@sbx");
        account.setName("Maruti Rama Waingankar");
        accountList.add(account);
       */

        for (Account account : mobileLoginOnOTPVerifiedResponse.getAccounts()) {
            accountList.add(account);
        } // todo: uncomment later. testing purpose.

        accountsAdapter = new MultipleAccountsAdapter(context, accountList, new MultipleAccountsAdapter.OnItemClick() {
            @Override
            public void OnItemSelected(Account account, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "OnItemSelected: " + account.toString());
                    selectedAccount = account;
                }
                else
                    selectedAccount = null;
            }
        });

        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(context));
        binding.rvAccounts.setAdapter(accountsAdapter);

        binding.submitABHAAccountBtn.setOnClickListener(v -> {
            if (selectedAccount != null) {
                // pass this account payload to fetch details api and move to Identification screen...
                Toast.makeText(context, "Success: " + selectedAccount.getName(), Toast.LENGTH_SHORT).show();
                callFetchUserProfileAPI(selectedAccount.getABHANumber(), txnId, X_TOKEN);
            }
            else {
                snackbarUtils.showSnackConstraintLayoutParentSuccess(context, binding.layoutParent,
                        StringUtils.getMessageTranslated(getString(R.string.abha_account_selection), sessionManager.getAppLanguage()), false);

            }
        });
    }

    private void callFetchUserProfileAPI(String abhaNumber, String txnId, String xToken) {
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
                                intent = new Intent(context, IdentificationActivity_New.class);
                                intent.putExtra("mobile_payload", abhaProfileResponse);
                                intent.putExtra("accessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("callFetchUserProfileAPI", "onError: " + e.toString());
                            }
                        });
            }
        }).start();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        snackbarUtils.showSnackConstraintLayoutParentSuccess(context, binding.layoutParent,
                StringUtils.getMessageTranslated(getString(R.string.please_click_on_submit_button_to_proceed), sessionManager.getAppLanguage()),
                true);
    }

}