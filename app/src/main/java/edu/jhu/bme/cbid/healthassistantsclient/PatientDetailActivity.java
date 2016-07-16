package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PatientDetailActivity extends AppCompatActivity {

    LocalRecordsDatabaseHelper mDbHelper;
    private WebView mWebView;

    String mPatientName;
    String mPatientDob;
    String mAddress;
    String mCityState;
    String mPhone;
    String mSdw;
    String mOccupation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null && intent.getStringArrayListExtra("patientInfo") != null) {
            ArrayList<String> mPatientInfo = intent.getStringArrayListExtra("patientInfo");
            mPatientName = mPatientInfo.get(0) + ", " + mPatientInfo.get(1)
                    + " " + mPatientInfo.get(2);
            mPatientDob = "Date of Birth: " + mPatientInfo.get(3) + " (Age "
                    + mPatientInfo.get(4) + ")";
            mAddress = mPatientInfo.get(5);
            mCityState = mPatientInfo.get(6);
            mPhone = "Phone Number: " + mPatientInfo.get(7);
            mSdw = "Son/Daughter/Wife of " + mPatientInfo.get(8);
            mOccupation = "Occupation: " + mPatientInfo.get(9);

            //TextView textViewName = (TextView) findViewById(R.id.textview_patient_details);
            //textViewName.setText(this.mPatientName);

            getSupportActionBar().setTitle(mPatientName);

            TextView textViewDob = (TextView) findViewById(R.id.textview_patient_info_age);
            textViewDob.setText(mPatientDob);

            TextView textViewSdw = (TextView) findViewById(R.id.textView_sdw);
            textViewSdw.setText(mSdw);

            TextView textViewOcc = (TextView) findViewById(R.id.textview_occup);
            textViewOcc.setText(mOccupation);

            TextView textViewAddr = (TextView) findViewById(R.id.textView_addr1);
            textViewAddr.setText(mAddress);

            TextView textViewCityState = (TextView) findViewById(R.id.textView_addr2);
            textViewCityState.setText(mCityState);

            TextView textViewPhone = (TextView) findViewById(R.id.textView_phone);
            textViewPhone.setText(mPhone);

            Bitmap imageBitmap = BitmapFactory.decodeFile(mPatientInfo.get(11));
            ImageView mImageView = (ImageView) findViewById(R.id.detail_image);
            mImageView.setImageBitmap(imageBitmap);

            UpdatePatientTask upd = new UpdatePatientTask();
            upd.execute(mPatientInfo.get(10));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.detail_print:
                doWebViewPrint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Patient WebView", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument =
                String.format("<h1 id=\"Intelehealth-patient-detail\">Intelehealth Patient Detail</h1>\n" +
                "<h1>%s</h1>\n" +
                "<h2 id=\"basic-information\">Basic Information</h2>\n" +
                "<ul>\n" +
                "<li>%s</li>\n" +
                "<li>%s</li>\n" +
                "<li>%s</li>\n" +
                "</ul>\n" +
                "<h2 id=\"address-and-contact\">Address and Contact</h2>\n" +
                "<p>%s</p>\n" +
                "<p>%s</p>\n" +
                "<p>%s</p>\n" +
                "<h2 id=\"recent-vists\">Recent Vists</h2>\n" +
                "<h2 id=\"patient-history\">Patient History</h2>\n" +
                "<h2 id=\"family-history\">Family History</h2>\n" +
                "<h2 id=\"current-medications\">Current Medications</h2>",
                mPatientName, mPatientDob, mOccupation, mSdw, mAddress, mCityState, mPhone);
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Patient Detail";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    protected class UpdatePatientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(findViewById(R.id.clayout_detail), "Refreshing patient data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        @Override
        public Void doInBackground(String... params) {
            String patientId = params[0];
            queryExtraInfo(patientId);
            loadImage(patientId);

            return null;
        }

        public void queryExtraInfo(String id) {
            // TODO: Connect to OpenMRS via the Middleware API and get more info on the Patient
        }

        public void loadImage(String id) {
            // TODO: Update the image with the picture of the patient as stored on the filesystem
        }

    }

}
