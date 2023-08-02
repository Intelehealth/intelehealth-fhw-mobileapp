package org.intelehealth.ezazi.activities.addNewPatient;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;

public class AddNewPatientActivity extends BaseActionBarActivity {
    private static final String TAG = "AddNewPatientActivity";
    public static final int PAGE_PERSONAL = 0;
    public static final int PAGE_ADDRESS = 1;
    public static final int PAGE_OTHER = 2;
//    private ViewPager2 pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_add_new_patient);
        super.onCreate(savedInstanceState);
        initUI();
    }

    @Override
    protected int getScreenTitle() {
        return R.string.add_patient;
    }

    private void initUI() {
        View viewToolbar = findViewById(R.id.toolbar_common);
        Toolbar toolbar = viewToolbar.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v ->
                onBackPressed()
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, new PatientPersonalInfoFragment())
                .commit();
        changeCurrentButtonState(PAGE_PERSONAL);

//        pager = findViewById(R.id.viewPager);
//        pager.setUserInputEnabled(false);
//        pager.setAdapter(new PatientTabPagerAdapter(getSupportFragmentManager(), getLifecycle()));
//        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                changeCurrentButtonState(position);
//            }
//        });

//        pager.setCurrentItem(0);
       /* Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {

              String  patient_detail = intent.getStringExtra("ScreenEdit");

                Bundle args = intent.getBundleExtra("BUNDLE");
                if (patient_detail.equalsIgnoreCase("personal_edit")) {
                    setScreen(new PatientPersonalInfoFragment());
                } else if (patient_detail.equalsIgnoreCase("address_edit")) {
                    setScreen(new PatientAddressInfoFragment());
                } else if (patient_detail.equalsIgnoreCase("others_edit")) {
                    setScreen(new PatientOtherInfoFragment());
                }

            }*/
    }

    private void changeCurrentButtonState(int position) {
        TextView tvPersonal = findViewById(R.id.tv_personal_info);
        TextView tvAddress = findViewById(R.id.tv_address_info);
        TextView tvOther = findViewById(R.id.tv_other_info);

        if (position == PAGE_PERSONAL) {
            tvPersonal.setSelected(true);
        } else if (position == PAGE_ADDRESS) {
            tvPersonal.setActivated(true);
            tvAddress.setSelected(true);
        } else if (position == PAGE_OTHER) {
            tvOther.setSelected(true);
            tvAddress.setActivated(true);
        }
    }

    public void changeCurrentPage(int position) {
//        pager.setCurrentItem(position);
        changeCurrentButtonState(position);
    }

    private void setScreen(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, fragment)
                .commit();
    }

    /*
        private void setscreen(Fragment fragment) {
            // Bundle data
            Bundle bundle = new Bundle();
            bundle.putSerializable("patientDTO", (Serializable) patientdto);
            Log.v(TAG, "reltion: " + patientID_edit);
            if (patientID_edit != null) {
                bundle.putString("patientUuid", patientID_edit);
            } else {
                bundle.putString("patientUuid", patientdto.getUuid());
            }
            bundle.putBoolean("fromFirstScreen", true);
            bundle.putBoolean("fromSecondScreen", true);
            bundle.putBoolean("fromThirdScreen", true);
            bundle.putBoolean("patient_detail", true);
            fragment.setArguments(bundle); // passing data to Fragment

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, fragment)
                    .commit();
        }
    */
    @Override
    public void onBackPressed() {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .content(getString(R.string.are_you_want_go_back))
                .positiveButtonLabel(R.string.yes)
                .build();

        dialog.setListener(() -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());

//        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
//        alertdialogBuilder.setMessage();
//        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Intent i_back = new Intent(getApplicationContext(), HomeActivity.class);
//                startActivity(i_back);
//            }
//        });
//        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);
//
//        AlertDialog alertDialog = alertdialogBuilder.create();
//        alertDialog.show();
//
//        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
//        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
//
//        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//
//        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

}