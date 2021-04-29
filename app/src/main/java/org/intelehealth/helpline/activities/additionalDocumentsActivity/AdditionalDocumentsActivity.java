package org.intelehealth.helpline.activities.additionalDocumentsActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;



import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.helpline.R;
import org.intelehealth.helpline.app.AppConstants;
import org.intelehealth.helpline.database.dao.ImagesDAO;
import org.intelehealth.helpline.models.DocumentObject;
import org.intelehealth.helpline.utilities.SessionManager;
import org.intelehealth.helpline.utilities.UuidDictionary;

import org.intelehealth.helpline.activities.cameraActivity.CameraActivity;
import org.intelehealth.helpline.utilities.StringUtils;
import org.intelehealth.helpline.utilities.exception.DAOException;

public class AdditionalDocumentsActivity extends AppCompatActivity {


    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
    private List<DocumentObject> rowListItem;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    SessionManager sessionManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_documents);
        Toolbar topToolBar = findViewById(R.id.toolbar);

        //removes the bug of no translation seen even when provided....
        topToolBar.setTitle(getString(R.string.title_activity_additional_documents));
        setSupportActionBar(topToolBar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

            ImagesDAO imagesDAO = new ImagesDAO();
            ArrayList<String> fileuuidList = new ArrayList<String>();
            ArrayList<File> fileList = new ArrayList<File>();
            try {
                fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);
                for (String fileuuid : fileuuidList) {
                    String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                    if (new File(filename).exists()) {
                        fileList.add(new File(filename));
                    }
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);

            RecyclerView recyclerView = findViewById(R.id.document_RecyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this, rowListItem, AppConstants.IMAGE_PATH);
            recyclerView.setAdapter(recyclerViewAdapter);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                File photo = new File(mCurrentPhotoPath);
                if (photo.exists()) {
                    try{

                        long length = photo.length();
                        length = length/1024;
                        Log.e("------->>>>",length+"");
                    }catch(Exception e){
                        System.out.println("File not found : " + e.getMessage() + e);
                    }

                    recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
                    updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
                }
            }
        }
    }
    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageuuid, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                String imageName = UUID.randomUUID().toString();
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
